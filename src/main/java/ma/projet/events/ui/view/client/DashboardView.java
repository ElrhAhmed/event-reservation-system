package ma.projet.events.ui.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Reservation;
import ma.projet.events.entity.ReservationStatus;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.card.StatCard;
import ma.projet.events.ui.layout.UserLayout;
import ma.projet.events.ui.navigation.NavigationManager;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route(value = "dashboard", layout = UserLayout.class)
@PageTitle("Tableau de bord | FESTIVENT")
@RolesAllowed({"CLIENT", "ADMIN", "ORGANIZER"})
public class DashboardView extends VerticalLayout {

    private final UserService userService;
    private final ReservationService reservationService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    public DashboardView(UserService userService,
                         ReservationService reservationService,
                         SecurityService securityService,
                         NavigationManager navigationManager) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.securityService = securityService;
        this.navigationManager = navigationManager;

        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        // 1. Récupération de l'utilisateur connecté
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            add(new Span("Erreur : Utilisateur non identifié"));
            return;
        }

        // 2. En-tête
        add(createHeader(currentUser));

        // 3. Cartes de Statistiques
        add(createStatsSection(currentUser.getId()));

        // 4. Actions Rapides
        add(createQuickActions());

        // 5. Dernières réservations
        add(createRecentReservationsSection(currentUser.getId()));
    }

    private User getCurrentUser() {
        var userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            return userService.getUserByEmail(userDetails.getUsername());
        }
        return null;
    }

    private VerticalLayout createHeader(User user) {
        H2 title = new H2("Bonjour, " + user.getPrenom() + " !");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Span subtitle = new Span("Ravi de vous revoir sur FestiVent.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.MEDIUM);

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private FlexLayout createStatsSection(Long userId) {
        Map<String, Object> stats = userService.getUserStatistics(userId);

        FlexLayout layout = new FlexLayout();
        layout.setWidthFull();
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.MEDIUM);

        // Carte 1 : Total Réservations (Bleu)
        StatCard totalResa = new StatCard(
                "Réservations",
                String.valueOf(stats.get("totalReservations")),
                VaadinIcon.TICKET,
                stats.get("reservationsConfirmees") + " confirmées",
                "#6366f1" // Indigo
        );
        styleCard(totalResa);

        // Carte 2 : Dépenses (Vert)
        StatCard depenses = new StatCard(
                "Total Dépensé",
                stats.get("montantTotalDepense") + " DH",
                VaadinIcon.WALLET,
                "Total cumulé",
                "#10b981" // Vert
        );
        styleCard(depenses);

        // Carte 3 : Places (Orange)
        StatCard places = new StatCard(
                "Billets achetés",
                String.valueOf(stats.get("totalPlacesReservees")),
                VaadinIcon.GROUP,
                "Sièges réservés",
                "#f59e0b" // Orange
        );
        styleCard(places);

        layout.add(totalResa, depenses, places);
        return layout;
    }

    private void styleCard(StatCard card) {
        card.setMinWidth("260px");
        card.getStyle().set("flex", "1");
    }

    private VerticalLayout createQuickActions() {
        H3 title = new H3("Accès Rapide");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.LARGE);

        FlexLayout actions = new FlexLayout();
        actions.setWidthFull();
        actions.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        actions.addClassName(LumoUtility.Gap.MEDIUM);

        actions.add(createActionButton("Parcourir les événements", VaadinIcon.SEARCH, ButtonVariant.LUMO_PRIMARY, navigationManager::goToEvents));
        actions.add(createActionButton("Mes Réservations", VaadinIcon.LIST, ButtonVariant.LUMO_CONTRAST, navigationManager::goToClientReservations));
        actions.add(createActionButton("Mon Profil", VaadinIcon.USER, ButtonVariant.LUMO_CONTRAST, navigationManager::goToProfile));

        return new VerticalLayout(title, actions);
    }

    private Button createActionButton(String label, VaadinIcon icon, ButtonVariant variant, Runnable action) {
        Button btn = new Button(label, new Icon(icon));
        btn.addThemeVariants(variant);
        btn.setHeight("50px");
        btn.addClickListener(e -> action.run());
        return btn;
    }

    private VerticalLayout createRecentReservationsSection(Long userId) {
        H3 title = new H3("Activité Récente");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.LARGE);

        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(true);

        List<Reservation> reservations = reservationService.findUserReservations(userId);

        if (reservations.isEmpty()) {
            Span empty = new Span("Aucune activité récente.");
            empty.addClassName(LumoUtility.TextColor.SECONDARY);
            list.add(empty);
        } else {
            reservations.stream()
                    .sorted((r1, r2) -> r2.getDateReservation().compareTo(r1.getDateReservation()))
                    .limit(3)
                    .forEach(r -> list.add(createMiniReservationItem(r)));
        }
        return new VerticalLayout(title, list);
    }

    private HorizontalLayout createMiniReservationItem(Reservation r) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.CENTER);
        row.setJustifyContentMode(JustifyContentMode.BETWEEN);
        row.addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.XSMALL, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.MEDIUM);
        row.getStyle().set("border", "1px solid var(--lumo-contrast-5pct)");

        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);

        Icon icon = VaadinIcon.TICKET.create();
        icon.addClassName(LumoUtility.TextColor.PRIMARY);
        icon.setSize("20px");

        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        Span eventName = new Span(r.getEvenement().getTitre());
        eventName.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.SMALL);

        Span date = new Span(r.getDateReservation().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        date.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        info.add(eventName, date);
        left.add(icon, info);

        Span status = new Span(r.getStatut().getLabel());
        status.getElement().getThemeList().add("badge pill " + (r.getStatut() == ReservationStatus.CONFIRMEE ? "success" : "contrast"));

        row.add(left, status);
        return row;
    }
}