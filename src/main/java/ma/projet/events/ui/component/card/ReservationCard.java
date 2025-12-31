package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Reservation;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

public class ReservationCard extends Div {

    private Runnable onView;
    private Runnable onCancel;

    public ReservationCard(Reservation reservation) {
        addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Overflow.HIDDEN,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN
        );
        getStyle().set("transition", "transform 0.2s");

        // Effet Hover
        getElement().addEventListener("mouseenter", e -> getStyle().set("transform", "translateY(-3px)"));
        getElement().addEventListener("mouseleave", e -> getStyle().set("transform", "translateY(0)"));

        /* ===== 1. HEADER (Image + Statut) ===== */
        Div media = new Div();
        media.setHeight("120px");
        media.setWidthFull();
        media.addClassName(LumoUtility.Position.RELATIVE);

        // Image
        String imgUrl = reservation.getEvenement().getImageUrl();
        if (imgUrl != null && !imgUrl.isBlank()) {
            media.getStyle().set("background-image", "url('" + imgUrl + "')");
            media.getStyle().set("background-size", "cover");
            media.getStyle().set("background-position", "center");
        } else {
            media.addClassName(LumoUtility.Background.CONTRAST_10);
        }

        // Badge Statut (Flottant en haut à droite)
        Span statusBadge = new Span(reservation.getStatut().getLabel());
        statusBadge.addClassNames(LumoUtility.Position.ABSOLUTE, LumoUtility.FontSize.XSMALL, LumoUtility.FontWeight.BOLD);
        statusBadge.getStyle().set("top", "10px").set("right", "10px").set("padding", "4px 8px").set("border-radius", "4px").set("color", "white");

        // Couleur du badge
        switch (reservation.getStatut()) {
            case CONFIRMEE -> statusBadge.getStyle().set("background-color", "var(--lumo-success-color)");
            case ANNULEE -> statusBadge.getStyle().set("background-color", "var(--lumo-error-color)");
            case EN_ATTENTE -> statusBadge.getStyle().set("background-color", "var(--lumo-contrast-color)");
        }
        media.add(statusBadge);

        /* ===== 2. CONTENT ===== */
        VerticalLayout content = new VerticalLayout();
        content.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        content.setSpacing(false);

        // Titre Événement
        H4 title = new H4(reservation.getEvenement().getTitre());
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontSize.LARGE);
        // Limite 1 ligne
        title.getStyle().set("white-space", "nowrap").set("overflow", "hidden").set("text-overflow", "ellipsis");

        // Code Réservation (Style "Ticket")
        Span code = new Span("#" + reservation.getCodeReservation());
        code.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.Background.CONTRAST_5);

        code.getStyle().set("font-family", "monospace");
        code.getStyle().set("padding", "2px 6px").set("border-radius", "4px");

        HorizontalLayout titleRow = new HorizontalLayout(title, code);
        titleRow.setWidthFull();
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);

        // Détails
        VerticalLayout details = new VerticalLayout();
        details.setPadding(false);
        details.setSpacing(false);
        details.add(createIconText(VaadinIcon.CALENDAR, DateFormatter.format(reservation.getEvenement().getDateDebut())));
        details.add(createIconText(VaadinIcon.MAP_MARKER, reservation.getEvenement().getVille()));
        details.add(createIconText(VaadinIcon.TICKET, reservation.getNombrePlaces() + " place(s) • " + PriceFormatter.format(reservation.getMontantTotal())));

        content.add(titleRow, details);

        /* ===== 3. ACTIONS ===== */
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Bottom.MEDIUM);

        Button viewBtn = new Button("Détails", VaadinIcon.EYE.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        viewBtn.addClickListener(e -> { if (onView != null) onView.run(); });

        Button cancelBtn = new Button("Annuler", VaadinIcon.CLOSE_SMALL.create());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        cancelBtn.setVisible(reservation.isAnnulable());
        cancelBtn.addClickListener(e -> { if (onCancel != null) onCancel.run(); });

        actions.add(viewBtn, cancelBtn);
        actions.setFlexGrow(1, viewBtn); // Le bouton détails prend la place

        add(media, content, actions);
    }

    private HorizontalLayout createIconText(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("14px");
        i.addClassName(LumoUtility.TextColor.TERTIARY);
        Span s = new Span(text);
        s.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
        HorizontalLayout hl = new HorizontalLayout(i, s);
        hl.setAlignItems(FlexComponent.Alignment.CENTER);
        hl.setSpacing(true);
        return hl;
    }

    /* ===== CALLBACKS ===== */
    public void setOnView(Runnable onView) { this.onView = onView; }
    public void setOnCancel(Runnable onCancel) { this.onCancel = onCancel; }
}