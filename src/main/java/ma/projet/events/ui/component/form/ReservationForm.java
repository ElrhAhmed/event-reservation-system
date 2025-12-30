package ma.projet.events.ui.component.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import ma.projet.events.entity.Event;
import ma.projet.events.ui.util.DateFormatter;
import ma.projet.events.ui.util.PriceFormatter;

import java.util.function.Consumer;

public class ReservationForm extends VerticalLayout {

    private final Event event;

    private IntegerField placesField;
    private Span totalPriceSpan;

    private Consumer<Integer> onSubmit;

    public ReservationForm(Event event) {
        this.event = event;

        setWidthFull();
        setSpacing(true);
        setPadding(false);

        add(
                buildEventSummary(),
                buildReservationSection()
        );
    }

    /* =========================
       API POUR LA VIEW
       ========================= */

    public void setOnSubmit(Consumer<Integer> onSubmit) {
        this.onSubmit = onSubmit;
    }

    public int getSelectedPlaces() {
        return placesField.getValue() != null ? placesField.getValue() : 1;
    }

    /* =========================
       UI
       ========================= */

    private Component buildEventSummary() {
        VerticalLayout box = new VerticalLayout();
        box.addClassName("card");

        box.add(
                new H3("Récapitulatif de l'événement"),
                new H4(event.getTitre()),
                new Span(DateFormatter.format(event.getDateDebut())),
                new Span(event.getLieu() + ", " + event.getVille()),
                new Span(PriceFormatter.format(event.getPrixUnitaire()) + " / place")
        );

        return box;
    }

    private Component buildReservationSection() {
        VerticalLayout box = new VerticalLayout();
        box.addClassName("card");

        placesField = new IntegerField("Nombre de places");
        placesField.setMin(1);
        placesField.setMax(10);
        placesField.setValue(1);
        placesField.setStepButtonsVisible(true);
        placesField.setWidth("200px");

        totalPriceSpan = new Span();
        updateTotalPrice();

        placesField.addValueChangeListener(e -> updateTotalPrice());

        HorizontalLayout actions = new HorizontalLayout(
                buildResetButton(),
                buildSubmitButton()
        );

        box.add(
                new H3("Votre réservation"),
                placesField,
                totalPriceSpan,
                actions
        );

        return box;
    }

    private Button buildResetButton() {
        Button cancel = new Button("Annuler");
        cancel.addClickListener(e -> placesField.setValue(1));
        return cancel;
    }

    private Button buildSubmitButton() {
        Button submit = new Button("Confirmer la réservation");
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickListener(e -> {
            if (onSubmit != null) {
                onSubmit.accept(getSelectedPlaces());
            }
        });
        return submit;
    }

    private void updateTotalPrice() {
        int places = getSelectedPlaces();
        double total = places * event.getPrixUnitaire();

        totalPriceSpan.setText(
                places + " place(s) × " +
                        PriceFormatter.format(event.getPrixUnitaire()) +
                        " = " +
                        PriceFormatter.format(total)
        );
    }
}
