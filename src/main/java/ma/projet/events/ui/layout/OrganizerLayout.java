package ma.projet.events.ui.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.security.SecurityService;
import ma.projet.events.ui.navigation.NavigationManager;
import org.springframework.security.core.userdetails.UserDetails;

public class OrganizerLayout extends AppLayout implements RouterLayout {

    private final NavigationManager navigationManager;
    private final SecurityService securityService;

    public OrganizerLayout(NavigationManager navigationManager, SecurityService securityService) {
        this.navigationManager = navigationManager;
        this.securityService = securityService;

        createHeader();
        createDrawer();

        addClassName(LumoUtility.Background.CONTRAST_5);
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName(LumoUtility.TextColor.SECONDARY);

        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.TextColor.PRIMARY);

        Span badge = new Span("ORGANIZER");
        badge.getElement().getThemeList().add("badge contrast small");
        badge.addClassName(LumoUtility.Margin.Start.SMALL);

        HorizontalLayout branding = new HorizontalLayout(logo, badge);
        branding.setAlignItems(FlexComponent.Alignment.CENTER);
        branding.getStyle().set("cursor", "pointer");
        branding.addClickListener(e -> navigationManager.goToOrganizerDashboard());

        // User Info
        UserDetails user = securityService.getAuthenticatedUser();
        String username = (user != null) ? user.getUsername() : "Organizer";

        Avatar avatar = new Avatar(username);
        avatar.addThemeVariants(com.vaadin.flow.component.avatar.AvatarVariant.LUMO_XSMALL);
        avatar.getStyle().set("background-color", "#8b5cf6"); // Violet pour orga
        avatar.getStyle().set("color", "white");

        HorizontalLayout userArea = new HorizontalLayout(new Span(username), avatar);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);
        userArea.getStyle().set("cursor", "pointer");
        userArea.addClickListener(e -> navigationManager.goToProfile());

        HorizontalLayout header = new HorizontalLayout(toggle, branding, userArea);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(branding);

        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Background.BASE
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        H2 menuTitle = new H2("Gestion");
        menuTitle.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Top.MEDIUM, LumoUtility.TextColor.TERTIARY, LumoUtility.TextTransform.UPPERCASE);

        VerticalLayout navList = new VerticalLayout();
        navList.setPadding(false);
        navList.setSpacing(false);

        navList.add(createNavItem("Tableau de bord", VaadinIcon.CHART, navigationManager::goToOrganizerDashboard));
        navList.add(createNavItem("Mes Événements", VaadinIcon.LIST, navigationManager::goToMyEvents));


        Component createBtn = createNavItem("Créer un événement", VaadinIcon.PLUS_CIRCLE, navigationManager::goToCreateEvent);
        createBtn.getElement().getStyle().set("color", "var(--lumo-primary-text-color)");
        createBtn.getElement().getStyle().set("font-weight", "bold");
        navList.add(createBtn);

        navList.add(createNavItem("Réservations", VaadinIcon.TICKET, navigationManager::goToOrganizerReservations));

        VerticalLayout spacer = new VerticalLayout();
        spacer.setHeight("2rem");

        H2 accountTitle = new H2("Compte");
        accountTitle.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.Padding.Horizontal.LARGE, LumoUtility.TextColor.TERTIARY, LumoUtility.TextTransform.UPPERCASE);

        // CORRECTION ICI
        Component profileBtn = createNavItem("Mon Profil", VaadinIcon.USER_CARD, navigationManager::goToProfile);

        Component logoutBtn = createNavItem("Déconnexion", VaadinIcon.SIGN_OUT, securityService::logout);
        logoutBtn.getElement().getStyle().set("color", "var(--lumo-error-text-color)");

        VerticalLayout drawerContent = new VerticalLayout(
                menuTitle, navList,
                spacer,
                accountTitle, profileBtn, logoutBtn
        );
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        addToDrawer(new Scroller(drawerContent));
    }

    private Component createNavItem(String label, VaadinIcon icon, Runnable action) {
        // 1. Icône
        Icon i = icon.create();
        i.setSize("20px");
        i.getStyle().set("color", "var(--lumo-primary-color)");
        i.getStyle().set("min-width", "20px"); // Empêche l'écrasement
        i.getStyle().set("margin-right", "16px"); // Espace propre entre icône et texte

        // 2. Texte
        Span text = new Span(label);
        text.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.FontWeight.MEDIUM);
        text.getStyle().set("color", "var(--lumo-body-text-color)");

        // 3. Conteneur (Le "Bouton")
        HorizontalLayout item = new HorizontalLayout(i, text);
        item.setWidthFull();
        item.setAlignItems(FlexComponent.Alignment.CENTER);

        // --- LE SECRET DE L'ALIGNEMENT ---
        // On définit le padding ici. "12px" haut/bas, "24px" gauche/droite
        item.getStyle().set("padding", "12px 24px");
        item.getStyle().set("cursor", "pointer");
        item.getStyle().set("border-radius", "0 24px 24px 0"); // Arrondi à droite
        item.getStyle().set("transition", "background-color 0.2s"); // Animation douce

        // 4. Interaction (Click & Hover)
        item.addClickListener(e -> action.run());

        // Effet de survol simple en Java
        item.getElement().addEventListener("mouseenter", e ->
                item.getStyle().set("background-color", "var(--lumo-contrast-5pct)"));
        item.getElement().addEventListener("mouseleave", e ->
                item.getStyle().remove("background-color"));

        return item;
    }
}