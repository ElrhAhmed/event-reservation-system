package ma. projet.events.ui.layout;

import com.vaadin.flow.component.UI;
import com.vaadin. flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow. component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.router.RouterLink;
import ma. projet.events.ui.view. publicview.EventListView;
import ma.projet.events.ui.view.publicview.HomeView;

/**
 * Layout pour les pages publiques (sans authentification)
 * Utilisé par : LoginView, RegisterView, HomeView, EventListView, EventDetailView
 */
public class PublicLayout extends AppLayout {

    public PublicLayout() {
        createHeader();
    }

    /**
     * Crée le header avec logo et navigation
     */
    private void createHeader() {
        // Logo avec icône
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
        RouterLink homeLink = new RouterLink("", HomeView. class);
        homeLink.add(logoLayout);
        homeLink.getStyle().set("text-decoration", "none");

        // Navigation publique
        HorizontalLayout navigation = createNavigation();

        // Boutons Connexion / Inscription
        HorizontalLayout authButtons = createAuthButtons();

        // Layout du header
        HorizontalLayout header = new HorizontalLayout(
                homeLink,
                navigation,
                authButtons
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setSpacing(true);
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "var(--festivent-bg-card)")
                .set("box-shadow", "var(--festivent-shadow-md)");

        // Expansion pour pousser les boutons auth à droite
        header.expand(navigation);

        addToNavbar(header);
    }

    /**
     * Crée les liens de navigation publique
     */
    private HorizontalLayout createNavigation() {
        // Lien Accueil
        RouterLink accueilLink = new RouterLink("Accueil", HomeView. class);
        accueilLink.getStyle()
                .set("color", "var(--festivent-text-primary)")
                .set("text-decoration", "none")
                .set("font-weight", "500")
                .set("margin-left", "var(--lumo-space-l)");

        // ✅ CORRECTION : Lien vers EventListView
        RouterLink eventsLink = new RouterLink("Événements", EventListView.class);
        eventsLink.getStyle()
                .set("color", "var(--festivent-text-primary)")
                .set("text-decoration", "none")
                .set("font-weight", "500")
                .set("margin-left", "var(--lumo-space-l)");

        HorizontalLayout nav = new HorizontalLayout(accueilLink, eventsLink);
        nav.setSpacing(true);
        return nav;
    }

    /**
     * Crée les boutons Connexion / Inscription
     */
    private HorizontalLayout createAuthButtons() {
        // ✅ CORRECTION : Bouton Connexion avec navigation
        Button loginButton = new Button("Connexion", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        loginButton.getStyle().set("color", "var(--festivent-primary)");
        loginButton.addClickListener(e -> {
            UI.getCurrent().navigate("login");
        });

        // ✅ CORRECTION :  Bouton Inscription avec navigation
        Button registerButton = new Button("Inscription", VaadinIcon. USER_CHECK.create());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton. addClickListener(e -> {
            UI.getCurrent().navigate("register");
        });

        HorizontalLayout buttons = new HorizontalLayout(loginButton, registerButton);
        buttons.setSpacing(true);
        return buttons;
    }
}