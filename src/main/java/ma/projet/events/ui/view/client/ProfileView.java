package ma.projet.events.ui. view.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin. flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com.vaadin.flow.component. notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component. orderedlayout. VerticalLayout;
import com.vaadin.flow.component. textfield.EmailField;
import com.vaadin.flow.component.textfield. PasswordField;
import com.vaadin. flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import ma.projet.events. entity.User;
import ma.projet.events.exception.BusinessException;
import ma.projet.events. exception.ConflictException;
import ma.projet. events.exception.UnauthorizedException;
import ma.projet. events.security.SecurityService;
import ma. projet.events.service.UserService;
import ma.projet. events.ui.layout.MainLayout;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Vue Profil utilisateur
 */
@Route(value = "client/profile", layout = MainLayout.class)
@PageTitle("Profile - EventReserve")
@PermitAll
public class ProfileView extends VerticalLayout {

    private final SecurityService securityService;
    private final UserService userService;
    private User currentUser;

    // Champs du formulaire
    private TextField firstNameField;
    private TextField lastNameField;
    private EmailField emailField;
    private TextField phoneField;

    // Mode édition
    private boolean isEditMode = false;
    private Button editButton;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProfileView(SecurityService securityService, UserService userService) {
        this.securityService = securityService;
        this.userService = userService;
        this.currentUser = securityService.getAuthenticatedUser();

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle()
                .set("background-color", "#f8fafc")
                .set("padding", "var(--festivent-space-xl)")
                .set("gap", "var(--festivent-space-lg)");

        add(
                createHeaderSection(),
                createPersonalInfoSection(),
                createAccountStatsSection(),
                createSecuritySection(),
                createDangerZoneSection()
        );
    }

    /**
     * Section header
     */
    private VerticalLayout createHeaderSection() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);

        H2 title = new H2("Profile");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span subtitle = new Span("Manage your account settings");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        header.add(title, subtitle);
        return header;
    }

    /**
     * Section Personal Information
     */
    private Div createPersonalInfoSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        // Header de la section
        HorizontalLayout sectionHeader = new HorizontalLayout();
        sectionHeader. setWidthFull();
        sectionHeader. setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        sectionHeader.setAlignItems(FlexComponent.Alignment. CENTER);
        sectionHeader. getStyle().set("margin-bottom", "var(--festivent-space-md)");

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection. setSpacing(false);

        H3 sectionTitle = new H3("Personal Information");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span sectionSubtitle = new Span("Update your personal details");
        sectionSubtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        titleSection.add(sectionTitle, sectionSubtitle);

        editButton = new Button("Edit");
        editButton. addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        editButton. getStyle()
                .set("border", "1px solid var(--festivent-secondary)")
                .set("border-radius", "var(--festivent-radius-md)");
        editButton.addClickListener(e -> toggleEditMode());

        sectionHeader.add(titleSection, editButton);

        // Formulaire
        Div formGrid = new Div();
        formGrid. getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "1fr 1fr")
                .set("gap", "var(--festivent-space-md)");

        firstNameField = new TextField("First Name");
        firstNameField. setValue(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        firstNameField.setReadOnly(true);
        firstNameField.setWidthFull();

        lastNameField = new TextField("Last Name");
        lastNameField.setValue(currentUser.getNom() != null ? currentUser.getNom() : "");
        lastNameField. setReadOnly(true);
        lastNameField.setWidthFull();

        emailField = new EmailField("Email");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setReadOnly(true);
        emailField.setWidthFull();
        emailField.getStyle().set("grid-column", "span 2");

        phoneField = new TextField("Phone");
        phoneField.setValue(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        phoneField.setReadOnly(true);
        phoneField.setWidthFull();
        phoneField.getStyle().set("grid-column", "span 2");

        formGrid.add(firstNameField, lastNameField, emailField, phoneField);

        card.add(sectionHeader, formGrid);
        return card;
    }

    /**
     * Toggle mode édition
     */
    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            // Passer en mode édition
            editButton.setText("Save");
            editButton. addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            editButton.getStyle().remove("border");

            firstNameField.setReadOnly(false);
            lastNameField. setReadOnly(false);
            emailField.setReadOnly(false);
            phoneField.setReadOnly(false);
        } else {
            // Sauvegarder les modifications
            saveProfile();
        }
    }

    /**
     * Sauvegarde le profil
     */
    private void saveProfile() {
        try {
            User updatedUser = new User();
            updatedUser.setPrenom(firstNameField.getValue());
            updatedUser.setNom(lastNameField. getValue());
            updatedUser.setEmail(emailField.getValue());
            updatedUser.setTelephone(phoneField. getValue());

            currentUser = userService. updateProfile(currentUser. getId(), updatedUser, currentUser.getId());

            // Remettre en mode lecture
            editButton.setText("Edit");
            editButton.removeThemeVariants(ButtonVariant. LUMO_PRIMARY);
            editButton. addThemeVariants(ButtonVariant. LUMO_TERTIARY);
            editButton.getStyle().set("border", "1px solid var(--festivent-secondary)");

            firstNameField.setReadOnly(true);
            lastNameField.setReadOnly(true);
            emailField. setReadOnly(true);
            phoneField.setReadOnly(true);

            Notification.show("Profile updated successfully", 3000, Notification.Position. TOP_CENTER)
                    .addThemeVariants(NotificationVariant. LUMO_SUCCESS);

        } catch (ConflictException | BusinessException e) {
            Notification.show(e.getMessage(), 4000, Notification. Position.TOP_CENTER)
                    . addThemeVariants(NotificationVariant.LUMO_ERROR);
            isEditMode = true; // Rester en mode édition
        }
    }

    /**
     * Section Account Statistics
     */
    private Div createAccountStatsSection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        H3 sectionTitle = new H3("Account Statistics");
        sectionTitle.getStyle()
                .set("margin", "0 0 var(--festivent-space-md) 0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        // Récupérer les statistiques
        Map<String, Object> stats = userService.getUserStatistics(currentUser. getId());

        // Stats row
        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow. setSpacing(true);
        statsRow.getStyle().set("gap", "var(--festivent-space-xl)");

        statsRow.add(
                createStatItem(VaadinIcon. CALENDAR, "Member Since",
                        currentUser.getDateInscription().format(DATE_FORMATTER)),
                createStatItem(VaadinIcon.USER, "Total Reservations",
                        String.valueOf(stats.get("totalReservations"))),
                createStatItem(VaadinIcon.EURO, "Total Spent",
                        String.format("€%.2f", (Double) stats.get("montantTotalDepense")))
        );

        card.add(sectionTitle, statsRow);
        return card;
    }

    /**
     * Crée un item de statistique
     */
    private HorizontalLayout createStatItem(VaadinIcon iconType, String label, String value) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment. CENTER);
        item.setSpacing(true);
        item.getStyle().set("gap", "var(--festivent-space-sm)");

        // Icône dans un cercle
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "40px")
                .set("height", "40px")
                .set("border-radius", "var(--festivent-radius-md)")
                .set("background-color", "var(--festivent-accent)");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", "var(--festivent-primary)");
        iconContainer.add(icon);

        // Texte
        VerticalLayout textSection = new VerticalLayout();
        textSection.setPadding(false);
        textSection.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-xs)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("color", "var(--festivent-secondary-text)")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "600");

        textSection.add(labelSpan, valueSpan);

        item.add(iconContainer, textSection);
        return item;
    }

    /**
     * Section Security
     */
    private Div createSecuritySection() {
        Div card = new Div();
        card.addClassName("festivent-card");
        card.getStyle().set("padding", "var(--festivent-space-lg)");

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection.setSpacing(false);
        titleSection.getStyle().set("margin-bottom", "var(--festivent-space-md)");

        H3 sectionTitle = new H3("Security");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span sectionSubtitle = new Span("Manage your password and account security");
        sectionSubtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        titleSection. add(sectionTitle, sectionSubtitle);

        // Password row
        HorizontalLayout passwordRow = new HorizontalLayout();
        passwordRow.setWidthFull();
        passwordRow.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        passwordRow.setAlignItems(FlexComponent.Alignment. CENTER);

        VerticalLayout passwordInfo = new VerticalLayout();
        passwordInfo.setPadding(false);
        passwordInfo.setSpacing(false);

        Span passwordLabel = new Span("Password");
        passwordLabel.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span passwordHint = new Span("Change your password");
        passwordHint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        passwordInfo.add(passwordLabel, passwordHint);

        Button changePasswordButton = new Button("Change Password");
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        changePasswordButton. getStyle()
                .set("border", "1px solid var(--festivent-secondary)")
                .set("border-radius", "var(--festivent-radius-md)");
        changePasswordButton.addClickListener(e -> showChangePasswordDialog());

        passwordRow.add(passwordInfo, changePasswordButton);

        card.add(titleSection, passwordRow);
        return card;
    }

    /**
     * Dialog pour changer le mot de passe
     */
    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Change Password");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        PasswordField currentPassword = new PasswordField("Current Password");
        currentPassword.setWidthFull();
        currentPassword.setRequired(true);

        PasswordField newPassword = new PasswordField("New Password");
        newPassword. setWidthFull();
        newPassword.setRequired(true);
        newPassword.setHelperText("Minimum 8 characters");

        PasswordField confirmPassword = new PasswordField("Confirm New Password");
        confirmPassword.setWidthFull();
        confirmPassword.setRequired(true);

        content.add(currentPassword, newPassword, confirmPassword);
        dialog.add(content);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveButton = new Button("Save Password", e -> {
            // Validation
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Notification.show("Please fill all fields", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant. LUMO_ERROR);
                return;
            }

            if (!newPassword. getValue().equals(confirmPassword.getValue())) {
                Notification. show("New passwords do not match", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                userService.changePassword(
                        currentUser. getId(),
                        currentPassword.getValue(),
                        newPassword.getValue(),
                        currentUser.getId()
                );

                Notification.show("Password changed successfully", 3000, Notification. Position.TOP_CENTER)
                        . addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                dialog.close();

            } catch (UnauthorizedException | BusinessException ex) {
                Notification.show(ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    /**
     * Section Danger Zone
     */
    private Div createDangerZoneSection() {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "#fef2f2")
                .set("border", "1px solid #fecaca")
                .set("border-radius", "var(--festivent-radius-lg)")
                .set("padding", "var(--festivent-space-lg)");

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection. setSpacing(false);
        titleSection.getStyle().set("margin-bottom", "var(--festivent-space-md)");

        H3 sectionTitle = new H3("Danger Zone");
        sectionTitle. getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("color", "#dc2626");

        Span sectionSubtitle = new Span("Irreversible account actions");
        sectionSubtitle. getStyle()
                .set("color", "#991b1b")
                .set("font-size", "var(--lumo-font-size-s)");

        titleSection.add(sectionTitle, sectionSubtitle);

        // Deactivate row
        HorizontalLayout deactivateRow = new HorizontalLayout();
        deactivateRow.setWidthFull();
        deactivateRow.setJustifyContentMode(FlexComponent.JustifyContentMode. BETWEEN);
        deactivateRow.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout deactivateInfo = new VerticalLayout();
        deactivateInfo.setPadding(false);
        deactivateInfo. setSpacing(false);

        Span deactivateLabel = new Span("Deactivate Account");
        deactivateLabel.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--festivent-secondary-text)");

        Span deactivateHint = new Span("This will disable your account and you won't be able to log in");
        deactivateHint. getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        deactivateInfo.add(deactivateLabel, deactivateHint);

        Button deactivateButton = new Button("Deactivate");
        deactivateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant. LUMO_ERROR);
        deactivateButton.addClickListener(e -> showDeactivateConfirmation());

        deactivateRow.add(deactivateInfo, deactivateButton);

        card.add(titleSection, deactivateRow);
        return card;
    }

    /**
     * Dialog de confirmation de désactivation
     */
    private void showDeactivateConfirmation() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Deactivate Account");
        dialog.setWidth("400px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span message = new Span("Are you sure you want to deactivate your account?");
        message.getStyle().set("color", "var(--festivent-secondary-text)");

        Span warning = new Span("This action will disable your account.  You will need to contact an administrator to reactivate it.");
        warning.getStyle()
                .set("color", "#dc2626")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "500");

        content.add(message, warning);
        dialog.add(content);

        Button cancelButton = new Button("Keep Account", e -> dialog. close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmButton = new Button("Deactivate Account", e -> {
            Notification.show("Account deactivation requires admin action.  Please contact support.",
                            4000, Notification.Position. TOP_CENTER)
                    .addThemeVariants(NotificationVariant. LUMO_CONTRAST);
            dialog. close();
        });
        confirmButton. addThemeVariants(ButtonVariant. LUMO_PRIMARY, ButtonVariant. LUMO_ERROR);

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }
}