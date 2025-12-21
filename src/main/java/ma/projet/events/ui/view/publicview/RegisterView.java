package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component. UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin. flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield. PasswordField;
import com. vaadin.flow.component.textfield.TextField;
import com. vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin. flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow. router.RouterLink;
import com. vaadin.flow.server.auth.AnonymousAllowed;
import ma.projet.events.entity.User;
import ma.projet.events.service.UserService;

/**
 * Page d'inscription
 * Route : /register
 *
 * Permet à un nouvel utilisateur de créer un compte
 * Validation complète des champs
 */
@Route("register")
@PageTitle("Inscription - Festivent")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final Binder<User> binder = new Binder<>(User.class);

    // Champs du formulaire
    private final TextField nomField = new TextField("Nom");
    private final TextField prenomField = new TextField("Prénom");
    private final EmailField emailField = new EmailField("Email");
    private final TextField telephoneField = new TextField("Téléphone (optionnel)");
    private final PasswordField passwordField = new PasswordField("Mot de passe");
    private final PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
    private final Button registerButton = new Button("S'inscrire");

    public RegisterView(UserService userService) {
        this.userService = userService;

        // Configuration de la vue
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode. CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "var(--lumo-space-m)");

        // Créer le contenu
        createContent();

        // Configurer la validation
        configureValidation();
    }

    /**
     * Crée le contenu de la page d'inscription
     */
    private void createContent() {
        // Container principal
        VerticalLayout container = new VerticalLayout();
        container.setWidth("500px");
        container.setPadding(true);
        container. setSpacing(true);
        container.getStyle()
                .set("background-color", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 10px 40px rgba(0,0,0,0.2)");

        // Logo et titre
        H1 logo = new H1("🎉 Festivent");
        logo.getStyle()
                .set("color", "#667eea")
                .set("text-align", "center")
                .set("margin", "0 0 var(--lumo-space-s) 0")
                .set("font-weight", "700");

        Paragraph subtitle = new Paragraph("Créez votre compte gratuitement");
        subtitle.getStyle()
                .set("color", "#64748b")
                .set("text-align", "center")
                .set("margin", "0 0 var(--lumo-space-l) 0");

        // Formulaire
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Configuration des champs
        nomField.setRequired(true);
        nomField.setPlaceholder("Votre nom");

        prenomField.setRequired(true);
        prenomField. setPlaceholder("Votre prénom");

        emailField.setRequired(true);
        emailField.setPlaceholder("votre@email.com");

        telephoneField.setPlaceholder("+212 6XX XX XX XX");

        passwordField.setRequired(true);
        passwordField.setHelperText("Minimum 8 caractères");

        confirmPasswordField.setRequired(true);

        // Ajouter les champs au formulaire
        formLayout.add(nomField, prenomField);
        formLayout.add(emailField, 2);
        formLayout.add(telephoneField, 2);
        formLayout.add(passwordField, confirmPasswordField);

        // Bouton d'inscription
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        registerButton.getStyle()
                .set("width", "100%")
                .set("margin-top", "var(--lumo-space-m)")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("border", "none");

        registerButton.addClickListener(e -> handleRegister());

        // Lien vers la connexion
        Div loginSection = new Div();
        loginSection.getStyle()
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-m)");

        Span loginText = new Span("Déjà un compte ? ");
        loginText.getStyle().set("color", "#64748b");

        RouterLink loginLink = new RouterLink("Se connecter", ma.projet.events.ui.view.publicview.LoginView.class);
        loginLink.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "600")
                .set("text-decoration", "none");

        loginSection.add(loginText, loginLink);

        // Ajouter les composants
        container.add(logo, subtitle, formLayout, registerButton, loginSection);

        add(container);
    }

    /**
     * Configure la validation des champs
     */
    private void configureValidation() {
        // Validation du nom
        binder.forField(nomField)
                .asRequired("Le nom est obligatoire")
                .withValidator(nom -> nom.length() >= 2, "Le nom doit contenir au moins 2 caractères")
                .bind(User::getNom, User::setNom);

        // Validation du prénom
        binder.forField(prenomField)
                .asRequired("Le prénom est obligatoire")
                .withValidator(prenom -> prenom.length() >= 2, "Le prénom doit contenir au moins 2 caractères")
                .bind(User::getPrenom, User::setPrenom);

        // Validation de l'email
        binder.forField(emailField)
                .asRequired("L'email est obligatoire")
                .withValidator(new EmailValidator("Email invalide"))
                .bind(User::getEmail, User::setEmail);

        // Validation du téléphone (optionnel)
        binder.forField(telephoneField)
                .bind(User::getTelephone, User::setTelephone);

        // Validation du mot de passe
        binder.forField(passwordField)
                .asRequired("Le mot de passe est obligatoire")
                .withValidator(
                        password -> password.length() >= 8,
                        "Le mot de passe doit contenir au moins 8 caractères"
                )
                .bind(User::getPassword, User:: setPassword);

        // Validation de la confirmation
        binder.forField(confirmPasswordField)
                .asRequired("Veuillez confirmer le mot de passe")
                .withValidator(
                        confirm -> confirm.equals(passwordField.getValue()),
                        "Les mots de passe ne correspondent pas"
                )
                .bind(
                        user -> passwordField.getValue(), // getter (pas utilisé)
                        (user, value) -> {} // setter vide (pas besoin de stocker)
                );
    }

    /**
     * Gère la soumission du formulaire d'inscription
     */
    private void handleRegister() {
        try {
            // Créer un nouvel utilisateur
            User user = new User();

            // Valider et remplir l'utilisateur
            binder. writeBean(user);

            // Enregistrer l'utilisateur (le mot de passe sera hashé automatiquement)
            userService. register(user);

            // Notification de succès
            Notification notification = Notification.show(
                    "✅ Compte créé avec succès !  Vous pouvez maintenant vous connecter.",
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Rediriger vers la page de connexion après 1 seconde
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => window.location.href = 'login', 1000)"
            );

        } catch (ValidationException e) {
            // Erreur de validation
            Notification notification = Notification.show(
                    "❌ Veuillez corriger les erreurs dans le formulaire",
                    3000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        } catch (Exception e) {
            // Autre erreur (email déjà utilisé, etc.)
            Notification notification = Notification.show(
                    "❌ " + e.getMessage(),
                    4000,
                    Notification. Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant. LUMO_ERROR);
        }
    }
}