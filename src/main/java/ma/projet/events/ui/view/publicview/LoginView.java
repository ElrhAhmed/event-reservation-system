package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.ui.navigation.NavigationManager;

@Route(value = "login")
@PageTitle("Connexion | FESTIVENT")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    private final NavigationManager navigationManager;

    public LoginView(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;

        // 1. CONFIGURATION DE LA PAGE (Fond gris, Centrage total)
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName(LumoUtility.Background.CONTRAST_5); // Gris léger

        // 2. LA CARTE BLANCHE (Compacte)
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("450px"); // Largeur idéale pour un login
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(false); // On réduit l'espace entre les éléments pour la compacité

        // Style "Card" (Ombre, Arrondi, Blanc)
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.MEDIUM,
                LumoUtility.BorderRadius.LARGE
        );
        card.setAlignItems(Alignment.CENTER); // Centre le contenu dans la carte

        // --- CONTENU DE LA CARTE ---

        // A. Logo (Cliquable)
        H1 logo = new H1("FESTIVENT");
        logo.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.Bottom.NONE);
        logo.getStyle().set("cursor", "pointer");
        logo.addClickListener(e -> navigationManager.goToHome());

        // B. Titre
        H2 title = new H2("Se connecter");
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.SMALL, LumoUtility.Margin.Bottom.NONE);

        // C. Sous-titre
        Span subtitle = new Span("Accédez à votre espace membre");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL, LumoUtility.Margin.Bottom.MEDIUM);

        // D. Formulaire Vaadin (Configuré pour être compact)
        configureLoginForm();
        loginForm.getElement().getStyle().set("width", "100%");

        // E. Lien Inscription
        Button registerBtn = new Button("Pas encore de compte ? S'inscrire", e -> navigationManager.goToRegister());
        registerBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerBtn.addClassName(LumoUtility.Margin.Top.SMALL);

        // Assemblage
        card.add(logo, title, subtitle, loginForm, registerBtn);

        add(card);
    }

    private void configureLoginForm() {
        LoginI18n i18n = LoginI18n.createDefault();

        // Header (On le vide car on a mis notre propre Logo H1 au dessus)
        LoginI18n.Header i18nHeader = new LoginI18n.Header();
        i18nHeader.setTitle(null);
        i18nHeader.setDescription(null);
        i18n.setHeader(i18nHeader);

        // Formulaire
        LoginI18n.Form i18nForm = new LoginI18n.Form();
        i18nForm.setTitle(null); // Pas de titre interne
        i18nForm.setUsername("Email");
        i18nForm.setPassword("Mot de passe");
        i18nForm.setSubmit("Connexion");
        i18nForm.setForgotPassword("Mot de passe oublié ?");
        i18n.setForm(i18nForm);

        // Messages d'erreur
        LoginI18n.ErrorMessage i18nErrorMessage = new LoginI18n.ErrorMessage();
        i18nErrorMessage.setTitle("Échec de connexion");
        i18nErrorMessage.setMessage("Vérifiez vos identifiants.");
        i18n.setErrorMessage(i18nErrorMessage);

        loginForm.setI18n(i18n);
        loginForm.setAction("login");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}