package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma.projet.events.service.EventService;
import ma.projet.events.ui.component.card.EventCard;
import ma.projet.events.ui.layout.PublicLayout;
import ma.projet.events.ui.navigation.NavigationManager;

import java.util.Arrays;
import java.util.List;

@Route(value = "", layout = PublicLayout.class)
@RouteAlias(value = "home", layout = PublicLayout.class)
@PageTitle("Accueil | FESTIVENT")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private final FlexLayout popularEventsContainer;

    public HomeView(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        // Configuration globale de la vue
        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName(LumoUtility.Background.BASE);
        // Important: cache le scroll horizontal si la Hero section dépasse un peu
        addClassName(LumoUtility.Overflow.HIDDEN);

        /* 1. HERO SECTION (Bannière) */
        VerticalLayout heroSection = createHeroSection();

        /* 2. BARRE DE RECHERCHE (Flottante) */
        Div floatingSearchBar = createSimpleFloatingSearchBar();

        /* 3. CONTENU PRINCIPAL */
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setMaxWidth("1240px"); // Largeur standard container
        mainContent.setWidthFull();
        mainContent.addClassNames(LumoUtility.Margin.Horizontal.AUTO, LumoUtility.Margin.Top.XLARGE);
        mainContent.setPadding(true);
        mainContent.setSpacing(true);

        // -- Titre de section --
        HorizontalLayout headerRow = createSectionHeader();

        // -- Grille des événements --
        popularEventsContainer = new FlexLayout();
        popularEventsContainer.setWidthFull();
        popularEventsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        popularEventsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        popularEventsContainer.addClassName(LumoUtility.Gap.LARGE);
        popularEventsContainer.addClassName(LumoUtility.Margin.Bottom.XLARGE);

        mainContent.add(headerRow, popularEventsContainer);

        add(heroSection, floatingSearchBar, mainContent);

        // Chargement des données
        loadPopularEvents();
    }

    private VerticalLayout createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.addClassName("hero-section"); // Utilise le fond bleu défini en CSS
        hero.setWidthFull();
        hero.setSpacing(false);
        hero.setPadding(false);

        // 1. Grand Titre
        H1 title = new H1("Découvrez les meilleurs événements");
        title.addClassNames(
                LumoUtility.FontWeight.EXTRABOLD,
                LumoUtility.Margin.Bottom.SMALL
        );
        title.getStyle().set("font-size", "3.5rem"); // Taille imposante
        title.getStyle().set("line-height", "1.2");
        title.getStyle().set("color", "white");

        // 2. Sous-titre
        Span subtitle = new Span("Concerts, festivals, théâtre, sport... Trouvez et réservez vos places en quelques clics.");
        subtitle.addClassNames(LumoUtility.FontSize.LARGE);
        subtitle.getStyle().set("opacity", "0.9");
        subtitle.getStyle().set("max-width", "700px");
        subtitle.getStyle().set("font-weight", "400");

        // 3. Bouton Blanc "Explorer" (Comme sur l'image 2)
        Button exploreBtn = new Button("Explorer les événements");
        exploreBtn.addClassName("hero-btn"); // Utilise le style CSS bouton blanc
        exploreBtn.addClickListener(e -> navigationManager.goToEvents());

        hero.add(title, subtitle, exploreBtn);
        return hero;
    }

    private Div createSimpleFloatingSearchBar() {
        Div container = new Div();
        container.setWidthFull();
        container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER);

        // Marge négative pour faire remonter la carte sur le Hero
        container.getStyle().set("margin-top", "-3rem");
        container.getStyle().set("padding", "0 1rem"); // Sécurité mobile
        container.getStyle().set("z-index", "10"); // Au-dessus du reste

        Div searchCard = new Div();
        // Utilisation de la classe CSS définie dans styles.css
        searchCard.addClassName("floating-search-card");

        searchCard.addClassNames(
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexWrap.WRAP,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.AlignItems.END
        );
        searchCard.setMaxWidth("900px");
        searchCard.setWidthFull();

        // Champs de recherche
        TextField searchField = new TextField("Mots-clés");
        searchField.setPlaceholder("Ex: Jazz, Casablanca...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.addClassName(LumoUtility.Flex.GROW);
        searchField.setMinWidth("250px");

        ComboBox<EventCategory> categoryCombo = new ComboBox<>("Catégorie");
        categoryCombo.setItems(Arrays.asList(EventCategory.values()));
        categoryCombo.setItemLabelGenerator(EventCategory::getLabel);
        categoryCombo.setPlaceholder("Toutes");
        categoryCombo.setWidth("200px");

        Button searchBtn = new Button("Rechercher");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.setIcon(new Icon(VaadinIcon.ARROW_RIGHT));
        searchBtn.addClickListener(e -> navigationManager.goToEvents());

        searchCard.add(searchField, categoryCombo, searchBtn);
        container.add(searchCard);

        return container;
    }

    private HorizontalLayout createSectionHeader() {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(Alignment.END);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        VerticalLayout titles = new VerticalLayout();
        titles.setPadding(false);
        titles.setSpacing(false);

        H2 title = new H2("À la une");
        title.addClassNames(LumoUtility.Margin.NONE, LumoUtility.FontWeight.BOLD);

        Span subtitle = new Span("Les événements les plus populaires du moment");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        titles.add(title, subtitle);

        Button seeAllBtn = new Button("Voir tout le catalogue", new Icon(VaadinIcon.ARROW_RIGHT));
        seeAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        seeAllBtn.setIconAfterText(true);
        seeAllBtn.addClickListener(e -> navigationManager.goToEvents());

        row.add(titles, seeAllBtn);
        return row;
    }

    private void loadPopularEvents() {
        // Récupère les 4 ou 8 événements les plus populaires
        List<Event> events = eventService.getPopularEvents(4);
        popularEventsContainer.removeAll();

        if (events.isEmpty()) {
            Div emptyState = new Div();
            emptyState.setText("Aucun événement disponible pour le moment.");
            emptyState.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Padding.LARGE);
            popularEventsContainer.add(emptyState);
            return;
        }

        for (Event event : events) {
            EventCard card = new EventCard(event);
            card.setOnView(() -> navigationManager.goToEventDetail(event.getId()));
            popularEventsContainer.add(card);
        }
    }
}