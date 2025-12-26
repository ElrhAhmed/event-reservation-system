package ma.projet.events.ui.layout;

import com.vaadin. flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow. component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow. component.html.Div;
import com.vaadin.flow. component.html.Footer;
import com.vaadin. flow.component.html.H1;
import com.vaadin. flow.component.html.Span;
import com.vaadin. flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;

import java.util.Optional;

/**
 * Public application layout.
 * Used by all public views:  HomeView, EventListView, EventDetailView, LoginView, RegisterView.
 *
 * Features:
 * - Fixed/sticky header with navigation
 * - Logo and brand name
 * - Dynamic authentication state:
 *   - Not authenticated: Login/Register buttons
 *   - Authenticated:  User name with icon + Logout button
 * - Simple footer
 *
 * Authentication integration:
 * - Uses SecurityService to detect authentication state
 * - Automatically updates UI based on user session
 */
public class PublicLayout extends AppLayout {

    private final SecurityService securityService;

    public PublicLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createFooter();
    }

    /**
     * Creates the fixed header with logo, navigation, and auth buttons.
     */
    private void createHeader() {
        // Logo section (left)
        Icon calendarIcon = VaadinIcon.CALENDAR.create();
        calendarIcon.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("width", "24px")
                .set("height", "24px");

        H1 brandName = new H1("EventReserve");
        brandName.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("cursor", "pointer");

        // Make brand name clickable (navigate to home)
        brandName.addClickListener(e ->
                brandName.getUI().ifPresent(ui -> ui.navigate(""))
        );

        HorizontalLayout logoSection = new HorizontalLayout(calendarIcon, brandName);
        logoSection. setAlignItems(FlexComponent.Alignment.CENTER);
        logoSection.setSpacing(true);
        logoSection.getStyle().set("gap", "var(--festivent-space-xs)");

        // Navigation links (center) - Using Buttons for flexibility
        Button homeLink = new Button("Home");
        homeLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        homeLink.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "500");
        homeLink.addClickListener(e ->
                homeLink. getUI().ifPresent(ui -> ui.navigate(""))
        );

        Button eventsLink = new Button("Events");
        eventsLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eventsLink.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "500");
        eventsLink.addClickListener(e ->
                eventsLink.getUI().ifPresent(ui -> ui.navigate("events"))
        );

        HorizontalLayout navLinks = new HorizontalLayout(homeLink, eventsLink);
        navLinks.setAlignItems(FlexComponent.Alignment.CENTER);
        navLinks.setSpacing(true);
        navLinks.getStyle().set("gap", "var(--festivent-space-md)");

        // Auth section (right) - Dynamic based on authentication state
        HorizontalLayout authSection = createAuthSection();

        // Header layout
        HorizontalLayout header = new HorizontalLayout(logoSection, navLinks, authSection);
        header.setWidthFull();
        header.setAlignItems(FlexComponent. Alignment.CENTER);
        header.setSpacing(true);
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "white")
                .set("box-shadow", "var(--festivent-shadow-sm)")
                .set("padding", "var(--festivent-space-md) var(--festivent-space-xl)")
                .set("position", "sticky")
                .set("top", "0")
                .set("z-index", "1000");

        // Expand center section to push auth section to the right
        header.expand(navLinks);

        addToNavbar(header);
    }

    /**
     * Creates the authentication section (right side of header).
     * Displays different content based on authentication state.
     */
    private HorizontalLayout createAuthSection() {
        HorizontalLayout authSection = new HorizontalLayout();
        authSection.setAlignItems(FlexComponent. Alignment.CENTER);
        authSection.setSpacing(true);
        authSection.getStyle().set("gap", "var(--festivent-space-sm)");

        // Check if user is authenticated
        Optional<User> userOptional = securityService.getAuthenticatedUserOptional();

        if (userOptional.isPresent()) {
            // User is authenticated - Show user info
            User user = userOptional.get();
            authSection.add(createAuthenticatedUserSection(user));
        } else {
            // User is NOT authenticated - Show Login/Register buttons
            authSection.add(createUnauthenticatedSection());
        }

        return authSection;
    }

    /**
     * Creates the section for authenticated users (user name + icon + logout).
     */
    private HorizontalLayout createAuthenticatedUserSection(User user) {
        HorizontalLayout authSection = new HorizontalLayout();
        authSection.setAlignItems(FlexComponent.Alignment.CENTER);
        authSection. setSpacing(true);
        authSection.getStyle().set("gap", "var(--festivent-space-sm)");

        // User info section (clickable)
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setAlignItems(FlexComponent. Alignment.CENTER);
        userSection.setSpacing(true);
        userSection.getStyle()
                .set("gap", "var(--festivent-space-xs)")
                .set("padding", "var(--festivent-space-xs) var(--festivent-space-sm)")
                .set("border-radius", "var(--festivent-radius-md)")
                .set("background-color", "var(--festivent-accent)")
                .set("cursor", "pointer")
                .set("transition", "all var(--festivent-transition-fast)");

        // Hover effect
        userSection.getElement().addEventListener("mouseenter", e -> {
            userSection.getStyle().set("background-color", "var(--festivent-primary)");
        });
        userSection.getElement().addEventListener("mouseleave", e -> {
            userSection.getStyle().set("background-color", "var(--festivent-accent)");
        });

        // User icon
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("18px");
        userIcon.getStyle().set("color", "var(--festivent-accent-text)");

        // User name
        String displayName = user.getPrenom() + " " + user.getNom();
        Span userName = new Span(displayName);
        userName.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-accent-text)");

        userSection. add(userIcon, userName);

        // Click to navigate to dashboard (based on role)
        userSection.addClickListener(e -> {
            if (user.isAdmin()) {
                userSection.getUI().ifPresent(ui -> ui.navigate("admin/dashboard"));
            } else if (user. isOrganizer()) {
                userSection.getUI().ifPresent(ui -> ui.navigate("organizer/dashboard"));
            } else {
                userSection.getUI().ifPresent(ui -> ui.navigate("client/dashboard"));
            }
        });

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.setIcon(VaadinIcon. SIGN_OUT.create());
        logoutButton.getStyle()
                .set("color", "var(--festivent-error)")
                .set("font-size", "var(--lumo-font-size-s)");
        logoutButton.addClickListener(e -> {
            securityService.logout();
            UI.getCurrent().getPage().reload();
        });

        authSection.add(userSection, logoutButton);
        return authSection;
    }

    /**
     * Creates the section for unauthenticated users (Login/Register buttons).
     */
    private HorizontalLayout createUnauthenticatedSection() {
        HorizontalLayout unauthSection = new HorizontalLayout();
        unauthSection.setAlignItems(FlexComponent.Alignment.CENTER);
        unauthSection.setSpacing(true);
        unauthSection.getStyle().set("gap", "var(--festivent-space-sm)");

        // Login button (secondary/tertiary style)
        Button loginButton = new Button("Login");
        loginButton.addThemeVariants(ButtonVariant. LUMO_TERTIARY);
        loginButton.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "500");
        loginButton.addClickListener(e ->
                loginButton.getUI().ifPresent(ui -> ui.navigate("login"))
        );

        // Register button (primary style)
        Button registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e ->
                registerButton.getUI().ifPresent(ui -> ui.navigate("register"))
        );

        unauthSection.add(loginButton, registerButton);
        return unauthSection;
    }

    /**
     * Creates the simple footer with copyright information.
     */
    private void createFooter() {
        // Footer icon + brand
        Icon footerIcon = VaadinIcon.CALENDAR.create();
        footerIcon.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("width", "20px")
                .set("height", "20px");

        Span brandName = new Span("EventReserve");
        brandName.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)")
                .set("margin-left", "var(--festivent-space-xs)");

        HorizontalLayout footerBrand = new HorizontalLayout(footerIcon, brandName);
        footerBrand. setAlignItems(FlexComponent.Alignment.CENTER);
        footerBrand.setSpacing(false);

        // Copyright text
        Span copyright = new Span("© 2025 EventReserve. All rights reserved.");
        copyright.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-top", "var(--festivent-space-xs)");

        // Footer container
        Div footerContent = new Div(footerBrand, copyright);
        footerContent.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "var(--festivent-space-xs)");

        Footer footer = new Footer(footerContent);
        footer.getStyle()
                .set("padding", "var(--festivent-space-xl)")
                .set("background-color", "white")
                .set("border-top", "1px solid var(--festivent-secondary)")
                .set("text-align", "center")
                .set("margin-top", "auto");

        // Note: Footer rendering handled by views individually if needed
    }
}