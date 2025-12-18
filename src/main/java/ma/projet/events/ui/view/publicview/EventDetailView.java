package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component. button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin. flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.exception.ResourceNotFoundException;
import ma.projet. events.service.EventService;
import ma.projet.events.ui.component.StatusBadge;
import ma. projet.events.ui.layout.PublicLayout;

import java.time.format.DateTimeFormatter;
import java.util. Locale;

/**
 * Vue détails d'un événement
 * Route : /event/{id}
 *
 * Utilise HasUrlParameter pour récupérer l'ID depuis l'URL
 */
@Route(value = "event", layout = PublicLayout. class)
@PageTitle("Détails de l'événement - Festivent")
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);

    private final EventService eventService;
    private Event event;

    public EventDetailView(EventService eventService) {
        this.eventService = eventService;

        // Configuration de la vue
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        getStyle().set("background-color", "var(--festivent-bg)");
    }

    /**
     * Méthode appelée automatiquement par Vaadin pour récupérer le paramètre d'URL
     * Exemple : /event/5 → eventId = 5
     */
    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        try {
            // Charger l'événement depuis le service
            event = eventService. getEventById(eventId);

            // Mettre à jour le titre de la page
            UI.getCurrent().getPage().setTitle(event.getTitre() + " - Festivent");

            // Créer le contenu
            createContent();

        } catch (ResourceNotFoundException e) {
            // Événement introuvable
            showErrorAndRedirect("Événement introuvable");
        } catch (Exception e) {
            // Autre erreur
            showErrorAndRedirect("Erreur lors du chargement de l'événement");
            e.printStackTrace();
        }
    }

    /**
     * Crée tout le contenu de la page
     */
    private void createContent() {
        // Image header (plein écran)
        Div imageHeader = createImageHeader();

        // Contenu principal (centré, max-width)
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setWidthFull();
        mainContent.setMaxWidth("1200px");
        mainContent.getStyle().set("margin", "0 auto");
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        // Breadcrumb (Accueil / Événements / Titre)
        HorizontalLayout breadcrumb = createBreadcrumb();

        // Header (Titre + Badges)
        VerticalLayout header = createHeader();

        // Infos principales (Date, Lieu, Organisateur)
        HorizontalLayout mainInfo = createMainInfo();

        // Description
        VerticalLayout descriptionSection = createDescriptionSection();

        // Section réservation (Prix, Places, Bouton)
        VerticalLayout reservationSection = createReservationSection();

        mainContent.add(breadcrumb, header, mainInfo, descriptionSection, reservationSection);

        add(imageHeader, mainContent);
    }

    /**
     * Crée l'image header (ou dégradé si pas d'image)
     */
    private Div createImageHeader() {
        Div imageHeader = new Div();
        imageHeader.setWidthFull();
        imageHeader.setHeight("400px");

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            // Image réelle
            imageHeader.getStyle()
                    .set("background-image", "url('" + event.getImageUrl() + "')")
                    .set("background-size", "cover")
                    .set("background-position", "center")
                    .set("position", "relative");
        } else {
            // Dégradé avec icône de catégorie
            imageHeader.getStyle()
                    .set("background", "linear-gradient(135deg, var(--festivent-primary) 0%, var(--festivent-accent) 100%)")
                    .set("display", "flex")
                    . set("align-items", "center")
                    .set("justify-content", "center");

            Span icon = new Span(event.getCategorie().getIcon());
            icon.getStyle()
                    .set("font-size", "120px")
                    .set("color", "rgba(255, 255, 255, 0.9)");
            imageHeader.add(icon);
        }

        return imageHeader;
    }

    /**
     * Crée le breadcrumb de navigation
     */
    private HorizontalLayout createBreadcrumb() {
        HorizontalLayout breadcrumb = new HorizontalLayout();
        breadcrumb. setSpacing(true);
        breadcrumb.setAlignItems(Alignment.CENTER);

        // Accueil
        Anchor homeLink = new Anchor("", "Accueil");
        homeLink.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("text-decoration", "none")
                .set("font-size", "var(--lumo-font-size-s)");

        Span separator1 = new Span("/");
        separator1.getStyle().set("color", "var(--festivent-text-tertiary)");

        // Événements
        Anchor eventsLink = new Anchor("events", "Événements");
        eventsLink.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("text-decoration", "none")
                .set("font-size", "var(--lumo-font-size-s)");

        Span separator2 = new Span("/");
        separator2.getStyle().set("color", "var(--festivent-text-tertiary)");

        // Titre actuel (tronqué si trop long)
        String truncatedTitle = event.getTitre().length() > 50
                ? event.getTitre().substring(0, 50) + "..."
                : event.getTitre();

        Span currentPage = new Span(truncatedTitle);
        currentPage.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("font-size", "var(--lumo-font-size-s)");

        breadcrumb.add(homeLink, separator1, eventsLink, separator2, currentPage);
        return breadcrumb;
    }

    /**
     * Crée le header avec titre et badges
     */
    private VerticalLayout createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setSpacing(true);
        header.setPadding(false);

        // Badges (Catégorie + Statut)
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);

        // Badge catégorie
        Span categoryBadge = new Span(event.getCategorie().getIcon() + " " + event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("background-color", "var(--festivent-bg-card)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("font-weight", "600")
                .set("border", "1px solid var(--festivent-border)");

        // Badge statut
        StatusBadge statusBadge = new StatusBadge(event. getStatut());

        badges.add(categoryBadge, statusBadge);

        // Titre
        H1 title = new H1(event.getTitre());
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "var(--lumo-space-s) 0 0 0")
                .set("font-size", "var(--lumo-font-size-xxxl)");

        header.add(badges, title);
        return header;
    }

    /**
     * Crée la section des informations principales
     */
    private HorizontalLayout createMainInfo() {
        HorizontalLayout mainInfo = new HorizontalLayout();
        mainInfo.setWidthFull();
        mainInfo.setSpacing(true);
        mainInfo.getStyle().set("flex-wrap", "wrap");

        // Date
        VerticalLayout dateInfo = createInfoBlock(
                VaadinIcon.CALENDAR. create(),
                "Date et heure",
                event.getDateDebut().format(DATE_FORMATTER)
        );

        // Lieu
        VerticalLayout lieuInfo = createInfoBlock(
                VaadinIcon. LOCATION_ARROW.create(),
                "Lieu",
                event.getLieu() + ", " + event.getVille()
        );

        // Organisateur
        VerticalLayout orgInfo = createInfoBlock(
                VaadinIcon.USER.create(),
                "Organisé par",
                event.getOrganisateur().getNomComplet()
        );

        mainInfo.add(dateInfo, lieuInfo, orgInfo);
        return mainInfo;
    }

    /**
     * Crée un bloc d'information (icône + label + valeur)
     */
    private VerticalLayout createInfoBlock(Icon icon, String label, String value) {
        VerticalLayout block = new VerticalLayout();
        block.setSpacing(false);
        block.setPadding(true);
        block.addClassName("festivent-card");
        block.setWidth("auto");
        block.getStyle().set("flex", "1");

        icon.setSize("28px");
        icon.getStyle().set("color", "var(--festivent-primary)");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--festivent-text-tertiary)")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px")
                .set("margin-top", "var(--lumo-space-xs)");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-m)")
                .set("color", "var(--festivent-text-primary)")
                .set("font-weight", "600")
                .set("margin-top", "var(--lumo-space-xs)");

        block.add(icon, labelSpan, valueSpan);
        return block;
    }

    /**
     * Crée la section description
     */
    private VerticalLayout createDescriptionSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.addClassName("festivent-card");

        H3 title = new H3("À propos de cet événement");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        Paragraph description = new Paragraph(event.getDescription());
        description. getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("line-height", "1.6")
                .set("margin", "var(--lumo-space-m) 0 0 0");

        section.add(title, description);
        return section;
    }

    /**
     * Crée la section réservation (prix, places, bouton)
     */
    private VerticalLayout createReservationSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.addClassName("festivent-card");
        section.getStyle().set("background-color", "var(--festivent-bg)");

        H3 title = new H3("Réservation");
        title.getStyle()
                .set("color", "var(--festivent-primary)")
                .set("margin", "0");

        // Prix
        HorizontalLayout priceLayout = new HorizontalLayout();
        priceLayout.setAlignItems(Alignment. BASELINE);
        priceLayout. setSpacing(true);

        Span priceLabel = new Span("Prix par personne :");
        priceLabel.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("font-size", "var(--lumo-font-size-m)");

        Span price = new Span(String.format("%.2f DH", event.getPrixUnitaire()));
        price.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", "var(--festivent-accent)")
                .set("font-weight", "700");

        priceLayout.add(priceLabel, price);

        // Places disponibles
        int placesDisponibles = eventService.calculateAvailablePlaces(event. getId());
        int capaciteMax = event.getCapaciteMax();
        double fillRate = 1.0 - ((double) placesDisponibles / capaciteMax);

        // Info places
        HorizontalLayout placesLayout = new HorizontalLayout();
        placesLayout.setWidthFull();
        placesLayout.setJustifyContentMode(JustifyContentMode. BETWEEN);
        placesLayout. setAlignItems(Alignment.CENTER);

        Span placesInfo = new Span(placesDisponibles + " places disponibles");
        placesInfo.getStyle()
                .set("color", "var(--festivent-text-secondary)")
                .set("font-weight", "500");

        Span capacityInfo = new Span(capaciteMax + " places au total");
        capacityInfo. getStyle()
                .set("color", "var(--festivent-text-tertiary)")
                .set("font-size", "var(--lumo-font-size-s)");

        placesLayout. add(placesInfo, capacityInfo);

        // ProgressBar
        ProgressBar progressBar = new ProgressBar(0, 1, fillRate);
        progressBar.setWidthFull();

        // Couleur selon taux de remplissage
        if (fillRate >= 0.9) {
            progressBar.getStyle().set("--vaadin-progress-bar-color", "var(--festivent-error)");
        } else if (fillRate >= 0.7) {
            progressBar.getStyle().set("--vaadin-progress-bar-color", "var(--festivent-warning)");
        } else {
            progressBar.getStyle().set("--vaadin-progress-bar-color", "var(--festivent-success)");
        }

        // Bouton réserver
        Button reserverButton = new Button("Réserver maintenant", VaadinIcon.TICKET.create());
        reserverButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserverButton.setWidthFull();
        reserverButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Logique du bouton
        if (placesDisponibles == 0) {
            // Complet
            reserverButton.setEnabled(false);
            reserverButton.setText("Complet");
            reserverButton.setIcon(VaadinIcon.BAN.create());
        } else if (event.getStatut() != EventStatus.PUBLIE) {
            // Non publié
            reserverButton. setEnabled(false);
            reserverButton.setText("Non disponible");
        } else if (! event.isReservable()) {
            // Non réservable (date passée, annulé, etc.)
            reserverButton.setEnabled(false);
            reserverButton.setText("Non disponible");
        } else {
            // Réservable → Action à définir plus tard (Phase 5)
            reserverButton.addClickListener(e -> {
                Notification.show(
                        "Veuillez vous connecter pour réserver",
                        3000,
                        Notification. Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_PRIMARY);

                // TODO Phase 5 : Rediriger vers LoginView ou ReservationFormView
                // UI.getCurrent().navigate("login");
            });
        }

        section.add(title, priceLayout, placesLayout, progressBar, reserverButton);
        return section;
    }

    /**
     * Affiche une erreur et redirige vers l'accueil
     */
    private void showErrorAndRedirect(String message) {
        Notification.show(message, 3000, Notification.Position. MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);

        // Redirection après 1 seconde
        UI.getCurrent().getPage().executeJs(
                "setTimeout(() => window.location.href = '/', 1000)"
        );
    }
}