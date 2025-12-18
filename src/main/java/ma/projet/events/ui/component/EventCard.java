package ma.projet.events.ui.component;

import com.vaadin.flow. component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com. vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin. flow.component.orderedlayout.VerticalLayout;
import ma.projet.events.entity.Event;

import java.time.format.DateTimeFormatter;

/**
 * Card pour afficher un événement de manière attractive
 * Utilisée dans :  HomeView, EventListView, recherche
 *
 * Affiche :  Image, titre, date, lieu, prix, catégorie, statut
 */
public class EventCard extends VerticalLayout {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy à HH:mm");

    private final Event event;

    public EventCard(Event event) {
        this.event = event;

        addClassName("festivent-card");
        setSpacing(false);
        setPadding(false);
        setWidth("320px");

        // Effet hover
        getStyle()
                .set("cursor", "pointer")
                .set("transition", "var(--festivent-transition)");

        createContent();
    }

    private void createContent() {
        // Image de l'événement (placeholder si pas d'image)
        Div imageContainer = createImageContainer();

        // Badge catégorie + statut
        HorizontalLayout badges = createBadges();

        // Titre
        H3 title = new H3(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("color", "var(--festivent-text-primary)")
                .set("padding", "var(--lumo-space-m) var(--lumo-space-m) 0");

        // Infos :  date, lieu, prix
        VerticalLayout infos = createInfoSection();

        add(imageContainer, badges, title, infos);
    }

    /**
     * Crée le conteneur d'image
     */
    private Div createImageContainer() {
        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.setHeight("180px");

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            // Image réelle
            imageContainer.getStyle()
                    .set("background-image", "url('" + event.getImageUrl() + "')")
                    .set("background-size", "cover")
                    .set("background-position", "center");
        } else {
            // Placeholder avec dégradé
            imageContainer.getStyle()
                    .set("background", "linear-gradient(135deg, var(--festivent-primary) 0%, var(--festivent-accent) 100%)")
                    .set("display", "flex")
                    . set("align-items", "center")
                    .set("justify-content", "center");

            Span placeholder = new Span(event.getCategorie().getIcon());
            placeholder.getStyle()
                    .set("font-size", "48px")
                    .set("color", "white");
            imageContainer.add(placeholder);
        }

        imageContainer.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l) var(--lumo-border-radius-l) 0 0");

        return imageContainer;
    }

    /**
     * Crée les badges (catégorie + statut)
     */
    private HorizontalLayout createBadges() {
        HorizontalLayout badges = new HorizontalLayout();
        badges.setWidthFull();
        badges.setJustifyContentMode(JustifyContentMode. BETWEEN);
        badges.setPadding(true);
        badges.getStyle().set("margin-top", "-30px").set("position", "relative");

        // Badge catégorie
        Span categoryBadge = new Span(event.getCategorie().getIcon() + " " + event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("background-color", "white")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "600")
                .set("box-shadow", "var(--festivent-shadow-md)");

        // Badge statut
        StatusBadge statusBadge = new StatusBadge(event. getStatut());

        badges.add(categoryBadge, statusBadge);
        return badges;
    }

    /**
     * Crée la section informations (date, lieu, prix)
     */
    private VerticalLayout createInfoSection() {
        VerticalLayout infos = new VerticalLayout();
        infos.setSpacing(true);
        infos.setPadding(true);

        // Date
        HorizontalLayout dateLayout = createInfoRow(
                VaadinIcon.CALENDAR. create(),
                event.getDateDebut().format(DATE_FORMATTER)
        );

        // Lieu
        HorizontalLayout lieuLayout = createInfoRow(
                VaadinIcon. LOCATION_ARROW.create(),
                event.getLieu() + " - " + event.getVille()
        );

        // Prix
        HorizontalLayout prixLayout = createInfoRow(
                VaadinIcon. MONEY. create(),
                event.getPrixUnitaire() + " DH"
        );
        prixLayout.getStyle().set("color", "var(--festivent-accent)").set("font-weight", "600");

        infos.add(dateLayout, lieuLayout, prixLayout);
        return infos;
    }

    /**
     * Crée une ligne d'info (icône + texte)
     */
    private HorizontalLayout createInfoRow(com.vaadin.flow.component. icon.Icon icon, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(Alignment.CENTER);

        icon.setSize("16px");
        icon.getStyle().set("color", "var(--festivent-text-secondary)");

        Span textSpan = new Span(text);
        textSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)");

        row.add(icon, textSpan);
        return row;
    }

    /**
     * Récupère l'événement associé
     */
    public Event getEvent() {
        return event;
    }
}