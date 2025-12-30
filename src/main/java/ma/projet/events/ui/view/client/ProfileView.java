package ma.projet.events.ui.view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.common.ConfirmDialogUtil;
import ma.projet.events.ui.layout.UserLayout;

import java.time.format.DateTimeFormatter;

@Route(value = "profile", layout = UserLayout.class)
@PageTitle("Mon Profil | FESTIVENT")
public abstract class ProfileView extends VerticalLayout {


    private final UserService userService;
    private final SecurityService securityService;

    // Binder pour les infos personnelles
    private final Binder<User> binder = new BeanValidationBinder<>(User.class);
    private User currentUser;

    // Champs du formulaire
    private TextField nom;
    private TextField prenom;
    private EmailField email;
    private TextField telephone;

    // Champs mot de passe
    private PasswordField oldPassword;
    private PasswordField newPassword;
    private PasswordField confirmNewPassword;

    public ProfileView(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;

        setPadding(true);
        setSpacing(true);
        addClassName(LumoUtility.Background.BASE);
        // Centrer le contenu sur grand écran
        setMaxWidth("800px"); // Largeur un peu réduite pour faire plus "focus"
        addClassName(LumoUtility.Margin.Horizontal.AUTO);

        loadCurrentUser();

        if (currentUser == null) {
            add(new Span("Utilisateur non trouvé. Veuillez vous reconnecter."));
            return;
        }

        // Construction de l'UI (SANS les Stats)
        add(
                createHeader(),
                // createStatsSection() -> SUPPRIMÉ
                createPersonalInfoSection(),
                createSecuritySection(),
                createDangerZone()
        );
    }

    private void loadCurrentUser() {
        var userDetails = securityService.getAuthenticatedUser();
        if (userDetails != null) {
            currentUser = userService.getUserByEmail(userDetails.getUsername());
        }
    }

    /* =========================
       1. HEADER
       ========================= */
    private HorizontalLayout createHeader() {
        H2 title = new H2(currentUser.getPrenom() + " " + currentUser.getNom());
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Span memberSince = new Span("Membre depuis le " +
                currentUser.getDateInscription().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        memberSince.addClassName(LumoUtility.TextColor.SECONDARY);

        VerticalLayout textLayout = new VerticalLayout(title, memberSince);
        textLayout.setSpacing(false);
        textLayout.setPadding(false);

        // Avatar (Initiale)
        Div avatar = new Div();
        avatar.setText(currentUser.getPrenom().substring(0, 1).toUpperCase());
        avatar.addClassNames(
                LumoUtility.Background.PRIMARY,
                LumoUtility.TextColor.PRIMARY_CONTRAST,
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.FontSize.XXLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.BorderRadius.FULL
        );
        avatar.setWidth("64px");
        avatar.setHeight("64px");

        HorizontalLayout header = new HorizontalLayout(avatar, textLayout);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassName(LumoUtility.Margin.Bottom.LARGE);
        return header;
    }

    /* =========================
       2. INFO PERSONNELLES
       ========================= */
    private VerticalLayout createPersonalInfoSection() {
        H3 title = new H3("Informations personnelles");

        VerticalLayout card = createCard();

        nom = new TextField("Nom");
        prenom = new TextField("Prénom");
        email = new EmailField("Email");
        telephone = new TextField("Téléphone");

        // Configuration FormLayout responsive
        FormLayout formLayout = new FormLayout();
        formLayout.add(prenom, nom, email, telephone);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Binding
        binder.bindInstanceFields(this);
        binder.readBean(currentUser);

        Button saveBtn = new Button("Enregistrer les modifications");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> updateProfile());

        card.add(title, formLayout, saveBtn);
        return card;
    }

    private void updateProfile() {
        try {
            User updatedData = new User();
            binder.writeBean(updatedData);

            if (updatedData.getEmail() == null) updatedData.setEmail(currentUser.getEmail());

            User savedUser = userService.updateProfile(currentUser.getId(), updatedData, currentUser.getId());

            this.currentUser = savedUser;
            binder.readBean(currentUser);

            showSuccess("Profil mis à jour avec succès.");

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    /* =========================
       3. SÉCURITÉ (PASSWORD)
       ========================= */
    private VerticalLayout createSecuritySection() {
        H3 title = new H3("Sécurité");

        VerticalLayout card = createCard();

        oldPassword = new PasswordField("Ancien mot de passe");
        newPassword = new PasswordField("Nouveau mot de passe");
        newPassword.setHelperText("Min 8 caractères");
        confirmNewPassword = new PasswordField("Confirmer le mot de passe");

        FormLayout formLayout = new FormLayout();
        formLayout.add(oldPassword, newPassword, confirmNewPassword);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 3)
        );

        Button changePassBtn = new Button("Changer le mot de passe");
        changePassBtn.addClickListener(e -> changePassword());

        card.add(title, formLayout, changePassBtn);
        return card;
    }

    private void changePassword() {
        String oldP = oldPassword.getValue();
        String newP = newPassword.getValue();
        String confP = confirmNewPassword.getValue();

        if (oldP.isEmpty() || newP.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!newP.equals(confP)) {
            newPassword.setInvalid(true);
            confirmNewPassword.setInvalid(true);
            showError("Les nouveaux mots de passe ne correspondent pas.");
            return;
        }

        try {
            userService.changePassword(currentUser.getId(), oldP, newP, currentUser.getId());

            oldPassword.clear();
            newPassword.clear();
            confirmNewPassword.clear();

            showSuccess("Mot de passe modifié avec succès.");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /* =========================
       4. DANGER ZONE
       ========================= */
    private VerticalLayout createDangerZone() {
        VerticalLayout card = createCard();
        card.addClassName(LumoUtility.Border.ALL);
        card.getStyle().set("border-color", "var(--lumo-error-color)");
        card.getStyle().set("background-color", "var(--lumo-error-10pct)");

        H3 title = new H3("Zone de danger");
        title.addClassName(LumoUtility.TextColor.ERROR);

        Span warning = new Span("La désactivation de votre compte est irréversible. Vous ne pourrez plus accéder à vos réservations.");

        Button deactivateBtn = new Button("Désactiver mon compte");
        deactivateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        deactivateBtn.addClickListener(e -> {
            ConfirmDialogUtil.show(
                    "Désactiver le compte ?",
                    "Voulez-vous vraiment désactiver votre compte ? Cette action entraînera la perte de l'accès à vos données.",
                    this::handleDeactivation
            );
        });

        card.add(title, warning, deactivateBtn);
        return card;
    }

    private void handleDeactivation() {
        Notification.show("Demande envoyée au support.",
                        5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    /* =========================
       UTILITAIRES UI
       ========================= */
    private VerticalLayout createCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.LARGE,
                LumoUtility.Margin.Bottom.MEDIUM
        );
        return card;
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}