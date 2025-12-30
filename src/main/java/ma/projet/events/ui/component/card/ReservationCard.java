package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import ma.projet.events.entity.Reservation;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

public class ReservationCard extends Card {

    private Runnable onView;
    private Runnable onCancel;

    public ReservationCard(Reservation reservation) {

        setWidthFull();
        addClassName("reservation-card");

        /* ===== HEADER ===== */
        H4 code = new H4(reservation.getCodeReservation());
        code.getStyle().set("margin", "0");

        StatusBadge statusBadge = new StatusBadge(
                reservation.getStatut().getLabel(),
                reservation.getStatut().getColor()
        );

        HorizontalLayout header = new HorizontalLayout(code, statusBadge);
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        /* ===== CONTENT ===== */
        Span title = new Span(reservation.getEvenement().getTitre());
        title.getStyle().set("font-weight", "600");

        Span date = new Span(
                DateFormatter.format(
                        reservation.getEvenement().getDateDebut()
                )
        );

        Span city = new Span(reservation.getEvenement().getVille());

        Span places = new Span(reservation.getNombrePlaces() + " place(s)");

        Span price = new Span(
                PriceFormatter.format(reservation.getMontantTotal())
        );
        price.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        VerticalLayout content = new VerticalLayout(
                title, date, city, places, price
        );
        content.setPadding(false);
        content.setSpacing(false);

        /* ===== ACTIONS ===== */
        Button viewBtn = new Button(VaadinIcon.EYE.create());
        viewBtn.addClickListener(e -> {
            if (onView != null) onView.run();
        });

        Button cancelBtn = new Button(VaadinIcon.CLOSE_SMALL.create());
        cancelBtn.getStyle().set("color", "var(--lumo-error-text-color)");
        cancelBtn.setVisible(reservation.isAnnulable());
        cancelBtn.addClickListener(e -> {
            if (onCancel != null) onCancel.run();
        });

        HorizontalLayout actions = new HorizontalLayout(viewBtn, cancelBtn);

        Div footer = new Div(actions);
        footer.getStyle().set("margin-top", "var(--lumo-space-s)");

        add(header, content, footer);
    }

    /* ===== CALLBACKS ===== */
    public void setOnView(Runnable onView) {
        this.onView = onView;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }
}
