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

    private final TextField nom = new TextField("Nom");
    private final TextField prenom = new TextField("Prénom");
    private final EmailField email = new EmailField("Email");
    private final PasswordField password = new PasswordField("Mot de passe");
    private final PasswordField confirmPassword = new PasswordField("Confirmation");
    private final Checkbox organizerCheck = new Checkbox("Je suis organisateur");
    private final Button submitBtn = new Button("S'inscrire");
    private final Binder<User> binder = new BeanValidationBinder<>(User.class);

    public RegisterView(UserService userService, NavigationManager navigationManager) {
        this.userService = userService;
        this.navigationManager = navigationManager;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName(LumoUtility.Background.CONTRAST_5);
        // Réduction du padding global de la page
        setPadding(false);

        // Logo plus compact
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(
                LumoUtility.TextColor.PRIMARY,
                LumoUtility.FontSize.XXLARGE,
                LumoUtility.Margin.Bottom.SMALL // Marge réduite
        );
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> navigationManager.goToHome());

        VerticalLayout registerCard = createRegisterCard();

        Button backButton = new Button("Retour accueil", e -> navigationManager.goToHome());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        backButton.addClassName(LumoUtility.Margin.Top.SMALL); // Marge réduite

        add(logo, registerCard, backButton);

        configureBinder();
    }

    private VerticalLayout createRegisterCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("600px"); // Plus large pour accommoder les 2 colonnes

        // Espacement interne réduit pour la compacité
        card.setSpacing(false);
        card.setPadding(true);

        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.MEDIUM, // Ombre moins diffuse
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.LARGE // Changé de XLARGE à LARGE
        );

        // Header de carte compact
        H2 title = new H2("Créer un compte");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.NONE);

        Span subtitle = new Span("Rejoignez la communauté FESTIVENT");
        subtitle.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.SMALL,
                LumoUtility.Margin.Bottom.MEDIUM
        );

        // --- FORM LAYOUT 2 COLONNES ---
        FormLayout formLayout = new FormLayout();
        formLayout.add(prenom, nom, email, password, confirmPassword, organizerCheck);

        // Responsive : 1 colonne sur mobile, 2 colonnes sur desktop (>500px)
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // L'email et la checkbox prennent toute la largeur (2 colonnes)
        formLayout.setColspan(email, 2);
        formLayout.setColspan(organizerCheck, 2);

        // Ajustement des labels pour gagner de la place visuelle
        password.setHelperText(null); // On enlève le helper text qui prend de la place (validation gérée par binder)
        password.setPlaceholder("Min 8 caractères");

        // Bouton d'action avec marge au dessus
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.setWidthFull();
        submitBtn.addClassName(LumoUtility.Margin.Top.MEDIUM);
        submitBtn.addClickListener(e -> register());

        // Footer compact
        Span hasAccountText = new Span("Déjà inscrit ?");
        hasAccountText.addClassName(LumoUtility.TextColor.SECONDARY);
        hasAccountText.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Button loginLink = new Button("Se connecter", e -> navigationManager.goToLogin());
        loginLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        loginLink.addClassName(LumoUtility.FontWeight.BOLD);

        HorizontalLayout footer = new HorizontalLayout(hasAccountText, loginLink);
        footer.setAlignItems(Alignment.CENTER);
        footer.addClassName(LumoUtility.Margin.Top.SMALL);

        card.add(title, subtitle, formLayout, submitBtn, footer);
        return card;
    }

    private void configureBinder() {
        binder.bindInstanceFields(this);
    }

    private void register() {
        String pass = password.getValue();
        String confirm = confirmPassword.getValue();

        if (pass == null || confirm == null || !pass.equals(confirm)) {
            confirmPassword.setInvalid(true);
            confirmPassword.setErrorMessage("Les mots de passe ne correspondent pas");
            return;
        } else {
            confirmPassword.setInvalid(false);
        }

        User newUser = new User();
        if (binder.writeBeanIfValid(newUser)) {
            try {
                newUser.setRole(organizerCheck.getValue() ? Role.ORGANIZER : Role.CLIENT);
                userService.register(newUser);
                Notification.show("Compte créé ! Connectez-vous.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                navigationManager.goToLogin();
            } catch (Exception e) {
                Notification.show(e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
}