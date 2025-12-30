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

public class AdminLayout extends AppLayout implements RouterLayout {

    private final NavigationManager navigationManager;
    private final SecurityService securityService;

    public AdminLayout(NavigationManager navigationManager, SecurityService securityService) {
        this.navigationManager = navigationManager;
        this.securityService = securityService;

        createHeader();
        createDrawer();

        addClassName(LumoUtility.Height.FULL);
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName(LumoUtility.Margin.End.MEDIUM);

        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Badge ADMIN
        Span badge = new Span("ADMINISTRATION");
        badge.getElement().getThemeList().add("badge error");
        badge.addClassName(LumoUtility.Margin.Start.SMALL);
        badge.addClassName(LumoUtility.FontSize.XSMALL);

        HorizontalLayout branding = new HorizontalLayout(logo, badge);
        branding.setAlignItems(FlexComponent.Alignment.CENTER);
        branding.getStyle().set("cursor", "pointer");
        branding.addClickListener(e -> navigationManager.goToAdminDashboard());

        // User Info
        UserDetails user = securityService.getAuthenticatedUser();
        String username = (user != null) ? user.getUsername() : "Admin";

        Avatar avatar = new Avatar(username);
        avatar.addThemeVariants(com.vaadin.flow.component.avatar.AvatarVariant.LUMO_XSMALL);

        HorizontalLayout userArea = new HorizontalLayout(new Span(username), avatar);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);


        // ✅ AJOUT : Rendre la zone avatar cliquable vers le profil
        userArea.getStyle().set("cursor", "pointer");
        userArea.addClickListener(e -> navigationManager.goToProfile());

        HorizontalLayout header = new HorizontalLayout(toggle, branding, userArea);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(branding);
        header.addClassNames(
                LumoUtility.Padding.Vertical.XSMALL,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Background.BASE
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        // Section Principale
        H2 menuTitle = new H2("Gouvernance");
        menuTitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.Padding.Top.MEDIUM,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.TextTransform.UPPERCASE,
                LumoUtility.FontWeight.BOLD
        );

        VerticalLayout navList = new VerticalLayout();
        navList.setPadding(false);
        navList.setSpacing(false);

        // --- MENU ITEMS ---
        navList.add(createNavItem("Vue Globale", VaadinIcon.DASHBOARD, navigationManager::goToAdminDashboard));
        navList.add(createNavItem("Utilisateurs", VaadinIcon.USERS, navigationManager::goToAdminUsers));
        navList.add(createNavItem("Événements", VaadinIcon.ARCHIVE, navigationManager::goToAdminEvents));
        navList.add(createNavItem("Réservations", VaadinIcon.TICKET, navigationManager::goToAdminReservations));

        // Spacer pour pousser le footer en bas
        VerticalLayout spacer = new VerticalLayout();
        spacer.setHeight("2rem");

        // ✅ AJOUT : Section Compte (Pour la cohérence avec User/Organizer Layouts)
        H2 accountTitle = new H2("Compte");
        accountTitle.addClassNames(
                LumoUtility.FontSize.XSMALL,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.TextColor.TERTIARY,
                LumoUtility.TextTransform.UPPERCASE
        );

        // ✅ AJOUT : Bouton Mon Profil
        Button profileBtn = createNavItem("Mon Profil", VaadinIcon.USER, navigationManager::goToProfile);

        // Bouton Déconnexion
        Button logoutBtn = createNavItem("Déconnexion", VaadinIcon.POWER_OFF, securityService::logout);
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        VerticalLayout drawerContent = new VerticalLayout(
                menuTitle, navList,
                spacer,
                accountTitle, profileBtn, logoutBtn // Ajout des éléments du footer
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
        btn.setHeight("2.5rem");
        btn.addClassNames(
                LumoUtility.JustifyContent.START,
                LumoUtility.Padding.Start.LARGE,
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );
        return btn;
    }
}