package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.navigation.NavigationManager;

@Route(value = "register")
@PageTitle("Inscription | FESTIVENT")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserService userService;
    private final NavigationManager navigationManager;
    private final Binder<User> binder = new BeanValidationBinder<>(User.class);

    // Champs
    private final TextField nom = new TextField("Nom");
    private final TextField prenom = new TextField("Prénom");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Mot de passe");
    private final PasswordField confirmPassword = new PasswordField("Confirmation");
    private final Checkbox organizerCheck = new Checkbox("Je suis un organisateur");

    public RegisterView(UserService userService, NavigationManager navigationManager) {
        this.userService = userService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName(LumoUtility.Background.CONTRAST_5); // Fond gris très clair

        // Carte centrale
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("600px");
        card.setWidthFull();
        card.setPadding(true);
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.MEDIUM,
                LumoUtility.BorderRadius.LARGE
        );

        // Header
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontSize.XLARGE);
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> navigationManager.goToHome());

        H2 title = new H2("Créer un compte");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.NONE);

        Span subtitle = new Span("Rejoignez la communauté et réservez vos places.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL, LumoUtility.Margin.Bottom.MEDIUM);

        // Formulaire 2 colonnes
        FormLayout formLayout = new FormLayout();
        formLayout.add(prenom, nom, email, password, confirmPassword, organizerCheck);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(email, 2);
        formLayout.setColspan(organizerCheck, 2);

        // Actions
        Button submitBtn = new Button("S'inscrire");
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull();
        submitBtn.addClickListener(e -> register());

        Button loginLink = new Button("J'ai déjà un compte", e -> navigationManager.goToLogin());
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginLink.setWidthFull();

        // Assemblage
        card.add(logo, title, subtitle, formLayout, submitBtn, loginLink);
        add(card);

        configureBinder();
    }

    private void configureBinder() {
        binder.bindInstanceFields(this);
    }

    private void register() {
        // Validation manuelle du password match
        if (!password.getValue().equals(confirmPassword.getValue())) {
            confirmPassword.setInvalid(true);
            confirmPassword.setErrorMessage("Les mots de passe ne correspondent pas");
            return;
        }

        User newUser = new User();
        if (binder.writeBeanIfValid(newUser)) {
            try {
                newUser.setRole(organizerCheck.getValue() ? Role.ORGANIZER : Role.CLIENT);
                userService.register(newUser);
                Notification.show("Compte créé avec succès !", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                navigationManager.goToLogin();
            } catch (Exception e) {
                Notification.show("Erreur : " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
}