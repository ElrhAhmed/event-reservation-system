package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

public class EventCard extends Div {

    private Runnable onView;

    public EventCard(Event event) {
        // APPLICATION DU STYLE CSS PERSONNALISÉ
        addClassName("event-card");
        setWidth("320px");
        setMaxWidth("100%");

        /* ===== 1. IMAGE ===== */
        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.setHeight("180px");
        imageContainer.addClassName(LumoUtility.Position.RELATIVE);

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            Image image = new Image(event.getImageUrl(), event.getTitre());
            image.addClassName("event-card-image");
            imageContainer.add(image);
        } else {
            // Placeholder élégant
            Div placeholder = new Div();
            placeholder.addClassName("event-card-placeholder");

            Icon icon = VaadinIcon.PICTURE.create();
            icon.setSize("48px");
            icon.addClassName(LumoUtility.TextColor.DISABLED);
            placeholder.add(icon);
            imageContainer.add(placeholder);
        }

        /* ===== 2. CONTENU ===== */
        VerticalLayout content = new VerticalLayout();
        content.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        content.setSpacing(false);

        // -- TOP ROW : Catégorie en Badge (MODIFICATION ICI) --
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        // On aligne le badge tout à droite
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Récupération de la catégorie
        EventCategory cat = event.getCategorie();

        // Création du badge avec l'icône de la catégorie et une couleur spécifique
        // On utilise cat.getIcon() + cat.getLabel() pour un rendu sympa (ex: 🎵 Concert)
        StatusBadge categoryBadge = new StatusBadge(
                cat.getIcon() + " " + cat.getLabel(),
                getCategoryColor(cat)
        );
        // On réduit un peu la taille de la police pour que ça rentre bien
        categoryBadge.getElement().getStyle().set("font-size", "11px");

        topRow.add(categoryBadge);

        // -- Titre --
        H3 title = new H3(event.getTitre());
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD, LumoUtility.Margin.Vertical.XSMALL);
        title.getStyle().set("display", "-webkit-box");
        title.getStyle().set("-webkit-line-clamp", "2");
        title.getStyle().set("-webkit-box-orient", "vertical");
        title.getStyle().set("overflow", "hidden");
        title.setHeight("3.6em");

        // -- Infos (Date & Lieu) --
        VerticalLayout metaInfo = new VerticalLayout();
        metaInfo.setPadding(false);
        metaInfo.setSpacing(false);

        Icon dateIcon = VaadinIcon.CALENDAR_CLOCK.create();
        dateIcon.setSize("14px");
        Span date = new Span(dateIcon, new Span(" " + DateFormatter.format(event.getDateDebut())));
        date.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

        Icon cityIcon = VaadinIcon.MAP_MARKER.create();
        cityIcon.setSize("14px");
        Span city = new Span(cityIcon, new Span(" " + event.getVille()));
        city.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER);

        metaInfo.add(date, city);

        // -- Prix et Organisateur --
        HorizontalLayout bottomRow = new HorizontalLayout();
        bottomRow.setWidthFull();
        bottomRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottomRow.setAlignItems(FlexComponent.Alignment.END);
        bottomRow.addClassName(LumoUtility.Margin.Top.SMALL);

        Span price = new Span(PriceFormatter.format(event.getPrixUnitaire()));
        price.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.EXTRABOLD, LumoUtility.TextColor.PRIMARY);

        Span organizer = new Span("Par " + event.getOrganisateur().getNom());
        organizer.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.TERTIARY);
        organizer.getStyle().set("max-width", "100px");
        organizer.getStyle().set("overflow", "hidden");
        organizer.getStyle().set("text-overflow", "ellipsis");
        organizer.getStyle().set("white-space", "nowrap");

        bottomRow.add(organizer, price);

        content.add(topRow, title, metaInfo, bottomRow);

        /* ===== 3. ACTION ===== */
        Button actionBtn = new Button("Réserver / Détails");
        actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionBtn.setWidthFull();
        actionBtn.addClassName(LumoUtility.BorderRadius.MEDIUM);
        actionBtn.addClickListener(e -> {
            if (onView != null) onView.run();
        });

        Div footer = new Div(actionBtn);
        footer.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Bottom.MEDIUM);

        add(imageContainer, content, footer);
    }

    public void setOnView(Runnable onView) {
        this.onView = onView;
    }


    private String getCategoryColor(EventCategory category) {
        if (category == null) return "#777777"; // Gris par défaut

        switch (category) {
            case CONCERT:
                return "#8A2BE2"; // Violet
            case THEATRE:
                return "#D32F2F"; // Rouge théâtre
            case SPORT:
                return "#E65100"; // Orange dynamique
            case CONFERENCE:
                return "#1976D2"; // Bleu professionnel
            case AUTRE:
            default:
                return "#607D8B"; // Gris bleuté
        }
    }
}