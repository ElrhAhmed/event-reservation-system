package ma.projet.events.ui.component.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;

import java.time.Duration;

public class EventForm extends FormLayout {

    private final Binder<Event> binder = new BeanValidationBinder<>(Event.class);
    private Event event;

    // Champs
    TextField titre = new TextField("Titre de l'événement");
    ComboBox<EventCategory> categorie = new ComboBox<>("Catégorie");

    DateTimePicker dateDebut = new DateTimePicker("Début");
    DateTimePicker dateFin = new DateTimePicker("Fin");

    TextField lieu = new TextField("Lieu (Salle, Adresse)");
    TextField ville = new TextField("Ville");

    IntegerField capaciteMax = new IntegerField("Capacité (Personnes)");
    NumberField prixUnitaire = new NumberField("Prix du billet (MAD)");

    TextField imageUrl = new TextField("URL de l'image (Affiche)");
    TextArea description = new TextArea("Description détaillée");

    // Boutons
    Button saveDraft = new Button("Sauvegarder en Brouillon");
    Button publish = new Button("Publier maintenant");
    Button preview = new Button("Prévisualiser");
    Button cancel = new Button("Annuler");

    public EventForm() {
        addClassName("event-form");

        configureFields();

        // Configuration du Binder
        binder.bindInstanceFields(this);

        // Validation Dates
        binder.withValidator(e -> {
            if (dateDebut.getValue() == null || dateFin.getValue() == null) return true;
            return dateFin.getValue().isAfter(dateDebut.getValue());
        }, "La date de fin doit être après la date de début");

        add(
                titre, categorie,
                dateDebut, dateFin,
                lieu, ville,
                capaciteMax, prixUnitaire,
                imageUrl,
                description,
                createButtonsLayout()
        );

        setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2)
        );

        setColspan(titre, 2);
        setColspan(imageUrl, 2);
        setColspan(description, 2);
    }

    private void configureFields() {
        categorie.setItems(EventCategory.values());
        categorie.setItemLabelGenerator(EventCategory::getLabel);

        dateDebut.setStep(Duration.ofMinutes(15));
        dateFin.setStep(Duration.ofMinutes(15));

        capaciteMax.setMin(1);
        capaciteMax.setStepButtonsVisible(true);

        prixUnitaire.setMin(0.0);
        Div suffix = new Div();
        suffix.setText("DH");
        prixUnitaire.setSuffixComponent(suffix);

        description.setMinHeight("150px");

        imageUrl.setPlaceholder("https://exemple.com/image.jpg");
        imageUrl.setHelperText("Lien direct vers une image JPG ou PNG");
    }

    private Component createButtonsLayout() {
        saveDraft.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        publish.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        preview.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);



        saveDraft.addClickListener(e -> {
            try {
                // 1. On écrit les valeurs du formulaire dans l'objet this.event
                binder.writeBean(this.event);
                // 2. On déclenche l'événement avec l'objet rempli
                fireEvent(new SaveDraftEvent(this, this.event));
            } catch (ValidationException valEx) {
                // Erreur de validation visuelle, on ne fait rien (Vaadin affiche rouge)
            }
        });

        publish.addClickListener(e -> {
            try {
                binder.writeBean(this.event);
                fireEvent(new PublishEvent(this, this.event));
            } catch (ValidationException valEx) {
                // Erreur validation
            }
        });

        preview.addClickListener(e -> {
            // Pour la prévisualisation, on écrit même si invalide (best effort)
            try { binder.writeBean(this.event); } catch (ValidationException ignored) {}
            fireEvent(new PreviewEvent(this, this.event));
        });

        cancel.addClickListener(event -> fireEvent(new CancelEvent(this)));

        saveDraft.addClickShortcut(Key.ENTER);
        cancel.addClickShortcut(Key.ESCAPE);

        return new HorizontalLayout(saveDraft, publish, preview, cancel);
    }

    public void setEvent(Event event) {
        this.event = event;
        binder.readBean(event); // Initialise le formulaire (mode lecture)
    }


    public boolean writeBeanIfValid(Event event) {
        return binder.writeBeanIfValid(event);
    }

    // --- EVENTS CLASSES (Inchangées) ---
    public static abstract class EventFormEvent extends ComponentEvent<EventForm> {
        private final Event event;
        protected EventFormEvent(EventForm source, Event event) {
            super(source, false);
            this.event = event;
        }
        public Event getEvent() { return event; }
    }

    public static class SaveDraftEvent extends EventFormEvent {
        SaveDraftEvent(EventForm source, Event event) { super(source, event); }
    }
    public static class PublishEvent extends EventFormEvent {
        PublishEvent(EventForm source, Event event) { super(source, event); }
    }
    public static class PreviewEvent extends EventFormEvent {
        PreviewEvent(EventForm source, Event event) { super(source, event); }
    }
    public static class CancelEvent extends EventFormEvent {
        CancelEvent(EventForm source) { super(source, null); }
    }

    public Registration addSaveDraftListener(ComponentEventListener<SaveDraftEvent> listener) {
        return addListener(SaveDraftEvent.class, listener);
    }
    public Registration addPublishListener(ComponentEventListener<PublishEvent> listener) {
        return addListener(PublishEvent.class, listener);
    }
    public Registration addPreviewListener(ComponentEventListener<PreviewEvent> listener) {
        return addListener(PreviewEvent.class, listener);
    }
    public Registration addCancelListener(ComponentEventListener<CancelEvent> listener) {
        return addListener(CancelEvent.class, listener);
    }
}