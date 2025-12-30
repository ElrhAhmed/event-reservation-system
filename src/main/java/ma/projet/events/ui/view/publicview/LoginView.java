package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.ui.navigation.NavigationManager;

// MODIFICATION : On retire "layout = PublicLayout.class" pour être autonome
@Route(value = "login")
@PageTitle("Connexion | FESTIVENT")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final NavigationManager navigationManager;

    public LoginView(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;

        // Configuration Pleine Page
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Fond gris clair élégant
        addClassName(LumoUtility.Background.CONTRAST_5);

        // 1. Logo "FESTIVENT" (Cliquable pour retour accueil)
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(
                LumoUtility.TextColor.PRIMARY,
                LumoUtility.Margin.Bottom.MEDIUM
        );
        logo.addClickListener(e -> navigationManager.goToHome());

        // 2. La Carte de Login
        VerticalLayout loginCard = createLoginCard();

        // 3. Lien retour discret (Sécurité UX)
        Button backButton = new Button("Retour à l'accueil", e -> navigationManager.goToHome());
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.addClassName(LumoUtility.Margin.Top.MEDIUM);

        add(logo, loginCard, backButton);
    }

    private VerticalLayout createLoginCard() {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("100%");
        card.setMaxWidth("450px"); // Un peu plus large pour respirer
        card.setSpacing(true);
        card.setPadding(true);

        // Style de la carte : Fond blanc, Ombre portée, Bords arrondis
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.LARGE, // Ombre plus marquée pour effet "pop"
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.XLARGE
        );
        card.setAlignItems(Alignment.CENTER);

        // En-tête Interne
        H2 title = new H2("Bon retour !");
        title.addClassNames(LumoUtility.Margin.Bottom.XSMALL);

        Span subtitle = new Span("Entrez vos identifiants pour accéder à votre espace.");
        subtitle.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextAlignment.CENTER
        );

        // Formulaire
        configureLoginForm();

        // Footer de carte
        Span noAccountText = new Span("Pas encore membre ?");
        noAccountText.addClassName(LumoUtility.TextColor.SECONDARY);

        Button registerLink = new Button("Créer un compte", e -> navigationManager.goToRegister());
        registerLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        registerLink.addClassName(LumoUtility.FontWeight.BOLD);

        HorizontalLayout footer = new HorizontalLayout(noAccountText, registerLink);
        footer.setAlignItems(Alignment.CENTER);
        footer.addClassName(LumoUtility.Margin.Top.SMALL);

        card.add(title, subtitle, loginForm, footer);

        return card;
    }

    private void configureLoginForm() {
        LoginI18n i18n = LoginI18n.createDefault();

        // On enlève le header interne du composant car on a fait le nôtre au-dessus
        LoginI18n.Header i18nHeader = new LoginI18n.Header();
        i18nHeader.setTitle(null);
        i18n.setHeader(i18nHeader);

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle(null);
        i18nForm.setUsername("Email");
        i18nForm.setPassword("Mot de passe");
        i18nForm.setSubmit("Se connecter");
        i18nForm.setForgotPassword("Mot de passe oublié ?");
        i18n.setForm(i18nForm);

        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();
        i18nErrorMessage.setTitle("Erreur d'authentification");
        i18nErrorMessage.setMessage("Vérifiez votre email et votre mot de passe.");
        i18n.setErrorMessage(i18nErrorMessage);

        loginForm.setI18n(i18n);
        loginForm.setAction("login");

        // Style pour supprimer le padding interne inutile du composant natif
        loginForm.addClassName(LumoUtility.Padding.NONE);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}