package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component. UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component. html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com. vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router. PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;

import java.util.Optional;

/**
 * Page d'accueil après connexion
 * Redirige vers /login si l'utilisateur n'est pas connecté
 */
@Route("")
@PageTitle("Accueil - Festivent")
@AnonymousAllowed  // ✅ Permet l'accès, mais on va rediriger si non connecté
public class HomeView extends VerticalLayout implements BeforeEnterObserver {

    private final SecurityService securityService;

    public HomeView(SecurityService securityService) {
        this.securityService = securityService;

        // Configuration de la vue
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
    }

    /**
     * ✅ Appelée AVANT l'affichage de la vue
     * Vérifie si l'utilisateur est connecté
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = securityService.getAuthenticatedUserOptional();

        if (user.isEmpty()) {
            // ❌ Pas connecté → Rediriger vers login
            event.forwardTo("login");
        } else {
            // ✅ Connecté → Créer le contenu
            createContent(user.get());
        }
    }

    /**
     * Crée le contenu de la page d'accueil
     */
    private void createContent(User currentUser) {
        // Container principal
        VerticalLayout container = new VerticalLayout();
        container.setWidth("600px");
        container.setPadding(true);
        container.setSpacing(true);
        container.setAlignItems(Alignment.CENTER);
        container.getStyle()
                .set("background-color", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 20px 60px rgba(0,0,0,0.3)");

        // Icône de succès
        Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
        successIcon. setSize("80px");
        successIcon.getStyle().set("color", "#10b981");

        // Titre
        H1 title = new H1("🎉 Connexion réussie !");
        title.getStyle()
                .set("color", "#1e293b")
                .set("margin", "var(--lumo-space-m) 0 var(--lumo-space-s) 0")
                .set("text-align", "center");

        // Message de bienvenue
        H3 welcome = new H3("Bienvenue, " + currentUser.getNomComplet() + " !");
        welcome.getStyle()
                .set("color", "#667eea")
                .set("margin", "0 0 var(--lumo-space-l) 0")
                .set("font-weight", "600");

        // Informations utilisateur
        Div userInfo = createUserInfoCard(currentUser);

        // Boutons d'action
        HorizontalLayout actions = createActionButtons();

        // Ajouter les composants
        container.add(successIcon, title, welcome, userInfo, actions);

        add(container);
    }

    /**
     * Crée la carte d'informations utilisateur
     */
    private Div createUserInfoCard(User user) {
        Div card = new Div();
        card.getStyle()
                .set("background", "linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)")
                .set("border-radius", "12px")
                .set("padding", "var(--lumo-space-l)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        H4 cardTitle = new H4("📋 Vos informations");
        cardTitle.getStyle()
                .set("margin", "0 0 var(--lumo-space-m) 0")
                .set("color", "#334155");

        // Email
        Div emailRow = createInfoRow(VaadinIcon.ENVELOPE, "Email", user.getEmail());

        // Rôle
        Div roleRow = createInfoRow(
                VaadinIcon.USER_STAR,
                "Rôle",
                user.getRole().getIcon() + " " + user.getRole().getLabel()
        );

        // Statut
        Div statusRow = createInfoRow(
                VaadinIcon.CHECK_CIRCLE_O,
                "Statut",
                user.isActif() ? "✅ Actif" : "❌ Inactif"
        );

        // Date d'inscription
        Div dateRow = createInfoRow(
                VaadinIcon.CALENDAR,
                "Membre depuis",
                user.getDateInscription().toLocalDate().toString()
        );

        card.add(cardTitle, emailRow, roleRow, statusRow, dateRow);
        return card;
    }

    /**
     * Crée une ligne d'information
     */
    private Div createInfoRow(VaadinIcon iconType, String label, String value) {
        Div row = new Div();
        row.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("padding", "var(--lumo-space-s)")
                .set("background-color", "white")
                .set("border-radius", "8px");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle()
                .set("color", "#667eea")
                .set("margin-right", "var(--lumo-space-s)");

        Span labelSpan = new Span(label + " : ");
        labelSpan.getStyle()
                .set("font-weight", "600")
                .set("color", "#475569")
                .set("margin-right", "var(--lumo-space-xs)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("color", "#64748b");

        row.add(icon, labelSpan, valueSpan);
        return row;
    }

    /**
     * Crée les boutons d'action
     */
    private HorizontalLayout createActionButtons() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "var(--lumo-space-l)");

        // Bouton Tableau de bord
        Button dashboardButton = new Button("Tableau de bord", VaadinIcon.DASHBOARD. create());
        dashboardButton. addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dashboardButton.addClickListener(e -> {
            Notification.show("🚧 Tableau de bord en cours de développement", 3000, Notification.Position.MIDDLE);
        });

        // Bouton Déconnexion
        Button logoutButton = new Button("Se déconnecter", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        logoutButton.addClickListener(e -> {
            securityService.logout();
            UI.getCurrent().getPage().setLocation("login");
        });

        actions.add(dashboardButton, logoutButton);
        return actions;
    }
}