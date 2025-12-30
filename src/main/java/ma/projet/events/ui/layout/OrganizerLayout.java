package ma.projet.events.ui.layout;

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

        addClassName(LumoUtility.Height.FULL);
    }

    private void createHeader() {
        // Toggle Mobile
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName(LumoUtility.Margin.End.MEDIUM);

        // Logo & Titre de section
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Span badge = new Span("ORGANIZER");
        badge.getElement().getThemeList().add("badge contrast");
        badge.addClassName(LumoUtility.Margin.Start.SMALL);

        HorizontalLayout branding = new HorizontalLayout(logo, badge);
        branding.setAlignItems(FlexComponent.Alignment.CENTER);
        branding.getStyle().set("cursor", "pointer");
        branding.addClickListener(e -> navigationManager.goToOrganizerDashboard());

        // User Info (Droite)
        UserDetails user = securityService.getAuthenticatedUser();
        String username = (user != null) ? user.getUsername() : "Organizer";

        Avatar avatar = new Avatar(username);
        avatar.addThemeVariants(com.vaadin.flow.component.avatar.AvatarVariant.LUMO_XSMALL);

        HorizontalLayout userArea = new HorizontalLayout(new Span(username), avatar);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);


        // Assemblage
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
        H2 menuTitle = new H2("Gestion Événements");
        menuTitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.Padding.Top.MEDIUM,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.TextTransform.UPPERCASE
        );

        VerticalLayout navList = new VerticalLayout();
        navList.setPadding(false);
        navList.setSpacing(false);

        // --- MENU ITEMS ---
        // 1. Pilotage
        navList.add(createNavItem("Tableau de bord", VaadinIcon.CHART, navigationManager::goToOrganizerDashboard));

        // 2. Gestion Cœur de métier
        navList.add(createNavItem("Mes Événements", VaadinIcon.LIST, navigationManager::goToMyEvents));

        // 3. Action Principale (Mise en avant)
        Button createBtn = createNavItem("Créer un événement", VaadinIcon.PLUS_CIRCLE, navigationManager::goToCreateEvent);
        createBtn.addClassName(LumoUtility.FontWeight.BOLD);
        createBtn.getStyle().set("color", "var(--lumo-primary-text-color)"); // Subtile mise en avant
        navList.add(createBtn);

        // 4. Suivi
        navList.add(createNavItem("Réservations & Participants", VaadinIcon.TICKET, navigationManager::goToOrganizerReservations));

        // Spacer
        VerticalLayout spacer = new VerticalLayout();
        spacer.setHeight("2rem");

        // --- FOOTER MENU ---
        H2 accountTitle = new H2("Compte");
        accountTitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.TextTransform.UPPERCASE
        );

        Button profileBtn = createNavItem("Mon Profil", VaadinIcon.USER_CARD, navigationManager::goToProfile);
        Button logoutBtn = createNavItem("Déconnexion", VaadinIcon.SIGN_OUT, securityService::logout);
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

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

    private Button createNavItem(String label, VaadinIcon icon, Runnable action) {
        Button btn = new Button(label, new Icon(icon));
        btn.addClickListener(e -> action.run());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.setWidthFull();
        btn.setHeight("3rem");
        btn.addClassNames(
                LumoUtility.JustifyContent.START,
                LumoUtility.Padding.Start.LARGE,
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.TextColor.SECONDARY
        );
        return btn;
    }
}