package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin. flow.component.html. Anchor;
import com.vaadin. flow.component.html.H2;
import com.vaadin.flow.component.html. Paragraph;
import com.vaadin.flow.component.html. Span;
import com.vaadin.flow.component.notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield. PasswordField;
import com. vaadin.flow.component.textfield.TextField;
import com. vaadin.flow.router.PageTitle;
import com. vaadin.flow.router.Route;
import ma.projet.events.ui.layout.PublicLayout;

/**
 * Vue d'inscription (Register)
 * Route : /register
 *
 * Phase 4 : Interface uniquement (pas d'enregistrement réel)
 * Phase 10 : Intégration avec UserService + BCrypt
 */
@Route(value = "register", layout = PublicLayout.class)
@PageTitle("Inscription - Festivent")
public class RegisterView extends VerticalLayout {

    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField telephoneField;
    private Button registerButton;

    public RegisterView() {
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
     * Crée le conteneur du formulaire d'inscription
     */
    private VerticalLayout createFormContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidth("500px");
        container.setPadding(true);
        container. setSpacing(true);
        container.addClassName("festivent-card");

        // Titre
        H2 title = new H2("Créer un compte");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0 0 var(--lumo-space-m) 0")
                .set("text-align", "center");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Rejoignez Festivent et réservez vos événements préférés");
        subtitle.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("text-align", "center")
                .set("margin", "0 0 var(--lumo-space-l) 0");

        // Nom et Prénom (côte à côte)
        HorizontalLayout nameLayout = new HorizontalLayout();
        nameLayout.setWidthFull();
        nameLayout.setSpacing(true);

        nomField = new TextField("Nom");
        nomField.setWidthFull();
        nomField.setPlaceholder("Votre nom");
        nomField.setRequiredIndicatorVisible(true);

        prenomField = new TextField("Prénom");
        prenomField.setWidthFull();
        prenomField.setPlaceholder("Votre prénom");
        prenomField.setRequiredIndicatorVisible(true);

        nameLayout.add(nomField, prenomField);

        // Email
        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("votre@email.com");
        emailField.setClearButtonVisible(true);
        emailField.setRequiredIndicatorVisible(true);
        emailField.setHelperText("Utilisez un email valide pour confirmer votre inscription");

        // Téléphone (optionnel)
        telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("06 12 34 56 78");
        telephoneField. setClearButtonVisible(true);
        telephoneField.setHelperText("Optionnel");

        // Mot de passe
        passwordField = new PasswordField("Mot de passe");
        passwordField. setWidthFull();
        passwordField.setPlaceholder("Minimum 8 caractères");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setHelperText("Minimum 8 caractères");

        // Confirmation mot de passe
        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPlaceholder("Confirmez votre mot de passe");
        confirmPasswordField.setRequiredIndicatorVisible(true);

        // Bouton d'inscription
        registerButton = new Button("S'inscrire");
        registerButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        registerButton.setWidthFull();
        registerButton. getStyle().set("margin-top", "var(--lumo-space-m)");

        registerButton.addClickListener(e -> handleRegister());

        // Enter sur le dernier champ → déclenche l'inscription
        confirmPasswordField.addKeyPressListener(event -> {
            if (event.getKey().equals("Enter")) {
                registerButton.click();
            }
        });

        // Lien vers connexion
        Paragraph loginLink = new Paragraph();
        loginLink.getStyle()
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-m)")
                .set("color", "var(--festivent-text-secondary)");

        Span loginText = new Span("Déjà inscrit ?  ");
        loginText.getStyle().set("color", "var(--festivent-text-secondary)");

        Anchor loginAnchor = new Anchor("login", "Se connecter");
        loginAnchor.getStyle().set("color", "var(--festivent-primary)");

        loginLink.add(loginText, loginAnchor);

        container.add(
                title,
                subtitle,
                nameLayout,
                emailField,
                telephoneField,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginLink
        );

        return container;
    }

    /**
     * Gère la tentative d'inscription
     * Phase 4 : Validation basique uniquement
     * Phase 10 : Enregistrement réel avec UserService + BCrypt
     */
    private void handleRegister() {
        String nom = nomField.getValue();
        String prenom = prenomField. getValue();
        String email = emailField.getValue();
        String telephone = telephoneField.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validation des champs obligatoires
        if (nom == null || nom.isBlank()) {
            showError("Veuillez saisir votre nom");
            nomField.focus();
            return;
        }

        if (prenom == null || prenom.isBlank()) {
            showError("Veuillez saisir votre prénom");
            prenomField.focus();
            return;
        }

        if (email == null || email.isBlank()) {
            showError("Veuillez saisir votre email");
            emailField. focus();
            return;
        }

        // Validation basique de l'email
        if (!email.contains("@") || !email.contains(".")) {
            showError("Format d'email invalide");
            emailField.focus();
            return;
        }

        if (password == null || password.isBlank()) {
            showError("Veuillez saisir un mot de passe");
            passwordField.focus();
            return;
        }

        // Validation taille mot de passe
        if (password.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            passwordField.focus();
            return;
        }

        if (confirmPassword == null || confirmPassword. isBlank()) {
            showError("Veuillez confirmer votre mot de passe");
            confirmPasswordField.focus();
            return;
        }

        // Vérification correspondance mots de passe
        if (! password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            confirmPasswordField.focus();
            confirmPasswordField.clear();
            return;
        }

        // TODO Phase 10 : Enregistrement réel avec UserService
        // User newUser = new User();
        // newUser.setNom(nom);
        // newUser.setPrenom(prenom);
        // newUser.setEmail(email);
        // newUser.setTelephone(telephone);
        // newUser.setPassword(passwordEncoder.encode(password));
        // newUser.setRole(Role.CLIENT);
        // userService.save(newUser);

        // Pour l'instant, simulation
        showSuccess("Inscription simulée (Phase 10 :  vrai enregistrement)");

        // Redirection vers login après inscription
        UI.getCurrent().navigate("login");
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        Notification notification = Notification. show(message, 3000, Notification.Position. MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant. LUMO_SUCCESS);
    }
}