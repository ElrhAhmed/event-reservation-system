package ma.projet.events.ui.view.client;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*; // Important pour BeforeEnterObserver
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.Reservation;
import ma.projet.events.entity.User;
import ma.projet.events.exception.BusinessException;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.form.ReservationForm;
import ma.projet.events.ui.layout.UserLayout;
import ma.projet.events.ui.navigation.NavigationManager;

import java.util.Optional;

// 1. La route reste la même, avec le paramètre :eventID
@Route(value = "event/:eventID/reserve", layout = UserLayout.class)
@PageTitle("Réserver | FESTIVENT")
@RolesAllowed("CLIENT")
// 2. CHANGEMENT : On remplace HasUrlParameter<Long> par BeforeEnterObserver
public class ReservationFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    public ReservationFormView(EventService eventService,
                               ReservationService reservationService,
                               UserService userService,
                               SecurityService securityService,
                               NavigationManager navigationManager) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.userService = userService;
        this.securityService = securityService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName(LumoUtility.Background.CONTRAST_5);
    }

    // 3. CHANGEMENT : On utilise beforeEnter au lieu de setParameter
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        removeAll();

        // Extraction du paramètre :eventID depuis la route
        Optional<String> eventIdParam = event.getRouteParameters().get("eventID");

        if (eventIdParam.isPresent()) {
            try {
                Long eventId = Long.parseLong(eventIdParam.get());
                loadEventData(eventId);
            } catch (NumberFormatException e) {
                showErrorState("ID d'événement invalide.");
            }
        } else {
            showErrorState("Aucun événement spécifié.");
        }
    }

    // Méthode extraite pour charger les données proprement
    private void loadEventData(Long eventId) {
        try {
            Event event = eventService.getEventById(eventId);
            int availablePlaces = eventService.calculateAvailablePlaces(eventId);

            if (availablePlaces <= 0 || !event.isReservable()) {
                showErrorState("Cet événement est complet ou les ventes sont fermées.");
                return;
            }

            buildBookingUI(event, availablePlaces);

        } catch (Exception e) {
            showErrorState("Événement introuvable ou erreur technique.");
        }
    }

    private void buildBookingUI(Event event, int availablePlaces) {
        // ... (Ce code reste IDENTIQUE à avant) ...
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("600px");
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.LARGE,
                LumoUtility.BorderRadius.LARGE
        );

        H2 title = new H2("Finaliser votre réservation");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.MEDIUM);

        Span availabilityBadge = new Span(availablePlaces + " places restantes actuellement");
        availabilityBadge.getElement().getThemeList().add("badge " + (availablePlaces < 10 ? "error" : "success"));

        ReservationForm form = new ReservationForm(event);
        form.setOnSubmit(places -> processReservation(event, places));

        Button backBtn = new Button("Annuler et retour", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> navigationManager.goToEventDetail(event.getId()));

        card.add(title, availabilityBadge, form, backBtn);
        add(card);
    }

    private void processReservation(Event event, int places) {
        // ... (Code identique à avant) ...
        try {
            var userDetails = securityService.getAuthenticatedUser();
            if (userDetails == null) {
                navigationManager.goToLogin();
                return;
            }
            User currentUser = userService.getUserByEmail(userDetails.getUsername());

            Reservation reservation = reservationService.reserverTicket(event.getId(), currentUser.getId(), places);
            showSuccessDialog(reservation);

        } catch (BusinessException e) {
            Notification.show(e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Erreur inattendue : " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void showSuccessDialog(Reservation reservation) {
        // ... (Code identique à avant) ...
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Réservation Confirmée !");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(Alignment.CENTER);
        content.setSpacing(true);

        Icon check = VaadinIcon.CHECK_CIRCLE.create();
        check.setColor("var(--lumo-success-color)");
        check.setSize("64px");

        H3 code = new H3(reservation.getCodeReservation());
        code.addClassName(LumoUtility.TextColor.PRIMARY);

        Span msg = new Span("Vos billets ont été réservés avec succès.");
        Span emailMsg = new Span("Un email de confirmation vous sera envoyé.");
        emailMsg.addClassName(LumoUtility.TextColor.SECONDARY);

        content.add(check, new H4("Merci pour votre commande"), code, msg, emailMsg);

        Button myReservationsBtn = new Button("Voir mes réservations", e -> {
            dialog.close();
            navigationManager.goToClientReservations();
        });
        myReservationsBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button homeBtn = new Button("Retour à l'accueil", e -> {
            dialog.close();
            navigationManager.goToHome();
        });

        dialog.add(content);
        dialog.getFooter().add(homeBtn, myReservationsBtn);
        dialog.open();
    }

    private void showErrorState(String message) {
        // ... (Code identique à avant) ...
        VerticalLayout errorLayout = new VerticalLayout();
        errorLayout.setAlignItems(Alignment.CENTER);

        Icon icon = VaadinIcon.WARNING.create();
        icon.setSize("48px");
        icon.setColor("var(--lumo-error-color)");

        H3 title = new H3("Réservation impossible");
        Span msg = new Span(message);

        Button backBtn = new Button("Retour au catalogue", e -> navigationManager.goToEvents());

        errorLayout.add(icon, title, msg, backBtn);
        add(errorLayout);
    }
}