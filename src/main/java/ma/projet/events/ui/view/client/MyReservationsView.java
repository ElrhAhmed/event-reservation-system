package ma.projet.events.ui.view.client;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Reservation;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.card.ReservationCard;
import ma.projet.events.ui.component.filter.ReservationFilterBar;
import ma.projet.events.ui.layout.UserLayout;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "my-reservations", layout = UserLayout.class)
@PageTitle("Mes Réservations | FESTIVENT")
@RolesAllowed("CLIENT")
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private final SecurityService securityService;
    private final UserService userService;

    private final Div reservationsGrid;
    private Span countBadge;

    // Données locales pour le filtrage
    private List<Reservation> allReservations;

    public MyReservationsView(ReservationService reservationService,
                              SecurityService securityService,
                              UserService userService) {
        this.reservationService = reservationService;
        this.securityService = securityService;
        this.userService = userService;
        this.countBadge = new Span("0"); //


        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        // 1. En-tête
        HorizontalLayout header = createHeader();

        // 2. Barre de filtres
        // Note: On passe des listes vides pour Events et Users car le Client ne filtre que ses propres données
        ReservationFilterBar filterBar = new ReservationFilterBar(
                Collections.emptyList(),
                Collections.emptyList(),
                this::applyFilter
        );
        // Astuce UX : On masque les combobox Event et User qui sont inutiles pour le client
        // On suppose que ce sont les composants aux index 2 et 3 du layout (Code=0, Statut=1)
        customizeFilterBarForClient(filterBar);

        // 3. Grille responsive
        reservationsGrid = new Div();
        reservationsGrid.setWidthFull();
        reservationsGrid.addClassName(LumoUtility.Margin.Top.MEDIUM);
        reservationsGrid.getStyle().set("display", "grid");
        reservationsGrid.getStyle().set("grid-template-columns", "repeat(auto-fill, minmax(350px, 1fr))");
        reservationsGrid.getStyle().set("gap", "1.5rem");

        add(header, filterBar, reservationsGrid);

        // Chargement initial
        loadData();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Mes Réservations");
        title.addClassNames(LumoUtility.Margin.NONE);

        countBadge = new Span("0");
        countBadge.getElement().getThemeList().add("badge contrast");

        HorizontalLayout header = new HorizontalLayout(title, countBadge);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private void customizeFilterBarForClient(ReservationFilterBar filterBar) {
        // Cette méthode tente de masquer les filtres "Event" et "User" qui sont superflus pour un client
        // ReservationFilterBar extends HorizontalLayout.
        // L'ordre d'ajout dans ReservationFilterBar est: code, statut, event, user, filter, reset
        try {
            // Event ComboBox (index 2)
            filterBar.getComponentAt(2).setVisible(false);
            // User ComboBox (index 3)
            filterBar.getComponentAt(3).setVisible(false);
        } catch (Exception e) {
            // Sécurité silencieuse si la structure change
        }
    }

    private void loadData() {
        var userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            User user = userService.getUserByEmail(userDetails.getUsername());
            allReservations = reservationService.findUserReservations(user.getId());

            // Tri par défaut : Plus récent d'abord
            allReservations.sort(Comparator.comparing(Reservation::getDateReservation).reversed());

            updateGrid(allReservations);
        }
    }

    private void applyFilter(ReservationFilterBar.ReservationFilter filter) {
        if (allReservations == null) return;

        List<Reservation> filtered = allReservations.stream()
                .filter(r -> {
                    boolean matchesCode = filter.code == null || filter.code.isBlank() ||
                            r.getCodeReservation().toLowerCase().contains(filter.code.toLowerCase());
                    boolean matchesStatut = filter.statut == null || r.getStatut() == filter.statut;

                    return matchesCode && matchesStatut;
                })
                .collect(Collectors.toList());

        updateGrid(filtered);
    }

    private void updateGrid(List<Reservation> items) {
        reservationsGrid.removeAll();
        countBadge.setText(String.valueOf(items.size()));

        if (items.isEmpty()) {
            Div emptyState = new Div(new Span("Aucune réservation trouvée."));
            emptyState.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.TextAlignment.CENTER, LumoUtility.Padding.LARGE);
            // Centrer dans la grille
            emptyState.getStyle().set("grid-column", "1 / -1");
            reservationsGrid.add(emptyState);
            return;
        }

        for (Reservation reservation : items) {
            ReservationCard card = new ReservationCard(reservation);

            // Action : Voir détails (Dialog)
            card.setOnView(() -> showDetailsDialog(reservation));

            // Action : Annuler
            card.setOnCancel(() -> showCancellationConfirmation(reservation));

            reservationsGrid.add(card);
        }
    }

    /* =================================================================
       ACTIONS (DIALOGS)
       ================================================================= */

    private void showDetailsDialog(Reservation r) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Détails de la réservation");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setMinWidth("400px");

        content.add(createDetailRow("Code", r.getCodeReservation(), true));
        content.add(createDetailRow("Événement", r.getEvenement().getTitre(), false));
        content.add(createDetailRow("Date", DateFormatter.format(r.getEvenement().getDateDebut()), false));
        content.add(createDetailRow("Lieu", r.getEvenement().getLieu() + ", " + r.getEvenement().getVille(), false));
        content.add(new Hr());
        content.add(createDetailRow("Places", r.getNombrePlaces() + " personnes", false));
        content.add(createDetailRow("Total", PriceFormatter.format(r.getMontantTotal()), true));
        content.add(createDetailRow("Date commande", DateFormatter.format(r.getDateReservation()), false));

        Button closeBtn = new Button("Fermer", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(content);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value, boolean bold) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span lbl = new Span(label);
        lbl.addClassName(LumoUtility.TextColor.SECONDARY);

        Span val = new Span(value);
        if (bold) val.addClassName(LumoUtility.FontWeight.BOLD);

        row.add(lbl, val);
        return row;
    }

    private void showCancellationConfirmation(Reservation r) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Annuler la réservation ?");
        dialog.add(new Text("Cette action est irréversible. Voulez-vous vraiment annuler votre réservation pour " + r.getEvenement().getTitre() + " ?"));

        Button confirmBtn = new Button("Oui, annuler", e -> {
            try {
                // Récupération ID user sécurisé
                User user = userService.getUserByEmail(securityService.getAuthenticatedUser().getUsername());

                reservationService.annulerReservation(r.getId(), user.getId());

                Notification.show("Réservation annulée avec succès")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadData(); // Recharger la grille

            } catch (Exception ex) {
                Notification.show("Erreur : " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Non, garder", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, confirmBtn);
        dialog.open();
    }
}