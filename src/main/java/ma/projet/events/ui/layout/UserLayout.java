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

public class UserLayout extends AppLayout implements RouterLayout {

    private final NavigationManager navigationManager;
    private final SecurityService securityService;

    public UserLayout(NavigationManager navigationManager, SecurityService securityService) {
        this.navigationManager = navigationManager;
        this.securityService = securityService;

        createHeader();
        createDrawer();

        // Style global du layout pour éviter les débordements
        addClassName(LumoUtility.Height.FULL);
    }

    private void createHeader() {
        // 1. Toggle Menu (Mobile)
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassName(LumoUtility.Margin.End.MEDIUM);

        // 2. Logo
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE,
                LumoUtility.TextColor.PRIMARY
        );
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> navigationManager.goToHome());

        // 3. User Info (Droite)
        UserDetails user = securityService.getAuthenticatedUser();
        String username = (user != null) ? user.getUsername() : "Client";

        Avatar avatar = new Avatar(username);
        avatar.addClassName(LumoUtility.Margin.End.SMALL);

        Span nameSpan = new Span(username); // Idéalement, récupérer le nom complet via un service plus tard
        nameSpan.addClassNames(LumoUtility.FontWeight.MEDIUM, LumoUtility.FontSize.SMALL);
        // Cacher le nom sur mobile pour gagner de la place


        HorizontalLayout userArea = new HorizontalLayout(nameSpan, avatar);
        userArea.setAlignItems(FlexComponent.Alignment.CENTER);

        // Assemblage Header
        HorizontalLayout header = new HorizontalLayout(toggle, logo, userArea);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(logo); // Le logo pousse la zone user à droite
        header.addClassNames(
                LumoUtility.Padding.Vertical.SMALL,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Background.BASE
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        // Titre du Menu
        H2 menuTitle = new H2("Menu Client");
        menuTitle.addClassNames(
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.TextColor.SECONDARY
        );

        // Construction des items de navigation
        // On utilise des boutons pour forcer le passage par NavigationManager

        VerticalLayout navList = new VerticalLayout();
        navList.setPadding(false);
        navList.setSpacing(false); // On gère l'espacement via CSS si besoin

        navList.add(createNavItem("Dashboard", VaadinIcon.DASHBOARD, navigationManager::goToClientDashboard));
        navList.add(createNavItem("Mes Réservations", VaadinIcon.TICKET, navigationManager::goToClientReservations));
        navList.add(createNavItem("Mon Profil", VaadinIcon.USER, navigationManager::goToProfile));

        // Séparateur visuel (Spacer)
        VerticalLayout spacer = new VerticalLayout();
        spacer.setHeight("2rem");

        // Bouton Logout distinct
        Button logoutBtn = createNavItem("Déconnexion", VaadinIcon.SIGN_OUT, securityService::logout);
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR); // Rouge pour l'action destructive
        logoutBtn.removeClassNames(LumoUtility.TextColor.SECONDARY); // On garde la couleur d'erreur

        // Assemblage Drawer
        VerticalLayout drawerContent = new VerticalLayout(menuTitle, navList, spacer, logoutBtn);
        drawerContent.setSizeFull();
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);

        // Scroller pour gérer les petits écrans
        Scroller scroller = new Scroller(drawerContent);
        addToDrawer(scroller);
    }

    // Helper pour créer des boutons qui ressemblent à des liens de menu
    private Button createNavItem(String label, VaadinIcon icon, Runnable action) {
        Button btn = new Button(label, new Icon(icon));
        btn.addClickListener(e -> action.run());

        // Styles pour ressembler à un item de menu latéral
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.setWidthFull();
        btn.setHeight("3rem"); // Hauteur confortable pour le tactile
        btn.addClassNames(
                LumoUtility.JustifyContent.START, // Alignement gauche
                LumoUtility.Padding.Start.LARGE,
                LumoUtility.FontSize.MEDIUM,
                LumoUtility.TextColor.SECONDARY
        );

        return btn;
    }
}