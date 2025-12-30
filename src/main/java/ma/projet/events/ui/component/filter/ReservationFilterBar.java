package ma.projet.events.ui.component.filter;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.ReservationStatus;
import ma.projet.events.entity.User;

import java.util.List;
import java.util.function.Consumer;

public class ReservationFilterBar extends HorizontalLayout {

    private final TextField code = new TextField();
    private final ComboBox<ReservationStatus> statut = new ComboBox<>();
    private final ComboBox<Event> event = new ComboBox<>();
    private final ComboBox<User> user = new ComboBox<>();

    private final Button filter = new Button("Filtrer");
    private final Button reset = new Button("Réinitialiser");

    public ReservationFilterBar(
            List<Event> events,
            List<User> users,
            Consumer<ReservationFilter> onFilter
    ) {

        setWidthFull();
        setAlignItems(Alignment.END);

        code.setPlaceholder("Code réservation");

        statut.setPlaceholder("Statut");
        statut.setItems(ReservationStatus.values());
        statut.setItemLabelGenerator(ReservationStatus::getLabel);

        event.setPlaceholder("Événement");
        event.setItems(events);
        event.setItemLabelGenerator(Event::getTitre);

        user.setPlaceholder("Utilisateur");
        user.setItems(users);
        user.setItemLabelGenerator(User::getNomComplet);

        filter.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        reset.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        filter.addClickListener(e ->
                onFilter.accept(buildFilter()));

        reset.addClickListener(e -> {
            clear();
            onFilter.accept(new ReservationFilter());
        });

        add(
                code,
                statut,
                event,
                user,
                filter,
                reset
        );
    }

    private ReservationFilter buildFilter() {
        ReservationFilter f = new ReservationFilter();
        f.code = code.getValue();
        f.statut = statut.getValue();
        f.eventId = event.getValue() != null ? event.getValue().getId() : null;
        f.userId = user.getValue() != null ? user.getValue().getId() : null;
        return f;
    }

    private void clear() {
        code.clear();
        statut.clear();
        event.clear();
        user.clear();
    }

    /* =======================
       DTO interne
       ======================= */

    public static class ReservationFilter {
        public String code;
        public ReservationStatus statut;
        public Long eventId;
        public Long userId;
    }
}
