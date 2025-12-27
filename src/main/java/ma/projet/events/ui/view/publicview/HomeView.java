package ma.projet.events.ui.view. publicview;

import com.vaadin. flow.component.UI;
import com.vaadin. flow.component.button.Button;
import com.vaadin.flow.component.button. ButtonVariant;
import com. vaadin.flow.component.html. Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html. Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin. flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com. vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com. vaadin.flow.router.Route;
import com.vaadin. flow.server.auth.AnonymousAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma.projet.events.service.EventService;
import ma. projet.events.ui.component.EventCard;
import ma. projet.events.ui.component. EventFilterPanel;
import ma.projet. events.ui.component.EventSearchBar;
import ma.projet.events.ui.layout.PublicLayout;

import java.util.List;

/**
 * Public home page of the application.
 *
 * Sections:
 * 1. Hero/Search area with EventSearchBar and EventFilterPanel
 * 2. Popular events grid (using EventCard with real data from EventService)
 * 3. "Why Choose EventReserve" informational section
 *
 * Backend integration:
 * - EventService. getPopularEvents(6) for popular events
 * - Search navigation with parameters
 */
@Route(value = "", layout = PublicLayout.class)
@PageTitle("Home - EventReserve")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    private final EventService eventService;
    private final Div eventsGridContainer;

    public HomeView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f9fafb");

        // Section 1: Hero / Search Area
        add(createHeroSection());

        // Section 2: Popular Events
        this.eventsGridContainer = new Div();
        add(createPopularEventsSection());

        // Section 3: Why Choose EventReserve
        add(createWhyChooseSection());

        // Load popular events on initialization
        loadPopularEvents();
    }

    /**
     * SECTION 1: Hero / Search Area
     */
    private Div createHeroSection() {
        Div heroSection = new Div();
        heroSection.setWidthFull();
        heroSection.getStyle()
                .set("background-color", "var(--festivent-primary)")
                .set("padding", "4rem 1. 5rem")
                .set("text-align", "center");

        // Centered content container
        VerticalLayout heroContent = new VerticalLayout();
        heroContent.setMaxWidth("1200px");
        heroContent.getStyle()
                .set("margin", "0 auto")
                .set("width", "100%");
        heroContent.setAlignItems(FlexComponent.Alignment. CENTER);
        heroContent.setSpacing(true);

        // Large title
        H1 title = new H1("Discover & Book Amazing Events");
        title.getStyle()
                .set("color", "white")
                .set("font-size", "3rem")
                .set("font-weight", "800")
                .set("margin", "0 0 1rem 0")
                .set("line-height", "1.2");

        // Subtitle
        Span subtitle = new Span("Find and book the best events in your city");
        subtitle.getStyle()
                .set("color", "rgba(255, 255, 255, 0.9)")
                .set("font-size", "1.25rem")
                .set("margin-bottom", "2rem");

        // Event search bar
        EventSearchBar searchBar = new EventSearchBar();
        searchBar.setMaxWidth("800px");
        searchBar.setWidthFull();
        searchBar.getStyle()
                .set("background-color", "white")
                .set("border-radius", "var(--festivent-radius-lg)")
                .set("box-shadow", "0 10px 24px rgba(11, 100, 244, 0.16)");

        // Search listener
        searchBar.addSearchListener(e -> {
            String searchText = searchBar.getSearchText();
            EventCategory category = searchBar.getSelectedCategory();

            if (! searchText.isEmpty() || category != null) {
                UI.getCurrent().navigate("events");

                Notification.show(
                        "Searching for:  " + (searchText.isEmpty() ? "All" : searchText) +
                                (category != null ?  " in " + category.getLabel() : ""),
                        3000,
                        Notification. Position.TOP_CENTER
                );
            } else {
                UI.getCurrent().navigate("events");
            }
        });

        // Event filter panel (toggleable)
        EventFilterPanel filterPanel = new EventFilterPanel();
        filterPanel.setMaxWidth("800px");
        filterPanel.setWidthFull();
        filterPanel.setVisible(false);

        filterPanel.addApplyListener(e -> {
            UI.getCurrent().navigate("events");
            Notification.show("Applying filters...", 2000, Notification.Position.TOP_CENTER);
        });

        // Toggle filters button
        Button toggleFiltersButton = new Button("Advanced Filters");
        toggleFiltersButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleFiltersButton.getStyle().set("color", "white");
        toggleFiltersButton.setIcon(VaadinIcon.FILTER.create());
        toggleFiltersButton.addClickListener(e -> {
            filterPanel.setVisible(!filterPanel.isVisible());
            toggleFiltersButton.setText(filterPanel.isVisible() ? "Hide Filters" : "Advanced Filters");
        });

        heroContent.add(title, subtitle, searchBar, toggleFiltersButton, filterPanel);
        heroSection.add(heroContent);

        return heroSection;
    }

    /**
     * SECTION 2: Popular Events
     */
    private Div createPopularEventsSection() {
        Div popularSection = new Div();
        popularSection.setWidthFull();
        popularSection.getStyle()
                .set("padding", "4rem 1. 5rem")
                .set("background-color", "white");

        // Centered container
        VerticalLayout container = new VerticalLayout();
        container.setMaxWidth("1200px");
        container.getStyle()
                .set("margin", "0 auto")
                .set("width", "100%");
        container.setPadding(false);
        container.setSpacing(false);

        // Header:   Title + "View All" button
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent. Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent. JustifyContentMode. BETWEEN);
        header.getStyle().set("margin-bottom", "2rem");

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H2 sectionTitle = new H2("Popular Events");
        sectionTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)");

        Span sectionSubtitle = new Span("Discover the most popular events happening now");
        sectionSubtitle. getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "1rem");

        titleSection.add(sectionTitle, sectionSubtitle);

        Button viewAllButton = new Button("View All Events");
        viewAllButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        viewAllButton.setIcon(VaadinIcon.ARROW_RIGHT. create());
        viewAllButton. addClickListener(e -> UI.getCurrent().navigate("events"));

        header.add(titleSection, viewAllButton);

        // ✅ CORRECTION ICI - Events grid container avec CSS Grid
        eventsGridContainer. setWidthFull();
        eventsGridContainer.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(300px, 1fr))")
                .set("gap", "1.5rem")
                .set("justify-items", "center")
                .set("margin-top", "0");

        container.add(header, eventsGridContainer);
        popularSection.add(container);

        return popularSection;
    }

    /**
     * Loads popular events from EventService and displays them in the grid.
     */
    private void loadPopularEvents() {
        try {
            // Fetch popular events from backend
            List<Event> popularEvents = eventService.getPopularEvents(6);

            // Clear existing cards
            eventsGridContainer.removeAll();

            if (popularEvents.isEmpty()) {
                // Show empty state message
                Span emptyMessage = new Span("No events available at the moment.  Check back soon!");
                emptyMessage.getStyle()
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("font-size", "1.125rem")
                        .set("text-align", "center")
                        .set("padding", "2rem")
                        .set("grid-column", "1 / -1");
                eventsGridContainer.add(emptyMessage);
            } else {
                // Create EventCard for each event
                for (Event event : popularEvents) {
                    EventCard card = new EventCard(event);

                    // Add click listener to navigate to event details
                    card.addDetailsClickListener(e -> {
                        UI.getCurrent().navigate("event/" + event.getId());
                    });

                    eventsGridContainer.add(card);
                }
            }

        } catch (Exception e) {
            // Handle errors gracefully
            Notification notification = Notification.show(
                    "Error loading events: " + e.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            // Show error message in grid
            Span errorMessage = new Span("⚠️ Unable to load events. Please try again later.");
            errorMessage.getStyle()
                    .set("color", "#DC3545")
                    .set("font-size", "1.125rem")
                    .set("text-align", "center")
                    .set("padding", "2rem")
                    .set("grid-column", "1 / -1");
            eventsGridContainer.removeAll();
            eventsGridContainer.add(errorMessage);
        }
    }

    /**
     * SECTION 3: Why Choose EventReserve
     */
    private Div createWhyChooseSection() {
        Div whyChooseSection = new Div();
        whyChooseSection.setWidthFull();
        whyChooseSection.getStyle()
                .set("background-color", "#f3f4f6")
                .set("padding", "4rem 1.5rem");

        // Centered container
        VerticalLayout container = new VerticalLayout();
        container. setMaxWidth("1200px");
        container.getStyle()
                .set("margin", "0 auto")
                .set("width", "100%");
        container.setAlignItems(FlexComponent. Alignment.CENTER);
        container. setPadding(false);
        container.setSpacing(true);

        // Section title
        H2 sectionTitle = new H2("Why Choose EventReserve? ");
        sectionTitle.getStyle()
                .set("margin", "0 0 2rem 0")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("text-align", "center");

        // Three feature blocks
        HorizontalLayout featuresRow = new HorizontalLayout();
        featuresRow.setWidthFull();
        featuresRow. setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        featuresRow.setSpacing(true);
        featuresRow.getStyle()
                .set("gap", "2rem")
                .set("flex-wrap", "wrap");

        featuresRow.add(createFeatureBlock(
                VaadinIcon.CALENDAR_CLOCK,
                "Easy Booking",
                "Book your favorite events in just a few clicks with our simple and intuitive interface."
        ));

        featuresRow.add(createFeatureBlock(
                VaadinIcon. STAR,
                "Trusted Organizers",
                "All events are organized by verified and trusted partners for your peace of mind."
        ));

        featuresRow.add(createFeatureBlock(
                VaadinIcon.SHIELD,
                "Secure Payments",
                "Your transactions are protected with industry-standard encryption and security."
        ));

        container.add(sectionTitle, featuresRow);
        whyChooseSection.add(container);

        return whyChooseSection;
    }

    /**
     * Creates a feature block for the "Why Choose" section.
     */
    private VerticalLayout createFeatureBlock(VaadinIcon iconType, String title, String description) {
        VerticalLayout block = new VerticalLayout();
        block.setMaxWidth("350px");
        block.setAlignItems(FlexComponent. Alignment.CENTER);
        block.getStyle()
                .set("background-color", "white")
                .set("padding", "2rem")
                .set("border-radius", "1rem")
                .set("box-shadow", "0 4px 12px rgba(11, 100, 244, 0.12)")
                .set("text-align", "center");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
                .set("width", "80px")
                .set("height", "80px")
                .set("border-radius", "50%")
                .set("background-color", "var(--festivent-accent)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("margin-bottom", "1.5rem");

        Icon icon = iconType.create();
        icon.setSize("40px");
        icon.getStyle().set("color", "var(--festivent-accent-text)");

        iconContainer.add(icon);

        // Title
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "1.25rem")
                .set("font-weight", "700")
                .set("color", "var(--festivent-secondary-text)")
                .set("margin-bottom", "0.5rem");

        // Description
        Span descriptionSpan = new Span(description);
        descriptionSpan.getStyle()
                .set("font-size", "1rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("line-height", "1.6");

        block.add(iconContainer, titleSpan, descriptionSpan);
        return block;
    }
}