package ma.projet.events.ui.view. publicview;

import com.vaadin.flow.component.UI;
import com.vaadin.flow. component.button.Button;
import com.vaadin.flow.component.button. ButtonVariant;
import com. vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon. VaadinIcon;
import com.vaadin.flow.component. orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout. VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.service.EventService;
import ma. projet.events.ui.component. AvailablePlacesIndicator;
import ma.projet.events.ui.component.StatusBadge;
import ma. projet.events.ui.layout.PublicLayout;

import java.time.format.DateTimeFormatter;
import java.util. Locale;

/**
 * Public event detail page - Marketplace style.
 *
 * Layout:
 * - Centered content (max-width: 1200px)
 * - Back navigation at top
 * - Two-column layout:  content (left) + action card (right, sticky)
 * - Hero image, badges, title, description, organizer info
 * - Reserve Now button (redirects to login if not authenticated)
 */
@Route(value = "event", layout = PublicLayout.class)
@PageTitle("Event Details - EventReserve")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH);
    private static final String DEFAULT_IMAGE =
            "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=1200";

    private final EventService eventService;

    private Long eventId;
    private Event event;

    public EventDetailView(EventService eventService) {
        this.eventService = eventService;

        // Page-level configuration
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        getStyle()
                .set("background-color", "#f8f9fa")
                .set("padding-top", "1. 5rem")
                .set("padding-bottom", "3rem");
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        System.out.println("[DEBUG] EventDetailView.setParameter() - Reçu eventId=" + eventId);
        this.eventId = parameter;

        System.out. println("\n" + "=". repeat(60));
        System.out.println("🔍 EventDetailView - DÉBUT");
        System.out.println("   Requested Event ID: " + eventId);
        System.out.println("=".repeat(60));

        try {
            System.out.println("📡 Appel EventService.getEventById(" + eventId + ")...");
            this.event = eventService.getEventById(eventId);

            if (this.event == null) {
                System.err.println("❌ Event est NULL (ne devrait pas arriver)");
                showErrorPage();
                return;
            }

            System. out.println("✅ Event trouvé:");
            System.out.println("   - ID: " + this.event. getId());
            System.out. println("   - Titre: " + this.event.getTitre());
            System.out.println("   - Statut: " + this.event.getStatut());
            System.out.println("   - Catégorie: " + this.event.getCategorie());
            System.out.println("   - Ville: " + this.event.getVille());

            System.out.println("🔄 Vérification Organisateur.. .");
            if (this.event.getOrganisateur() == null) {
                System.err.println("❌ ERREUR:  Organisateur est NULL!");
                showErrorPage();
                return;
            }
            System.out.println("   - Organisateur ID: " + this.event.getOrganisateur().getId());
            System.out.println("   - Organisateur Nom: " + this.event.getOrganisateur().getNomComplet());

            System.out.println("🔄 Calcul des places disponibles...");
            int availablePlaces = eventService.calculateAvailablePlaces(eventId);
            System.out. println("   - Places disponibles: " + availablePlaces);

            System. out.println("🎨 Construction de la vue...");
            buildView();
            System.out.println("✅ Vue construite avec succès");

        } catch (ma. projet.events.exception.ResourceNotFoundException e) {
            System.err.println("❌ ResourceNotFoundException:");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            showErrorPage();

        } catch (org.hibernate.LazyInitializationException e) {
            System.err.println("❌ LazyInitializationException (problème Hibernate):");
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            showErrorPage();

        } catch (Exception e) {
            System.err.println("❌ Exception inattendue:");
            System.err.println("   Type: " + e.getClass().getName());
            System.err.println("   Message: " + e. getMessage());
            e.printStackTrace();
            showErrorPage();

        } finally {
            System.out.println("=".repeat(60));
            System.out.println("🏁 EventDetailView - FIN");
            System.out.println("=". repeat(60) + "\n");
        }
    }

    /**
     * Builds the complete event detail view.
     */
    private void buildView() {
        removeAll();

        // Main centered container
        VerticalLayout mainContainer = createMainContainer();
        add(mainContainer);
    }

    /**
     * Creates the main centered container.
     */
    private VerticalLayout createMainContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container. setMaxWidth("1200px");
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle()
                .set("padding", "0 1.5rem");

        // Back navigation
        container.add(createBackNavigation());

        // Two-column layout: content + action card
        container.add(createTwoColumnLayout());

        return container;
    }

    /**
     * Creates the back navigation link.
     */
    private HorizontalLayout createBackNavigation() {
        HorizontalLayout backNav = new HorizontalLayout();
        backNav.setPadding(false);
        backNav.setSpacing(true);
        backNav.setAlignItems(FlexComponent.Alignment.CENTER);
        backNav.getStyle()
                .set("margin-bottom", "1.5rem")
                .set("cursor", "pointer")
                .set("gap", "0.5rem");

        Icon backIcon = VaadinIcon.ARROW_LEFT.create();
        backIcon.setSize("18px");
        backIcon.getStyle().set("color", "#4a5568");

        Span backText = new Span("Back to events");
        backText.getStyle()
                .set("font-size", "0.95rem")
                .set("color", "#4a5568")
                .set("font-weight", "500");

        backNav.add(backIcon, backText);
        backNav.addClickListener(e -> UI.getCurrent().navigate("events"));

        // Hover effect
        backNav.getElement().addEventListener("mouseenter", e -> {
            backIcon.getStyle().set("color", "#0b64f4");
            backText. getStyle().set("color", "#0b64f4");
        });
        backNav.getElement().addEventListener("mouseleave", e -> {
            backIcon.getStyle().set("color", "#4a5568");
            backText.getStyle().set("color", "#4a5568");
        });

        return backNav;
    }

    /**
     * Creates the two-column layout (content + action card).
     */
    private HorizontalLayout createTwoColumnLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.START);
        layout.getStyle()
                .set("gap", "2rem")
                .set("align-items", "flex-start");

        // Left column: Event content (flexible)
        VerticalLayout contentColumn = createContentColumn();

        // Right column: Action card (fixed width, sticky)
        VerticalLayout actionColumn = createActionColumn();

        layout.add(contentColumn, actionColumn);
        layout.setFlexGrow(1, contentColumn);
        layout.setFlexGrow(0, actionColumn);

        return layout;
    }

    /**
     * Creates the left content column.
     */
    private VerticalLayout createContentColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setPadding(false);
        column.setSpacing(false);
        column.getStyle().set("gap", "1.5rem");

        // Hero image
        column.add(createHeroImage());

        // Badges
        column.add(createBadgesRow());

        // Event title
        column.add(createEventTitle());

        // Info row (date, time, location, seats)
        column.add(createInfoRow());

        // About section
        column.add(createAboutSection());

        // Organizer section
        column.add(createOrganizerSection());

        return column;
    }

    /**
     * Creates the hero image.
     */
    private Div createHeroImage() {
        Div imageContainer = new Div();
        imageContainer.setWidthFull();
        imageContainer.getStyle()
                .set("height", "400px")
                .set("border-radius", "1rem")
                .set("overflow", "hidden")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

        String imageUrl = (event.getImageUrl() != null && !event.getImageUrl().isBlank())
                ? event.getImageUrl()
                : DEFAULT_IMAGE;

        Image image = new Image(imageUrl, event.getTitre());
        image.setWidthFull();
        image.getStyle()
                .set("height", "100%")
                .set("object-fit", "cover");

        imageContainer.add(image);
        return imageContainer;
    }

    /**
     * Creates the badges row (category + city).
     */
    private HorizontalLayout createBadgesRow() {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.getStyle().set("gap", "0.75rem");

        // Category badge
        StatusBadge categoryBadge = new StatusBadge(
                event.getCategorie().getIcon() + " " + event.getCategorie().getLabel()
        );

        // City badge
        Span cityBadge = new Span("📍 " + event.getVille());
        cityBadge.getStyle()
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("padding", "0.25rem 0.75rem")
                .set("border-radius", "1rem")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("background-color", "#e2e8f0")
                .set("color", "#2d3748");

        row.add(categoryBadge, cityBadge);
        return row;
    }

    /**
     * Creates the event title.
     */
    private H1 createEventTitle() {
        H1 title = new H1(event.getTitre());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "2.25rem")
                .set("font-weight", "700")
                .set("color", "#1a202c")
                .set("line-height", "1.2");
        return title;
    }

    /**
     * Creates the info row with icons.
     */
    private VerticalLayout createInfoRow() {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(false);
        container.getStyle()
                .set("gap", "0.75rem")
                .set("padding", "1rem")
                .set("background-color", "#f7fafc")
                .set("border-radius", "0.75rem");

        container.add(
                createInfoItem(VaadinIcon.CALENDAR, "Date",
                        event.getDateDebut().format(DATE_FORMATTER)),
                createInfoItem(VaadinIcon.CLOCK, "Time",
                        event.getDateDebut().format(TIME_FORMATTER) + " - " +
                                event.getDateFin().format(TIME_FORMATTER)),
                createInfoItem(VaadinIcon.MAP_MARKER, "Location",
                        event.getLieu() + ", " + event.getVille()),
                createInfoItemWithComponent(VaadinIcon.USERS, "Capacity",
                        new AvailablePlacesIndicator(
                                eventService.calculateAvailablePlaces(event. getId())
                        ))
        );

        return container;
    }

    /**
     * Creates a single info item with icon and text.
     */
    private HorizontalLayout createInfoItem(VaadinIcon iconType, String label, String value) {
        HorizontalLayout item = new HorizontalLayout();
        item.setSpacing(true);
        item.setAlignItems(FlexComponent. Alignment.CENTER);
        item.getStyle().set("gap", "0.75rem");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", "#0b64f4");

        VerticalLayout textContainer = new VerticalLayout();
        textContainer.setPadding(false);
        textContainer.setSpacing(false);
        textContainer.getStyle().set("gap", "0.125rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#718096")
                .set("font-weight", "500")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "0.95rem")
                .set("color", "#2d3748")
                .set("font-weight", "500");

        textContainer.add(labelSpan, valueSpan);
        item.add(icon, textContainer);

        return item;
    }

    /**
     * Creates an info item with a component instead of text.
     */
    private HorizontalLayout createInfoItemWithComponent(
            VaadinIcon iconType, String label, com.vaadin.flow.component. Component valueComponent) {
        HorizontalLayout item = new HorizontalLayout();
        item.setSpacing(true);
        item.setAlignItems(FlexComponent. Alignment.CENTER);
        item.getStyle().set("gap", "0.75rem");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", "#0b64f4");

        VerticalLayout textContainer = new VerticalLayout();
        textContainer. setPadding(false);
        textContainer.setSpacing(false);
        textContainer.getStyle().set("gap", "0.125rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#718096")
                .set("font-weight", "500")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.05em");

        textContainer.add(labelSpan, valueComponent);
        item.add(icon, textContainer);

        return item;
    }

    /**
     * Creates the "About this event" section.
     */
    private VerticalLayout createAboutSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "1rem");

        H2 title = new H2("About this event");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "#1a202c");

        Paragraph description = new Paragraph(event. getDescription());
        description.getStyle()
                .set("margin", "0")
                .set("font-size", "1rem")
                .set("line-height", "1.7")
                .set("color", "#4a5568");

        section.add(title, description);
        return section;
    }

    /**
     * Creates the organizer section.
     */
    private VerticalLayout createOrganizerSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
                .set("gap", "1rem")
                .set("padding", "1.25rem")
                .set("background-color", "white")
                .set("border-radius", "0.75rem")
                .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

        H3 title = new H3("Organizer");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("color", "#1a202c");

        Span organizerName = new Span(event.getOrganisateur().getNomComplet());
        organizerName.getStyle()
                .set("font-size", "1rem")
                .set("color", "#4a5568");

        section.add(title, organizerName);
        return section;
    }

    /**
     * Creates the right action card (sticky).
     */
    private VerticalLayout createActionColumn() {
        VerticalLayout column = new VerticalLayout();
        column.setWidth("360px");
        column.setPadding(false);
        column.setSpacing(false);
        column.getStyle()
                .set("position", "sticky")
                .set("top", "2rem");

        // Card container
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "1rem")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem")
                .set("gap", "1.25rem");

        // Price section
        card.add(createPriceSection());

        // Divider
        card.add(createCardDivider());

        // Event details (compact)
        card.add(createCompactDetail(VaadinIcon.CALENDAR,
                event.getDateDebut().format(DATE_FORMATTER)));
        card.add(createCompactDetail(VaadinIcon. CLOCK,
                event.getDateDebut().format(TIME_FORMATTER)));
        card.add(createCompactDetail(VaadinIcon. MAP_MARKER,
                event.getLieu()));
        card.add(createCompactDetailWithComponent(VaadinIcon. USERS,
                new AvailablePlacesIndicator(
                        eventService.calculateAvailablePlaces(event.getId())
                )));

        // Divider
        card.add(createCardDivider());

        // Reserve button
        card.add(createReserveButton());

        // Info note
        card.add(createInfoNote());

        column.add(card);
        return column;
    }

    /**
     * Creates the price section in action card.
     */
    private VerticalLayout createPriceSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "0.25rem");

        Span priceLabel = new Span("Price per person");
        priceLabel.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#718096");

        Span priceValue = new Span(formatPrice(event.getPrixUnitaire()));
        priceValue.getStyle()
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", "#0b64f4");

        section.add(priceLabel, priceValue);
        return section;
    }

    /**
     * Creates a divider for the action card.
     */
    private Span createCardDivider() {
        Span divider = new Span();
        divider.getStyle()
                .set("width", "100%")
                .set("height", "1px")
                .set("background-color", "#e2e8f0");
        return divider;
    }

    /**
     * Creates a compact detail row in action card.
     */
    private HorizontalLayout createCompactDetail(VaadinIcon iconType, String text) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent. Alignment.CENTER);
        row.getStyle().set("gap", "0.75rem");

        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", "#718096");

        Span textSpan = new Span(text);
        textSpan.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "#4a5568");

        row.add(icon, textSpan);
        return row;
    }

    /**
     * Creates a compact detail row with component.
     */
    private HorizontalLayout createCompactDetailWithComponent(
            VaadinIcon iconType, com.vaadin.flow.component. Component component) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("gap", "0.75rem");

        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", "#718096");

        row.add(icon, component);
        return row;
    }

    /**
     * Creates the Reserve Now button.
     */
    private Button createReserveButton() {
        Button reserveButton = new Button("Reserve Now");
        reserveButton.setWidthFull();
        reserveButton.addThemeVariants(ButtonVariant. LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton. setIcon(VaadinIcon.TICKET. create());

        reserveButton.addClickListener(e -> {
            // TODO: Check if user is authenticated
            // For now, navigate to login
            UI.getCurrent().navigate("login");
        });

        return reserveButton;
    }

    /**
     * Creates the info note below the button.
     */
    private Span createInfoNote() {
        Span note = new Span("🔒 You'll need to sign in to complete your reservation");
        note.getStyle()
                .set("font-size", "0.8125rem")
                .set("color", "#718096")
                .set("line-height", "1.5")
                .set("text-align", "center");
        return note;
    }

    /**
     * Formats price safely.
     */
    private String formatPrice(Double price) {
        if (price == null || price == 0.0) {
            return "Free";
        }
        return String.format("%.2f DH", price);
    }

    /**
     * Shows an error page if event not found.
     */
    private void showErrorPage() {
        removeAll();

        VerticalLayout errorContainer = new VerticalLayout();
        errorContainer.setSizeFull();
        errorContainer.setAlignItems(Alignment.CENTER);
        errorContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        errorContainer.getStyle()
                .set("text-align", "center")
                .set("padding", "3rem");

        Span icon = new Span("❌");
        icon.getStyle().set("font-size", "4rem");

        H1 title = new H1("Event Not Found");
        title.getStyle()
                .set("color", "#e53e3e")
                .set("margin", "1rem 0");

        Paragraph message = new Paragraph(
                "The event you're looking for doesn't exist or has been removed."
        );
        message.getStyle().set("color", "#718096");

        Button backButton = new Button("Back to Events", VaadinIcon.ARROW_LEFT.create());
        backButton. addThemeVariants(ButtonVariant. LUMO_PRIMARY);
        backButton.addClickListener(e -> UI.getCurrent().navigate("events"));

        errorContainer.add(icon, title, message, backButton);
        add(errorContainer);
    }
}