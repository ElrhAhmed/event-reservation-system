package ma.projet.events.ui.view.publicview;

import com.vaadin.flow. component.html.*;
import com.vaadin.flow.component.login.LoginForm;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com. vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router. RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;


/**
 * Page de connexion
 * Route : /login
 *
 * Accessible sans authentification (@AnonymousAllowed)
 * Gère l'affichage des erreurs de connexion
 */
@Route("login")
@PageTitle("Connexion - Festivent")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        // Configuration de la vue
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("padding", "var(--lumo-space-m)");

        // Créer le contenu
        createContent();
    }

    /**
     * Crée le contenu de la page de connexion
     */
    private void createContent() {
        // Container principal
        VerticalLayout container = new VerticalLayout();
        container.setWidth("400px");
        container.setPadding(true);
        container.setSpacing(true);
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

        Paragraph subtitle = new Paragraph("Connectez-vous à votre compte");
        subtitle.getStyle()
                .set("color", "#64748b")
                .set("text-align", "center")
                .set("margin", "0 0 var(--lumo-space-l) 0");

        // Configuration du formulaire de connexion
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        // Traduction en français
        loginForm.getElement().setAttribute("no-autofocus", "");
        loginForm.getElement().executeJs(
                "this.i18n = {" +
                        "  form: {" +
                        "    title: ''," +
                        "    username: 'Email'," +
                        "    password: 'Mot de passe'," +
                        "    submit: 'Se connecter'," +
                        "    forgotPassword: 'Mot de passe oublié ?'" +
                        "  }," +
                        "  errorMessage: {" +
                        "    title: 'Erreur de connexion'," +
                        "    message: 'Email ou mot de passe incorrect'," +
                        "    username: 'Email requis'," +
                        "    password: 'Mot de passe requis'" +
                        "  }" +
                        "}"
        );

        // Lien vers l'inscription
        Div registerSection = new Div();
        registerSection.getStyle()
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-m)");

        Span registerText = new Span("Pas encore de compte ?  ");
        registerText.getStyle().set("color", "#64748b");

        RouterLink registerLink = new RouterLink("S'inscrire", ma.projet.events.ui.view.publicview.RegisterView.class);
        registerLink.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "600")
                .set("text-decoration", "none");

        registerSection.add(registerText, registerLink);

        // Informations de test (à supprimer en production)
        Div testInfo = createTestInfoBox();

        // Ajouter les composants
        container.add(logo, subtitle, loginForm, registerSection);

        add(container, testInfo);
    }

    /**
     * Crée un encadré avec les comptes de test
     * ⚠️ À SUPPRIMER EN PRODUCTION
     */
    private Div createTestInfoBox() {
        Div testBox = new Div();
        testBox.setWidth("400px");
        testBox.getStyle()
                .set("background-color", "rgba(255, 255, 255, 0.95)")
                .set("border-radius", "8px")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-top", "var(--lumo-space-m)")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

        H4 title = new H4("🧪 Comptes de test");
        title.getStyle()
                .set("margin", "0 0 var(--lumo-space-s) 0")
                .set("color", "#334155");

        Paragraph info = new Paragraph();
        info.getElement().setProperty("innerHTML",
                "<strong>Admin :</strong> admin@festivent. com / admin123<br>" +
                        "<strong>Organisateur :</strong> sarah.martin@festivent.com / sarah123<br>" +
                        "<strong>Client :</strong> ahmed.benali@festivent.com / ahmed123"
        );
        info.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "#64748b")
                .set("margin", "0")
                .set("line-height", "1.8");

        testBox.add(title, info);
        return testBox;
    }

    /**
     * Appelée avant l'entrée dans la vue
     * Gère l'affichage de l'erreur de connexion
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Afficher l'erreur si les identifiants sont incorrects
        if (event. getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}