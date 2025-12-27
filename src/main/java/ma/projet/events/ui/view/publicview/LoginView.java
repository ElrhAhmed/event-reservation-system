package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ma.projet.events.security.SecurityService;
import ma.projet.events.ui.layout.AuthLayout;

@Route(value = "login", layout = AuthLayout.class)
@PageTitle("Login - EventReserve")
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver {

    private final SecurityService securityService;
    private final LoginForm loginForm;

    public LoginView(SecurityService securityService) {
        this.securityService = securityService;

        /* =========================
           PAGE CONTAINER
           ========================= */
        addClassName("festivent-auth-page");
        getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center");

        /* =========================
           CARD
           ========================= */
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle()
                .set("max-width", "420px")
                .set("width", "100%")
                .set("padding", "2.5rem 2.25rem");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.setWidthFull();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.getStyle().set("gap", "1.4rem");

        /* =========================
           HEADER
           ========================= */
        Icon icon = VaadinIcon.CALENDAR.create();
        icon.setSize("42px");
        icon.getStyle().set("color", "var(--festivent-primary)");

        H2 title = new H2("Welcome Back");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Sign in to your account to continue");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "1rem");

        /* =========================
           LOGIN FORM (SPRING SECURITY)
           ========================= */
        loginForm = new LoginForm();
        loginForm.setAction("login"); // 🔑 Spring Security endpoint
        loginForm.setForgotPasswordButtonVisible(false);

        /* =========================
           REGISTER LINK
           ========================= */
        Span registerPrompt = new Span();
        registerPrompt.getStyle()
                .set("margin-top", "1rem")
                .set("font-size", "1rem");

        Anchor registerLink = new Anchor("register", "Create an account");
        registerLink.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("font-weight", "600")
                .set("text-decoration", "none");

        registerPrompt.add(new Text("Don’t have an account? "), registerLink);

        /* =========================
           ASSEMBLY
           ========================= */
        content.add(
                icon,
                title,
                subtitle,
                loginForm,
                registerPrompt
        );

        card.add(content);
        add(card);
    }

    /**
     * 🔁 Redirection intelligente
     * - utilisateur déjà connecté → dashboard
     * - erreur login → message d'erreur
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {

        // 🔁 Déjà connecté ? → redirection par rôle
        securityService.getAuthenticatedUserOptional().ifPresent(user -> {
            if (user.isAdmin()) {
                event.forwardTo("admin/dashboard");
            } else if (user.isOrganizer()) {
                event.forwardTo("organizer/dashboard");
            } else {
                event.forwardTo("client/dashboard");
            }
        });

        // ❌ Erreur login ?
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
