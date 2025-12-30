package ma.projet.events.ui.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
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

        // Styles globaux du layout
        addClassName(LumoUtility.Display.FLEX);
        addClassName(LumoUtility.FlexDirection.COLUMN);
        addClassName(LumoUtility.Height.FULL);
    }

    private void createHeader() {
        // --- 1. LOGO ---
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextColor.PRIMARY
        );
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> navigationManager.goToHome());

        // --- 2. MENU DE NAVIGATION (NOUVEAU) ---
        HorizontalLayout navLinks = new HorizontalLayout();
        navLinks.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.MEDIUM); // Masqué sur mobile, visible sur Desktop

        Button homeBtn = new Button("Accueil", e -> navigationManager.goToHome());
        homeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button eventsBtn = new Button("Événements", e -> navigationManager.goToEvents());
        eventsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        navLinks.add(homeBtn, eventsBtn);

        // --- 3. ACTIONS (Login/Register) ---
        HorizontalLayout headerActions = new HorizontalLayout();
        headerActions.addClassNames(LumoUtility.AlignItems.CENTER);

        if (navigationManager.isAuthenticated()) {
            Button dashboardBtn = new Button("Mon Espace", new Icon(VaadinIcon.DASHBOARD));
            dashboardBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            dashboardBtn.addClickListener(e -> navigationManager.redirectAfterLogin());

            Button logoutBtn = new Button(new Icon(VaadinIcon.SIGN_OUT));
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            logoutBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().setLocation("/logout")));

            headerActions.add(dashboardBtn, logoutBtn);
        } else {
            Button loginBtn = new Button("Se connecter");
            loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            loginBtn.addClickListener(e -> navigationManager.goToLogin());

            Button registerBtn = new Button("S'inscrire");
            registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            registerBtn.addClickListener(e -> navigationManager.goToRegister());

            headerActions.add(loginBtn, registerBtn);
        }

        // --- ASSEMBLAGE ---
        HorizontalLayout header = new HorizontalLayout(logo, navLinks, headerActions);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Background.BASE
        );
        // Espace entre les éléments : Logo à gauche, Menu au centre (via expand), Actions à droite
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Petite astuce : on s'assure que le header ne dépasse pas la largeur d'écran
        header.getStyle().set("box-sizing", "border-box");

        addToNavbar(header);
    }
}