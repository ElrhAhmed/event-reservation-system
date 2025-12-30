package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import ma.projet.events.entity.Event;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

public class EventCard extends Card {

    private Runnable onView;

    public EventCard(Event event) {

        setWidthFull();
        addClassName("event-card");

        /* ===== IMAGE ===== */
        /* Dans EventCard.java, remplacez la section IMAGE par ceci : */

        /* ===== IMAGE ===== */
        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            Image image = new Image(event.getImageUrl(), event.getTitre());
            image.setWidthFull();
            image.setHeight("180px");
            image.getStyle().set("object-fit", "cover");
            image.getStyle().set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0");
            add(image);
        } else {
            // PLACEHOLDER SI PAS D'IMAGE
            Div placeholder = new Div();
            placeholder.setWidthFull();
            placeholder.setHeight("180px");
            placeholder.getStyle().set("background-color", "var(--lumo-contrast-10pct)");
            placeholder.getStyle().set("display", "flex");
            placeholder.getStyle().set("align-items", "center");
            placeholder.getStyle().set("justify-content", "center");
            placeholder.getStyle().set("border-radius", "var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0 0");

            Icon icon = VaadinIcon.PICTURE.create();
            icon.setSize("32px");
            icon.setColor("var(--lumo-contrast-30pct)");

            placeholder.add(icon);
            add(placeholder);
        }

        /* ===== HEADER ===== */
        H4 title = new H4(event.getTitre());
        title.getStyle().set("margin", "0");

        StatusBadge statusBadge = new StatusBadge(
                event.getStatut().getLabel(),
                event.getStatut().getColor()
        );

        HorizontalLayout header = new HorizontalLayout(title, statusBadge);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        /* ===== CONTENT ===== */
        Span date = new Span(
                DateFormatter.format(event.getDateDebut())
        );

        Span city = new Span(event.getVille());

        Span price = new Span(
                PriceFormatter.format(event.getPrixUnitaire())
        );
        price.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        Span organizer = new Span(
                "Organisé par " + event.getOrganisateur().getNomComplet()
        );
        organizer.getStyle().set("font-size", "0.85rem");

        VerticalLayout content = new VerticalLayout(
                date, city, price, organizer
        );
        content.setPadding(false);
        content.setSpacing(false);

        /* ===== ACTION ===== */
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.addClickListener(e -> {
            if (onView != null) onView.run();
        });

        Div footer = new Div(detailsBtn);
        footer.getStyle().set("margin-top", "var(--lumo-space-s)");

        add(header, content, footer);
    }

    public void setOnView(Runnable onView) {
        this.onView = onView;
    }
}
