package ma.projet.events.ui.layout;

import com.vaadin.flow.component. applayout.AppLayout;
import com.vaadin.flow. component.button.Button;
import com.vaadin.flow.component.button. ButtonVariant;
import com. vaadin.flow.component.html.Div;
import com. vaadin.flow.component.html.Footer;
import com.vaadin. flow.component.html.H1;
import com.vaadin. flow.component.html. Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;

/**
 * Public application layout.
 * Used by all public views: HomeView, EventListView, EventDetailView, LoginView, RegisterView.
 *
 * Features:
 * - Fixed header with navigation
 * - Logo and brand name
 * - Login/Register buttons
 * - Simple footer
 */
public class PublicLayout extends AppLayout {

    public PublicLayout() {
        createHeader();
        createFooter();
    }

    /**
     * Creates the fixed header with logo, navigation, and auth buttons.
     */
    private void createHeader() {
        // Logo section (left)
        Icon calendarIcon = VaadinIcon.CALENDAR. create();
        calendarIcon.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("width", "24px")
                .set("height", "24px");

        H1 brandName = new H1("EventReserve");
        brandName.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        HorizontalLayout logoSection = new HorizontalLayout(calendarIcon, brandName);
        logoSection.setAlignItems(FlexComponent. Alignment.CENTER);
        logoSection.setSpacing(true);
        logoSection.getStyle().set("gap", "var(--festivent-space-xs)");

        // Navigation links (center)
        RouterLink homeLink = new RouterLink("Home", null); // Route will be set by views
        homeLink.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("text-decoration", "none")
                .set("font-weight", "500")
                .set("padding", "var(--festivent-space-xs) var(--festivent-space-sm)");

        RouterLink eventsLink = new RouterLink("Events", null);
        eventsLink.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("text-decoration", "none")
                .set("font-weight", "500")
                .set("padding", "var(--festivent-space-xs) var(--festivent-space-sm)");

        HorizontalLayout navLinks = new HorizontalLayout(homeLink, eventsLink);
        navLinks.setAlignItems(FlexComponent.Alignment.CENTER);
        navLinks.setSpacing(true);
        navLinks.getStyle().set("gap", "var(--festivent-space-md)");

        // Auth buttons (right)
        Button loginButton = new Button("Login");
        loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginButton.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-weight", "500");
        loginButton.addClickListener(e -> loginButton.getUI().ifPresent(ui -> ui.navigate("login")));

        Button registerButton = new Button("Register");
        registerButton. addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e -> registerButton. getUI().ifPresent(ui -> ui.navigate("register")));

        HorizontalLayout authButtons = new HorizontalLayout(loginButton, registerButton);
        authButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        authButtons.setSpacing(true);
        authButtons.getStyle().set("gap", "var(--festivent-space-sm)");

        // Header layout
        HorizontalLayout header = new HorizontalLayout(logoSection, navLinks, authButtons);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "white")
                .set("box-shadow", "var(--festivent-shadow-sm)")
                .set("padding", "var(--festivent-space-md) var(--festivent-space-xl)");

        // Expand center section to push auth buttons to the right
        header.expand(navLinks);

        addToNavbar(header);
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
        footerBrand.setAlignItems(FlexComponent. Alignment.CENTER);
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

        // Add footer to the layout
        // Note: AppLayout doesn't have a direct footer slot, so we add it to the content area
        // Views using this layout should be aware of the footer presence
        setContent(footer);
    }
}