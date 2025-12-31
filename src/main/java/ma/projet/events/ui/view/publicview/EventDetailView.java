package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Event;
import ma.projet.events.service.EventService;
import ma.projet.events.ui.layout.PublicLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import ma.projet.events.ui.util.PriceFormatter;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "event", layout = PublicLayout.class)
@PageTitle("Détails Événement | FESTIVENT")
@AnonymousAllowed
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventService eventService;
    private final NavigationManager navigationManager;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    public EventDetailView(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setWidthFull();
        setPadding(false);
        setSpacing(false);
        addClassName(LumoUtility.Background.BASE);
    }

    @Override
    public void setParameter(BeforeEvent event, Long eventId) {
        removeAll();
        try {
            Event eventEntity = eventService.getEventById(eventId);
            int availablePlaces = eventService.calculateAvailablePlaces(eventId);

            // --- 1. BANNIÈRE FLOU ---
            Div banner = new Div();
            banner.setWidthFull();
            banner.setHeight("350px");
            banner.addClassName(LumoUtility.Background.CONTRAST_10);
            if (eventEntity.getImageUrl() != null) {
                banner.getStyle()
                        .set("background-image", "url('" + eventEntity.getImageUrl() + "')")
                        .set("background-size", "cover")
                        .set("background-position", "center")
                        .set("filter", "blur(10px) brightness(0.6)") // Flou + Sombre
                        .set("transform", "scale(1.1)"); // Zoom léger pour éviter bords blancs du flou
            }
            // Position absolue
            banner.getStyle().set("position", "absolute").set("top", "0").set("left", "0").set("z-index", "0");
            // Overflow hidden sur le parent pour couper le flou qui dépasse
            getElement().getStyle().set("overflow-x", "hidden");

            // --- 2. CONTENU ---
            VerticalLayout contentContainer = new VerticalLayout();
            contentContainer.setMaxWidth("1100px");
            contentContainer.setWidthFull();
            contentContainer.addClassNames(LumoUtility.Margin.Horizontal.AUTO);
            contentContainer.setPadding(true);
            contentContainer.getStyle().set("z-index", "1"); // Au dessus de la bannière

            // Spacer pour descendre
            Div spacer = new Div();
            spacer.setHeight("100px");
            contentContainer.add(spacer);

            // Layout 2 colonnes
            HorizontalLayout split = new HorizontalLayout();
            split.setWidthFull();
            split.addClassName(LumoUtility.FlexWrap.WRAP);
            split.addClassName(LumoUtility.Gap.XLARGE);

            // --- COLONNE GAUCHE ---
            VerticalLayout leftCol = new VerticalLayout();
            leftCol.setWidth("60%");
            leftCol.setMinWidth("320px");
            leftCol.setSpacing(true);

            // Image Principale Nette
            Image mainImage = new Image(eventEntity.getImageUrl(), "Cover");
            mainImage.setWidthFull();
            mainImage.addClassName(LumoUtility.BorderRadius.LARGE);
            mainImage.addClassName(LumoUtility.BoxShadow.LARGE);
            mainImage.getStyle().set("max-height", "450px").set("object-fit", "cover");

            // Titre & Date
            H1 title = new H1(eventEntity.getTitre());
            title.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.FontSize.XXLARGE);

            // Description
            H3 descTitle = new H3("À propos de l'événement");
            descTitle.addClassName(LumoUtility.Margin.Top.LARGE);

            Paragraph desc = new Paragraph(eventEntity.getDescription());
            desc.addClassName(LumoUtility.TextColor.SECONDARY);
            desc.getStyle().set("line-height", "1.7").set("white-space", "pre-line");

            leftCol.add(mainImage, title, descTitle, desc);

            // --- COLONNE DROITE (Sticky) ---
            VerticalLayout rightCol = createStickyBookingCard(eventEntity, availablePlaces);
            rightCol.setWidth("35%");
            rightCol.setMinWidth("300px");

            split.add(leftCol, rightCol);
            contentContainer.add(split);

            add(banner, contentContainer);

        } catch (Exception e) {
            e.printStackTrace();
            add(new H2("Événement introuvable"));
        }
    }

    private VerticalLayout createStickyBookingCard(Event event, int availablePlaces) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.MEDIUM,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.LARGE
        );
        card.getStyle().set("position", "sticky").set("top", "20px");
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");

        // 1. En-tête : Prix + Catégorie
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 price = new H2(PriceFormatter.format(event.getPrixUnitaire()));
        price.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.NONE);

        // MODIFICATION : Badge Catégorie au lieu de Statut
        Span categoryBadge = new Span(event.getCategorie().getLabel());
        categoryBadge.getElement().getThemeList().add("badge contrast"); // Badge gris/neutre élégant
        categoryBadge.getStyle().set("text-transform", "uppercase");

        header.add(price, categoryBadge);

        // 2. Infos Pratiques
        VerticalLayout infos = new VerticalLayout();
        infos.setPadding(false);
        infos.setSpacing(true);
        infos.add(createRow(VaadinIcon.CALENDAR, event.getDateDebut().format(DATE_FMT)));
        infos.add(createRow(VaadinIcon.CLOCK, "À " + event.getDateDebut().format(DateTimeFormatter.ofPattern("HH:mm"))));
        infos.add(createRow(VaadinIcon.MAP_MARKER, event.getLieu() + ", " + event.getVille()));

        // 3. MODIFICATION : Barre de progression (Places restantes)
        VerticalLayout progressSection = new VerticalLayout();
        progressSection.setPadding(false);
        progressSection.setSpacing(false);
        progressSection.addClassName(LumoUtility.Margin.Top.MEDIUM);

        int totalPlaces = event.getCapaciteMax();
        int reserved = totalPlaces - availablePlaces;
        double progressValue = (double) reserved / totalPlaces;

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(progressValue);
        // Change la couleur si presque complet (>80%)
        if (progressValue > 0.8) {
            progressBar.addThemeVariants(com.vaadin.flow.component.progressbar.ProgressBarVariant.LUMO_ERROR);
        }

        Span placesText = new Span(availablePlaces + " places restantes sur " + totalPlaces);
        placesText.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);
        placesText.getStyle().set("margin-top", "5px");

        progressSection.add(progressBar, placesText);

        // 4. Bouton Action
        Button bookBtn = new Button("Réserver maintenant");
        bookBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        bookBtn.setWidthFull();
        bookBtn.addClassName(LumoUtility.Margin.Top.MEDIUM);

        if (availablePlaces <= 0) {
            bookBtn.setText("Complet");
            bookBtn.setEnabled(false);
            bookBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        } else {
            bookBtn.addClickListener(e -> {
                if(navigationManager.isAuthenticated()) {
                    navigationManager.goToReservation(event.getId());
                } else {
                    Notification.show("Connectez-vous pour réserver");
                    navigationManager.goToLogin();
                }
            });
        }

        // 5. Organisateur
        Div organizerInfo = new Div();
        organizerInfo.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.SMALL);
        organizerInfo.add(new Text("Organisé par "));
        Span orgName = new Span(event.getOrganisateur().getNomComplet());
        orgName.addClassName(LumoUtility.FontWeight.BOLD);
        organizerInfo.add(orgName);
        organizerInfo.getStyle().set("font-size", "0.9rem").set("color", "var(--lumo-secondary-text-color)");

        card.add(header, new Hr(), infos, progressSection, bookBtn, organizerInfo);
        return card;
    }

    private HorizontalLayout createRow(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("18px");
        i.addClassName(LumoUtility.TextColor.PRIMARY);
        Span s = new Span(text);
        s.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.BODY);

        HorizontalLayout row = new HorizontalLayout(i, s);
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        return row;
    }
}