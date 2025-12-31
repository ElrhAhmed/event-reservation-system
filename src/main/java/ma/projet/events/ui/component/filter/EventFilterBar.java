package ma.projet.events.ui.component.filter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import ma.projet.events.entity.EventCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class EventFilterBar extends HorizontalLayout {

    private final TextField keyword = new TextField();
    private final ComboBox<EventCategory> categorie = new ComboBox<>();
    private final TextField ville = new TextField();

    private final DatePicker dateMin = new DatePicker();
    private final DatePicker dateMax = new DatePicker();

    private final NumberField prixMin = new NumberField();
    private final NumberField prixMax = new NumberField();

    private final Button filter = new Button("Filtrer");
    private final Button reset = new Button("Réinitialiser");

    public EventFilterBar(
            List<EventCategory> categories,
            Consumer<EventFilter> onFilter
    ) {

        setWidthFull();
        setAlignItems(Alignment.END);

        keyword.setPlaceholder("Mot-clé");
        ville.setPlaceholder("Ville");

        categorie.setPlaceholder("Catégorie");
        categorie.setItems(categories);
        categorie.setItemLabelGenerator(EventCategory::getLabel);

        dateMin.setPlaceholder("Date min");
        dateMax.setPlaceholder("Date max");

        prixMin.setPlaceholder("Prix min (MAD)");
        prixMax.setPlaceholder("Prix max (MAD)");

        filter.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        filter.addClickListener(e ->
                onFilter.accept(buildFilter()));

        reset.addClickListener(e -> {
            clear();
            onFilter.accept(new EventFilter());
        });

        add(
                keyword,
                categorie,
                ville,
                dateMin,
                dateMax,
                prixMin,
                prixMax,
                filter,
                reset
        );
    }

    private EventFilter buildFilter() {
        EventFilter f = new EventFilter();
        f.keyword = keyword.getValue();
        f.categorie = categorie.getValue();
        f.ville = ville.getValue();
        f.dateMin = dateMin.getValue();
        f.dateMax = dateMax.getValue();
        f.prixMin = prixMin.getValue();
        f.prixMax = prixMax.getValue();
        return f;
    }

    private void clear() {
        keyword.clear();
        categorie.clear();
        ville.clear();
        dateMin.clear();
        dateMax.clear();
        prixMin.clear();
        prixMax.clear();
    }



    public static class EventFilter {
        public String keyword;
        public EventCategory categorie;
        public String ville;
        public LocalDate dateMin;
        public LocalDate dateMax;
        public Double prixMin;
        public Double prixMax;
    }
}
