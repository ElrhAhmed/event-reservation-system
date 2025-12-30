package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

    private final HorizontalLayout popularEventsRow;

    public HomeView(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName(LumoUtility.Background.BASE);
        // Correction Scrollbar Global : Empêcher le débordement horizontal
        getStyle().set("overflow-x", "hidden");
        setMinHeight("100vh");

        // 1. HERO SECTION
        VerticalLayout heroSection = createHeroSection();

        // 2. BARRE DE RECHERCHE SIMPLIFIÉE
        Div floatingSearchBar = createSimpleFloatingSearchBar();

        // 3. CONTENU PRINCIPAL
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setMaxWidth("1240px");
        mainContent.setWidthFull(); // Important pour le centrage
        mainContent.addClassNames(LumoUtility.Margin.Horizontal.AUTO);
        mainContent.setPadding(true);
        mainContent.setSpacing(true);
        mainContent.addClassName(LumoUtility.Margin.Top.XLARGE);

        // Section "À la une"
        HorizontalLayout titleRow = createSectionTitle("Événements à la une", "Découvrez les meilleures expériences");

        Button seeAllBtn = new Button("Voir tout", new Icon(VaadinIcon.ARROW_RIGHT));
        seeAllBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        seeAllBtn.setIconAfterText(true);
        seeAllBtn.addClickListener(e -> navigationManager.goToEvents());

        titleRow.add(seeAllBtn);
        titleRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        titleRow.setWidthFull();

        // CORRECTION VISUELLE : Remplacement du Scroll par un Wrap propre
        popularEventsRow = new HorizontalLayout();
        popularEventsRow.setWidthFull();
        popularEventsRow.addClassNames(
                LumoUtility.FlexWrap.WRAP,       // Permet le retour à la ligne
                LumoUtility.Gap.MEDIUM,          // Espace entre les cartes
                LumoUtility.Padding.Bottom.MEDIUM,
                LumoUtility.JustifyContent.CENTER // Centre les cartes si < 4
        );

        mainContent.add(titleRow, popularEventsRow);

        add(heroSection, floatingSearchBar, mainContent);

        loadPopularEvents();
    }

    private VerticalLayout createHeroSection() {
        VerticalLayout hero = new VerticalLayout();
        hero.setWidthFull();
        hero.setHeight("450px");
        hero.setAlignItems(Alignment.CENTER);
        hero.setJustifyContentMode(JustifyContentMode.CENTER);

        hero.getStyle().set("background", "linear-gradient(135deg, #2563eb 0%, #3b82f6 100%)");
        hero.addClassName(LumoUtility.TextColor.PRIMARY_CONTRAST);

        H1 slogan = new H1("Découvrez les meilleurs événements");
        slogan.addClassNames(
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.BOLD,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.SMALL
        );

        Span subSlogan = new Span("Concerts, festivals, théâtre, sport... Trouvez et réservez vos places en quelques clics.");
        subSlogan.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.TextAlignment.CENTER
        );
        subSlogan.setMaxWidth("800px");

        Button exploreBtn = new Button("Explorer les événements");
        exploreBtn.addClassName(LumoUtility.Margin.Top.LARGE);
        exploreBtn.getStyle().set("background-color", "white");
        exploreBtn.getStyle().set("color", "#2563eb");
        exploreBtn.addThemeVariants(ButtonVariant.LUMO_LARGE);
        exploreBtn.addClickListener(e -> navigationManager.goToEvents());

        hero.add(slogan, subSlogan, exploreBtn);
        return hero;
    }

    private Div createSimpleFloatingSearchBar() {
        Div container = new Div();
        container.setWidthFull();
        container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.JustifyContent.CENTER);
        container.getStyle().set("margin-top", "-3rem");
        // Padding safe pour éviter que ça touche les bords sur mobile
        container.getStyle().set("padding-left", "1rem");
        container.getStyle().set("padding-right", "1rem");
        container.getStyle().set("box-sizing", "border-box");

        Div card = new Div();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.LARGE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.END,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.FlexWrap.WRAP
        );
        card.setMaxWidth("900px");
        card.setWidth("100%");

        TextField keywordField = new TextField("Rechercher");
        keywordField.setPlaceholder("Concert, Artiste...");
        keywordField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        keywordField.getStyle().set("flex-grow", "1");
        keywordField.setMinWidth("200px"); // UX mobile

        ComboBox<EventCategory> categoryField = new ComboBox<>("Catégorie");
        categoryField.setItems(Arrays.asList(EventCategory.values()));
        categoryField.setItemLabelGenerator(EventCategory::getLabel);
        categoryField.setPlaceholder("Toutes");
        categoryField.setWidth("200px");

        Button searchBtn = new Button("Rechercher");
        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> navigationManager.goToEvents());

        card.add(keywordField, categoryField, searchBtn);
        container.add(card);

        return container;
    }

    private HorizontalLayout createSectionTitle(String title, String subtitle) {
        H3 h3 = new H3(title);
        h3.addClassNames(LumoUtility.Margin.Bottom.NONE, LumoUtility.FontWeight.EXTRABOLD);

        VerticalLayout layout = new VerticalLayout(h3);
        if (subtitle != null) {
            Span sub = new Span(subtitle);
            sub.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
            layout.add(sub);
        }
        layout.setPadding(false);
        layout.setSpacing(false);

        HorizontalLayout row = new HorizontalLayout(layout);
        row.setAlignItems(Alignment.END);
        return row;
    }

    private void loadPopularEvents() {
        List<Event> popularEvents = eventService.getPopularEvents(4);
        popularEventsRow.removeAll();

        if (popularEvents.isEmpty()) {
            popularEventsRow.add(new Span("Aucun événement à la une pour le moment."));
            return;
        }

        for (Event event : popularEvents) {
            EventCard card = new EventCard(event);
            card.setOnView(() -> navigationManager.goToEventDetail(event.getId()));
            card.setMinWidth("300px");
            card.setMaxWidth("300px");
            popularEventsRow.add(card);
        }
    }
}