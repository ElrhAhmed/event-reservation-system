package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html. H2;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import ma.projet.events.entity.*;
import ma.projet.events. ui.component.*;
import ma.projet.events. ui.layout.PublicLayout;

import java.time.LocalDateTime;

/**
 * Vue de test pour les composants (Phase 3)
 * Accessible sur /test-components
 */
@Route(value = "test-components", layout = PublicLayout.class)
public class ComponentTestView extends VerticalLayout {

    public ComponentTestView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.START);

        add(new H2("Test des composants Festivent"));

        // Test StatusBadge
        add(createStatusBadgeSection());

        // Test StatCard
        add(createStatCardSection());

        // Test EventCard
        add(createEventCardSection());

        // Test EventSearchBar
        add(createSearchBarSection());

        // Test FestiventConfirmDialog
        add(createDialogSection());
    }

    private VerticalLayout createStatusBadgeSection() {
        VerticalLayout section = new VerticalLayout();
        section.add(new H2("StatusBadge"));

        HorizontalLayout eventBadges = new HorizontalLayout(
                new StatusBadge(EventStatus.BROUILLON),
                new StatusBadge(EventStatus.PUBLIE),
                new StatusBadge(EventStatus.ANNULE),
                new StatusBadge(EventStatus.TERMINE)
        );

        HorizontalLayout reservationBadges = new HorizontalLayout(
                new StatusBadge(ReservationStatus.EN_ATTENTE),
                new StatusBadge(ReservationStatus. CONFIRMEE),
                new StatusBadge(ReservationStatus. ANNULEE)
        );

        section.add(eventBadges, reservationBadges);
        return section;
    }

    private HorizontalLayout createStatCardSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.add(new H2("StatCard"));

        section.add(
                new StatCard(VaadinIcon.TICKET.create(), "24", "Réservations", "var(--festivent-primary)"),
                new StatCard(VaadinIcon.MONEY.create(), "1 250 DH", "Revenus", "var(--festivent-accent)"),
                new StatCard(VaadinIcon. CALENDAR.create(), "8", "Événements", "var(--festivent-success)")
        );

        return section;
    }

    private HorizontalLayout createEventCardSection() {
        HorizontalLayout section = new HorizontalLayout();
        section.add(createMockEvent());
        return section;
    }

    private VerticalLayout createSearchBarSection() {
        VerticalLayout section = new VerticalLayout();
        section.add(new H2("EventSearchBar"));

        EventSearchBar searchBar = new EventSearchBar();
        searchBar.addValueChangeListener(e ->
                System.out.println("Recherche : " + e. getValue())
        );

        section.add(searchBar);
        return section;
    }

    private VerticalLayout createDialogSection() {
        VerticalLayout section = new VerticalLayout();
        section.add(new H2("FestiventConfirmDialog"));

        Button showDialogButton = new Button("Ouvrir le dialog", e ->
                FestiventConfirmDialog.showDelete(
                        "Supprimer l'événement",
                        "Êtes-vous sûr de vouloir supprimer cet événement ? Cette action est irréversible.",
                        () -> System.out.println("Événement supprimé !")
                )
        );

        section.add(showDialogButton);
        return section;
    }

    private EventCard createMockEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setTitre("Concert Jazz Festival");
        event.setCategorie(EventCategory.CONCERT);
        event.setStatut(EventStatus.PUBLIE);
        event.setDateDebut(LocalDateTime.now().plusDays(15));
        event.setDateFin(LocalDateTime.now().plusDays(15).plusHours(3));
        event.setLieu("Théâtre Mohammed V");
        event.setVille("Rabat");
        event.setPrixUnitaire(250.0);
        event.setCapaciteMax(500);

        return new EventCard(event);
    }
}