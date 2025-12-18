package ma.projet.events.ui. layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin. flow.component.applayout.AppLayout;
import com.vaadin.flow.component. applayout.DrawerToggle;
import com.vaadin.flow. component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow. component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin. flow.component.html.Span;
import com.vaadin. flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import ma.projet.events.ui.view.publicview.EventListView;
import ma.projet.events.ui.view.publicview.HomeView;

/**
 * Layout pour les pages client (après authentification)
 * Utilisé par :  ClientDashboardView, MyReservationsView, ReservationFormView
 *
 * Phase 5 : Interface uniquement (utilisateur simulé)
 * Phase 10 : Intégration Spring Security (vrai utilisateur connecté)
 *
 * NOTE : Les imports ClientDashboardView et MyReservationsView sont commentés
 * car ces vues n'existent pas encore.  Ils seront activés après leur création.
 */
public class ClientLayout extends AppLayout {

    // TODO Phase 10 : Récupérer le vrai utilisateur connecté via SecurityContext
    private static final String SIMULATED_USER_NAME = "Ahmed Benali";
    private static final String SIMULATED_USER_EMAIL = "ahmed.benali@festivent.com";

    public ClientLayout() {
        createHeader();
        createDrawer();
    }

    /**
     * Crée le header avec logo, toggle sidebar et bouton déconnexion
     */
    private void createHeader() {
        // Toggle pour ouvrir/fermer la sidebar (mobile friendly)
        DrawerToggle toggle = new DrawerToggle();

        // Logo
        Icon logoIcon = VaadinIcon.STAR.create();
        logoIcon.getStyle()
                .set("color", "var(--festivent-accent)")
                .set("margin-right", "var(--lumo-space-s)");

        H1 logo = new H1("Festivent");
        logo.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("margin", "0")
                .set("color", "var(--festivent-primary)")
                .set("font-weight", "700");

        HorizontalLayout logoLayout = new HorizontalLayout(logoIcon, logo);
        logoLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        logoLayout.setSpacing(false);

        // Lien vers l'accueil sur le logo
        RouterLink homeLink = new RouterLink("", HomeView.class);
        homeLink.add(logoLayout);
        homeLink.getStyle().set("text-decoration", "none");

        // Bouton déconnexion
        Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.getStyle().set("color", "var(--festivent-error)");

        // TODO Phase 10 : Vraie déconnexion avec Spring Security
        logoutButton. addClickListener(e -> {
            UI.getCurrent().navigate("");
        });

        // Layout du header
        HorizontalLayout header = new HorizontalLayout(
                toggle,
                homeLink,
                logoutButton
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setSpacing(true);
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "var(--festivent-bg-card)")
                .set("box-shadow", "var(--festivent-shadow-md)");

        // Expansion pour pousser le bouton logout à droite
        header.expand(homeLink);

        addToNavbar(header);
    }

    /**
     * Crée la sidebar avec navigation client
     */
    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.setSpacing(false);
        drawer.setPadding(true);
        drawer.setWidth("280px");
        drawer.getStyle()
                .set("background-color", "var(--festivent-bg)")
                .set("height", "100%");

        // Section profil utilisateur
        VerticalLayout userSection = createUserSection();

        // Séparateur
        Hr separator1 = new Hr();

        // Navigation client
        VerticalLayout clientNav = createClientNavigation();

        // Séparateur
        Hr separator2 = new Hr();

        // Navigation publique (accès rapide)
        VerticalLayout publicNav = createPublicNavigation();

        drawer.add(userSection, separator1, clientNav, separator2, publicNav);

        addToDrawer(drawer);
    }

    /**
     * Crée la section profil utilisateur
     */
    private VerticalLayout createUserSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(false);
        section.setPadding(true);
        section.getStyle()
                .set("background-color", "var(--festivent-bg-card)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-m)");

        // Icône utilisateur
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("48px");
        userIcon.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin-bottom", "var(--lumo-space-s)");

        // Nom
        Span userName = new Span(SIMULATED_USER_NAME);
        userName.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "var(--festivent-text-primary)");

        // Email
        Span userEmail = new Span(SIMULATED_USER_EMAIL);
        userEmail.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)");

        section.add(userIcon, userName, userEmail);
        section.setAlignItems(FlexComponent.Alignment. CENTER);

        return section;
    }

    /**
     * Crée la navigation client (section principale)
     *
     * NOTE TEMPORAIRE : Les liens utilisent HomeView.class en attendant
     * la création de ClientDashboardView et MyReservationsView
     */
    private VerticalLayout createClientNavigation() {
        VerticalLayout nav = new VerticalLayout();
        nav.setSpacing(false);
        nav.setPadding(false);

        // Titre section
        Span title = new Span("Mon espace");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-transform", "uppercase")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("margin-bottom", "var(--lumo-space-s)");

        // ✅ TEMPORAIRE : Utilisation de HomeView.class en attendant ClientDashboardView
        // TODO :  Remplacer par ClientDashboardView. class après création
        RouterLink dashboardLink = createNavLink(
                VaadinIcon.DASHBOARD.create(),
                "Tableau de bord",
                HomeView.class  // ← TEMPORAIRE
        );

        // ✅ TEMPORAIRE :  Utilisation de HomeView.class en attendant MyReservationsView
        // TODO :  Remplacer par MyReservationsView.class après création
        RouterLink reservationsLink = createNavLink(
                VaadinIcon.TICKET.create(),
                "Mes réservations",
                HomeView.class  // ← TEMPORAIRE
        );

        nav.add(title, dashboardLink, reservationsLink);

        return nav;
    }

    /**
     * Crée la navigation publique (accès rapide)
     */
    private VerticalLayout createPublicNavigation() {
        VerticalLayout nav = new VerticalLayout();
        nav.setSpacing(false);
        nav.setPadding(false);

        // Titre section
        Span title = new Span("Explorer");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-transform", "uppercase")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px")
                .set("margin-bottom", "var(--lumo-space-s)");

        // ✅ CORRECT : Utilisation de . class
        RouterLink homeLink = createNavLink(
                VaadinIcon.HOME.create(),
                "Accueil",
                HomeView. class
        );

        RouterLink eventsLink = createNavLink(
                VaadinIcon.CALENDAR.create(),
                "Événements",
                EventListView. class
        );

        nav.add(title, homeLink, eventsLink);

        return nav;
    }

    /**
     * Crée un lien de navigation stylisé
     *
     * @param icon Icône à afficher
     * @param text Texte du lien
     * @param navigationTarget Classe de la vue cible (DOIT être une Class, pas une String)
     * @return RouterLink stylisé
     */
    private RouterLink createNavLink(Icon icon, String text, Class<? > navigationTarget) {
        icon.setSize("20px");
        icon.getStyle().set("margin-right", "var(--lumo-space-m)");

        Span label = new Span(text);
        label.getStyle().set("font-size", "var(--lumo-font-size-m)");

        HorizontalLayout content = new HorizontalLayout(icon, label);
        content.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);

        // ✅ CORRECT : navigationTarget est de type Class<?>
        RouterLink link = new RouterLink("", (Class<? extends Component>) navigationTarget);
        link.add(content);
        link.getStyle()
                .set("text-decoration", "none")
                .set("color", "var(--festivent-text-primary)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("display", "block")
                .set("transition", "background-color 0.2s");

        // Hover effect (JavaScript events)
        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
        });

        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle().set("background-color", "transparent");
        });

        return link;
    }
}