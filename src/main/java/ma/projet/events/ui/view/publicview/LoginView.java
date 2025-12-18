package ma.projet.events.ui. view.publicview;

import com.vaadin.flow.component. UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin. flow.component.html. Anchor;
import com.vaadin. flow.component.html.H2;
import com.vaadin. flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin. flow.component.notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield. PasswordField;
import com. vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.projet.events.ui.layout.PublicLayout;

/**
 * Vue de connexion (Login)
 * Route : /login
 *
 * Phase 4 : Interface uniquement (pas d'authentification réelle)
 * Phase 10 : Intégration Spring Security
 */
@Route(value = "login", layout = PublicLayout.class)
@PageTitle("Connexion - Festivent")
public class LoginView extends VerticalLayout {

    private EmailField emailField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginView() {
        // Configuration de la vue
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "var(--festivent-bg)");

        // Conteneur du formulaire
        VerticalLayout formContainer = createFormContainer();

        add(formContainer);
    }

    /**
     * Crée le conteneur du formulaire de connexion
     */
    private VerticalLayout createFormContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidth("400px");
        container.setPadding(true);
        container.setSpacing(true);
        container.addClassName("festivent-card");

        // Titre
        H2 title = new H2("Connexion");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0 0 var(--lumo-space-m) 0")
                .set("text-align", "center");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Connectez-vous pour réserver vos événements");
        subtitle.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("text-align", "center")
                .set("margin", "0 0 var(--lumo-space-l) 0");

        // Champ Email
        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("votre@email.com");
        emailField.setClearButtonVisible(true);
        emailField.setRequiredIndicatorVisible(true);

        // Champ Mot de passe
        passwordField = new PasswordField("Mot de passe");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Votre mot de passe");
        passwordField.setRequiredIndicatorVisible(true);

        // Bouton de connexion
        loginButton = new Button("Se connecter");
        loginButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.setWidthFull();
        loginButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Action du bouton (temporaire pour Phase 4)
        loginButton.addClickListener(e -> handleLogin());

        // Enter sur les champs → déclenche la connexion
        emailField.addKeyPressListener(event -> {
            if (event. getKey().equals("Enter")) {
                loginButton.click();
            }
        });

        passwordField.addKeyPressListener(event -> {
            if (event.getKey().equals("Enter")) {
                loginButton.click();
            }
        });

        // Lien vers inscription
        // Lien vers inscription
        Paragraph registerLink = new Paragraph();
        registerLink.getStyle()
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-m)")
                .set("color", "var(--festivent-text-secondary)");

        // ✅ CORRECTION : Utiliser Span pour le texte
        Span registerText = new Span("Pas encore de compte ?  ");
        registerText.getStyle().set("color", "var(--festivent-text-secondary)");

        Anchor registerAnchor = new Anchor("register", "Créer un compte");
        registerAnchor.getStyle().set("color", "var(--festivent-primary)");

        registerLink.add(registerText, registerAnchor);  // ✅ Les deux sont des Component

        container.add(title, subtitle, emailField, passwordField, loginButton, registerLink);

        return container;
    }

    /**
     * Gère la tentative de connexion
     * Phase 4 : Validation basique uniquement
     * Phase 10 : Authentification réelle avec Spring Security
     */
    private void handleLogin() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        // Validation des champs
        if (email == null || email.isBlank()) {
            showError("Veuillez saisir votre email");
            emailField.focus();
            return;
        }

        if (password == null || password.isBlank()) {
            showError("Veuillez saisir votre mot de passe");
            passwordField.focus();
            return;
        }

        // TODO Phase 10 : Authentification réelle avec Spring Security
        // Pour l'instant, on simule une connexion réussie
        showSuccess("Connexion simulée (Phase 10 :  vraie authentification)");

        // Redirection vers le dashboard (sera implémenté en Phase 6-7)
        // UI.getCurrent().navigate("dashboard");
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification. Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String message) {
        Notification notification = Notification. show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant. LUMO_SUCCESS);
    }
}