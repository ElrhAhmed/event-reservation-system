package ma.projet.events.ui. view.publicview;

import com.vaadin.flow.component. UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component. html.Div;
import com.vaadin. flow.component.html.H1;
import com.vaadin. flow.component.html.H2;
import com.vaadin. flow.component.html.Paragraph;
import com.vaadin. flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin. flow.router.PageTitle;
import com. vaadin.flow.router.Route;
import ma.projet.events.entity.Event;
import ma.projet.events.service.EventService;
import ma.projet.events. ui.component.EventCard;
import ma.projet.events.ui. component.EventSearchBar;
import ma.projet.events.ui. layout.PublicLayout;

import java.util.List;

/**
 * Page d'accueil publique de Festivent
 * Route : / (racine)
 */
@Route(value = "", layout = PublicLayout.class)
@PageTitle("Festivent - Réservez vos événements culturels")
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final HorizontalLayout eventsContainer;

    public HomeView(EventService eventService) {
        this.eventService = eventService;

        // Configuration de la vue
        setSizeFull();
        setSpacing(false);
        setPadding(false);
        getStyle().set("background-color", "var(--festivent-bg)");

        // Hero section
        add(createHeroSection());

        // Section événements populaires
        VerticalLayout popularSection = new VerticalLayout();
        popularSection.setWidthFull();
        popularSection.setPadding(true);
        popularSection.setSpacing(true);
        popularSection.setAlignItems(Alignment.CENTER);

        H2 sectionTitle = new H2("Événements à la une");
        sectionTitle.getStyle().set("color", "var(--festivent-primary)");

        // Container des événements
        eventsContainer = new HorizontalLayout();
        eventsContainer.setWidthFull();
        eventsContainer.setSpacing(true);
        eventsContainer.getStyle()
                .set("flex-wrap", "wrap")
                .set("justify-content", "center");

        popularSection.add(sectionTitle, eventsContainer);
        add(popularSection);

        // Charger les événements
        loadPopularEvents();
    }

    /**
     * Crée la section hero (bandeau principal)
     */
    private VerticalLayout createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setHeight("400px");
        hero.setAlignItems(Alignment.CENTER);
        hero.setJustifyContentMode(JustifyContentMode.CENTER);
        hero.setSpacing(true);
        hero.getStyle()
                .set("background", "linear-gradient(135deg, var(--festivent-primary) 0%, var(--festivent-accent) 100%)")
                .set("padding", "var(--lumo-space-xl)");

        // Titre principal
        H1 title = new H1("Découvrez les meilleurs événements");
        title.getStyle()
                .set("color", "white")
                .set("text-align", "center")
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xxxl)");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Concerts, théâtres, conférences...  Réservez en quelques clics");
        subtitle.getStyle()
                .set("color", "rgba(255, 255, 255, 0.9)")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("text-align", "center")
                .set("margin-top", "var(--lumo-space-s)");

        // ✅ SECTION RECHERCHE CORRIGÉE
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setAlignItems(Alignment.END);

        EventSearchBar searchBar = new EventSearchBar("Rechercher un événement.. .");
        searchBar.setWidth("500px");

        Button searchButton = new Button("Rechercher");
        searchButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        searchButton.getStyle()
                .set("background-color", "white")
                .set("color", "var(--festivent-primary)");

        // Action du bouton
        searchButton.addClickListener(e -> {
            String keyword = searchBar.getValue();
            if (keyword != null && !keyword.isBlank()) {
                UI.getCurrent().navigate("events");
            }
        });

        // Action sur Enter (simule le clic sur le bouton)
        searchBar.getElement().addEventListener("keydown", event -> {
            if (event.getEventData().getString("event.key").equals("Enter")) {
                searchButton.click();
            }
        }).addEventData("event.key");

        searchLayout.add(searchBar, searchButton);

        // Bouton CTA
        Button exploreButton = new Button("Explorer tous les événements");
        exploreButton.addThemeVariants(ButtonVariant.LUMO_LARGE, ButtonVariant. LUMO_PRIMARY);
        exploreButton.getStyle()
                .set("background-color", "white")
                .set("color", "var(--festivent-primary)")
                .set("margin-top", "var(--lumo-space-m)");

        exploreButton.addClickListener(e -> {
            UI.getCurrent().navigate("events");
        });

        hero.add(title, subtitle, searchLayout, exploreButton);
        return hero;
    }

    /**
     * Charge les 6 événements les plus populaires
     */
    private void loadPopularEvents() {
        eventsContainer.removeAll();

        try {
            List<Event> popularEvents = eventService.getPopularEvents(6);

            if (popularEvents.isEmpty()) {
                // Aucun événement
                Paragraph noEvents = new Paragraph("Aucun événement disponible pour le moment");
                noEvents.getStyle()
                        .set("color", "var(--festivent-text-secondary)")
                        .set("font-size", "var(--lumo-font-size-l)")
                        .set("padding", "var(--lumo-space-xl)");
                eventsContainer.add(noEvents);
            } else {
                // Afficher les événements
                for (Event event :  popularEvents) {
                    EventCard card = new EventCard(event);

                    // Click sur la card → Redirection vers les détails
                    card.addClickListener(clickEvent -> {
                        UI.getCurrent().navigate("event/" + event.getId());
                    });

                    eventsContainer.add(card);
                }
            }
        } catch (Exception e) {
            // Erreur lors du chargement
            Paragraph error = new Paragraph("Erreur lors du chargement des événements");
            error.getStyle()
                    .set("color", "var(--festivent-error)")
                    .set("padding", "var(--lumo-space-xl)");
            eventsContainer. add(error);

            // Log l'erreur (pour debug)
            e.printStackTrace();
        }
    }
}