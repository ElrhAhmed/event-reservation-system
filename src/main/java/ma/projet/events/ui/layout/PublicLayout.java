package ma.projet.events.ui.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.ui.navigation.NavigationManager;

public class PublicLayout extends AppLayout implements RouterLayout {

    private final NavigationManager navigationManager;

    public PublicLayout(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
        createHeader();
        // S'assurer que le contenu prend toute la hauteur
        addClassName(LumoUtility.Display.FLEX);
        addClassName(LumoUtility.FlexDirection.COLUMN);
        addClassName(LumoUtility.MinHeight.FULL);
    }

    private void createHeader() {
        // --- 1. LOGO ---
        // Utilisation d'un Span avec icône pour un logo plus graphique
        Icon logoIcon = VaadinIcon.TICKET.create();
        logoIcon.addClassName(LumoUtility.TextColor.PRIMARY);
        logoIcon.setSize("24px");

        H1 logoText = new H1("FESTIVENT");
        logoText.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE,
                LumoUtility.FontWeight.EXTRABOLD,
                LumoUtility.TextColor.HEADER // Couleur sombre par défaut
        );

        HorizontalLayout logoWrapper = new HorizontalLayout(logoIcon, logoText);
        logoWrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        logoWrapper.setSpacing(true);
        logoWrapper.getStyle().set("cursor", "pointer");
        logoWrapper.addClickListener(e -> navigationManager.goToHome());

        // --- 2. MENU NAVIGATION (Desktop) ---
        HorizontalLayout navLinks = new HorizontalLayout();
        navLinks.addClassNames(LumoUtility.Display.HIDDEN, "md:flex", LumoUtility.Gap.LARGE);
        navLinks.setAlignItems(FlexComponent.Alignment.CENTER);

        Button homeBtn = createNavLink("Accueil", VaadinIcon.HOME);
        homeBtn.addClickListener(e -> navigationManager.goToHome());

        Button eventsBtn = createNavLink("Événements", VaadinIcon.CALENDAR);
        eventsBtn.addClickListener(e -> navigationManager.goToEvents());

        navLinks.add(homeBtn, eventsBtn);

        // --- 3. ACTIONS UTILISATEUR ---
        HorizontalLayout headerActions = new HorizontalLayout();
        headerActions.setAlignItems(FlexComponent.Alignment.CENTER);

        if (navigationManager.isAuthenticated()) {
            Button dashboardBtn = new Button("Mon Espace", new Icon(VaadinIcon.DASHBOARD));
            dashboardBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            dashboardBtn.addClickListener(e -> navigationManager.redirectAfterLogin());

            Button logoutBtn = new Button(new Icon(VaadinIcon.SIGN_OUT));
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            logoutBtn.setTooltipText("Déconnexion");
            logoutBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

            headerActions.add(dashboardBtn, logoutBtn);
        } else {
            Button loginBtn = new Button("Connexion");
            loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            loginBtn.addClassName(LumoUtility.FontWeight.MEDIUM);
            loginBtn.addClickListener(e -> navigationManager.goToLogin());

            Button registerBtn = new Button("S'inscrire");
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.addClickListener(e -> navigationManager.goToRegister());

            headerActions.add(loginBtn, registerBtn);
        }

        // --- ASSEMBLAGE ---
        HorizontalLayout header = new HorizontalLayout(logoWrapper, navLinks, headerActions);
        header.setWidthFull();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Festivent de la classe CSS personnalisée pour le flou et la bordure
        header.addClassName("navbar-header");
        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BoxShadow.SMALL
        );

        addToNavbar(header);
    }

    private Button createNavLink(String text, VaadinIcon icon) {
        Button btn = new Button(text, icon.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);
        return btn;
    }
}