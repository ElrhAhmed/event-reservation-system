package ma.projet.events.ui.view.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.ReservationService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.card.StatCard;
import ma.projet.events.ui.layout.AdminLayout;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.List;
import java.util.Map;

@Route(value = "admin/dashboard", layout = AdminLayout.class)
@PageTitle("Dashboard Admin | FESTIVENT")
@RolesAllowed("ADMIN")
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;
    private final SecurityService securityService;

    public AdminDashboardView(UserService userService,
                              EventService eventService,
                              ReservationService reservationService,
                              SecurityService securityService) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.securityService = securityService;

        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);

        // 1. En-tête
        add(createHeader());

        // 2. KPIs Financiers & Réservations
        add(createSectionTitle("Performance Commerciale"));
        add(createBusinessStats());

        // 3. KPIs Utilisateurs
        add(createSectionTitle("Communauté"));
        add(createUserStats());

        // 4. KPIs Événements
        add(createSectionTitle("Catalogue Événements"));
        add(createEventStats());
    }

    private Component createHeader() {
        var userDetails = securityService.getAuthenticatedUser();
        String username = userDetails != null ? userDetails.getUsername() : "Admin";

        H2 title = new H2("Vue d'ensemble");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Span subtitle = new Span("Bienvenue, " + username + ". Voici l'état de santé de la plateforme.");
        subtitle.addClassName(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        header.addClassName(LumoUtility.Margin.Bottom.LARGE);
        return header;
    }

    private Component createSectionTitle(String title) {
        H3 h3 = new H3(title);
        h3.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.MEDIUM);
        return h3;
    }

    /* =================================================================
       SECTION 1 : BUSINESS (Revenus & Résas)
       ================================================================= */
    private Component createBusinessStats() {
        // Utilisation de la méthode existante du ReservationService
        Map<String, Object> stats = reservationService.getReservationStatistics();

        FlexLayout layout = createCardLayout();

        // Revenu Total
        StatCard revenueCard = new StatCard(
                "Volume d'Affaires",
                PriceFormatter.format((Double) stats.get("totalRevenue")),
                VaadinIcon.MONEY,
                "Total généré",
                "var(--lumo-success-color)"
        );
        styleCard(revenueCard);

        // Total Réservations
        StatCard totalResaCard = new StatCard(
                "Réservations Totales",
                stats.get("totalReservations").toString(),
                VaadinIcon.TICKET,
                stats.get("confirmedReservations") + " confirmées",
                "var(--lumo-primary-color)"
        );
        styleCard(totalResaCard);

        // Taux d'annulation (Calculé)
        long total = (Integer) stats.get("totalReservations");
        long cancelled = (Long) stats.get("cancelledReservations");
        double rate = total > 0 ? ((double) cancelled / total) * 100 : 0;

        StatCard cancelCard = new StatCard(
                "Taux d'Annulation",
                String.format("%.1f %%", rate),
                VaadinIcon.BAN,
                cancelled + " réservations annulées",
                "var(--lumo-error-color)"
        );
        styleCard(cancelCard);

        layout.add(revenueCard, totalResaCard, cancelCard);
        return layout;
    }

    /* =================================================================
       SECTION 2 : UTILISATEURS
       ================================================================= */
    private Component createUserStats() {
        FlexLayout layout = createCardLayout();

        // Total
        long totalUsers = userService.countAllUsers();
        StatCard totalCard = new StatCard(
                "Utilisateurs Inscrits",
                String.valueOf(totalUsers),
                VaadinIcon.USERS,
                "Comptes actifs",
                null
        );
        styleCard(totalCard);

        // Clients
        long clients = userService.countUsersByRole(Role.CLIENT);
        StatCard clientCard = new StatCard(
                "Clients",
                String.valueOf(clients),
                VaadinIcon.USER,
                "Acheteurs potentiels",
                "var(--lumo-primary-text-color)"
        );
        styleCard(clientCard);

        // Organisateurs
        long organizers = userService.countUsersByRole(Role.ORGANIZER);
        StatCard orgCard = new StatCard(
                "Organisateurs",
                String.valueOf(organizers),
                VaadinIcon.BRIEFCASE,
                "Créateurs d'événements",
                "var(--lumo-contrast-color)"
        );
        styleCard(orgCard);

        layout.add(totalCard, clientCard, orgCard);
        return layout;
    }

    /* =================================================================
       SECTION 3 : ÉVÉNEMENTS
       ================================================================= */
    private Component createEventStats() {
        // Calcul manuel via Stream car pas de méthode groupée dans le service fourni
        // (Acceptable tant qu'il n'y a pas des milliers d'événements)
        List<Event> allEvents = eventService.getAllEvents();

        long published = allEvents.stream().filter(e -> e.getStatut() == EventStatus.PUBLIE).count();
        long drafts = allEvents.stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count();
        long finished = allEvents.stream().filter(e -> e.getStatut() == EventStatus.TERMINE).count();

        FlexLayout layout = createCardLayout();

        StatCard pubCard = new StatCard(
                "Événements Publiés",
                String.valueOf(published),
                VaadinIcon.CALENDAR_CLOCK,
                "Visibles en ligne",
                "var(--lumo-success-text-color)"
        );
        styleCard(pubCard);

        StatCard draftCard = new StatCard(
                "Brouillons",
                String.valueOf(drafts),
                VaadinIcon.EDIT,
                "En attente de validation",
                "var(--lumo-tertiary-text-color)"
        );
        styleCard(draftCard);

        StatCard finishedCard = new StatCard(
                "Terminés / Passés",
                String.valueOf(finished),
                VaadinIcon.ARCHIVE,
                "Historique",
                "var(--lumo-contrast-color)"
        );
        styleCard(finishedCard);

        layout.add(pubCard, draftCard, finishedCard);
        return layout;
    }

    /* =========================
       UTILITAIRES UI
       ========================= */
    private FlexLayout createCardLayout() {
        FlexLayout layout = new FlexLayout();
        layout.setWidthFull();
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.MEDIUM);
        return layout;
    }

    private void styleCard(StatCard card) {
        card.setMinWidth("250px");
        card.getStyle().set("flex", "1");
    }
}