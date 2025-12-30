package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
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

    // Formatteur spécifique pour correspondre à l'image ("jeudi 20 février 2025")
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public EventDetailView(EventService eventService, NavigationManager navigationManager) {
        this.eventService = eventService;
        this.navigationManager = navigationManager;

        setWidthFull();
        setPadding(false);
        addClassName(LumoUtility.Background.BASE);

        // Conteneur principal centré avec marge en haut
        setAlignItems(Alignment.CENTER);
    }

    @Override
    public void setParameter(BeforeEvent event, Long eventId) {
        removeAll();
        try {
            Event eventEntity = eventService.getEventById(eventId);
            int availablePlaces = eventService.calculateAvailablePlaces(eventId);

            // Conteneur large max 1200px
            VerticalLayout container = new VerticalLayout();
            container.setMaxWidth("1200px");
            container.setWidthFull();
            container.setPadding(true);
            container.setSpacing(true);

            // Construction du layout 2 colonnes
            HorizontalLayout splitLayout = createSplitLayout(eventEntity, availablePlaces);
            container.add(splitLayout);

            add(container);

        } catch (Exception e) {
            add(new H2("Événement introuvable"));
        }
    }

    private HorizontalLayout createSplitLayout(Event event, int availablePlaces) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        // Responsive : Wrap sur mobile
        layout.addClassName(LumoUtility.FlexWrap.WRAP);
        layout.addClassName(LumoUtility.Gap.XLARGE); // Grand espace entre les colonnes

        // --- COLONNE GAUCHE (Image + Titre + Desc) ---
        VerticalLayout leftCol = new VerticalLayout();
        leftCol.setPadding(false);
        leftCol.setSpacing(true);
        leftCol.setWidth("60%"); // Base desktop
        leftCol.setMinWidth("320px");
        leftCol.addClassName(LumoUtility.Flex.GROW);

        // 1. Image
        if (event.getImageUrl() != null) {
            Image img = new Image(event.getImageUrl(), "Cover");
            img.setWidthFull();
            img.setHeight("400px");
            img.getStyle().set("object-fit", "cover");
            img.addClassName(LumoUtility.BorderRadius.LARGE);
            leftCol.add(img);
        } else {
            // Fallback
            Div placeholder = new Div();
            placeholder.setWidthFull();
            placeholder.setHeight("400px");
            placeholder.getStyle().set("background", "#eee");
            placeholder.addClassName(LumoUtility.BorderRadius.LARGE);
            leftCol.add(placeholder);
        }

        // 2. Titre
        H2 title = new H2(event.getTitre());
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.Top.MEDIUM);

        // 3. Description
        Paragraph desc = new Paragraph(event.getDescription());
        desc.addClassName(LumoUtility.TextColor.SECONDARY);
        desc.getStyle().set("line-height", "1.6");

        leftCol.add(title, desc);

        // --- COLONNE DROITE (Card Détails) ---
        VerticalLayout rightCol = createBookingCard(event, availablePlaces);
        rightCol.setWidth("35%"); // Base desktop
        rightCol.setMinWidth("300px");
        rightCol.addClassName(LumoUtility.Flex.GROW);

        layout.add(leftCol, rightCol);
        return layout;
    }

    private VerticalLayout createBookingCard(Event event, int availablePlaces) {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL, // Ombre légère comme sur l'image
                LumoUtility.BorderRadius.MEDIUM,
                LumoUtility.Padding.LARGE,
                "booking-card" // Classe pour CSS spécifique si besoin
        );
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)"); // Bordure subtile

        // 1. Header (Prix + Badge)
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 price = new H2(PriceFormatter.format(event.getPrixUnitaire()));
        price.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.NONE);

        Span badge = new Span(event.getStatut() == EventStatus.PUBLIE ? "Actif" : "Inactif");
        badge.getElement().getThemeList().add("badge success pill"); // Vert et arrondi

        header.add(price, badge);

        // 2. Liste des détails (Icônes + Texte)
        VerticalLayout detailsList = new VerticalLayout();
        detailsList.setPadding(false);
        detailsList.setSpacing(true);
        detailsList.addClassName(LumoUtility.Margin.Vertical.LARGE);

        detailsList.add(createDetailRow(VaadinIcon.CALENDAR, event.getDateDebut().format(DATE_FMT)));
        detailsList.add(createDetailRow(VaadinIcon.CLOCK, event.getDateDebut().format(TIME_FMT)));
        detailsList.add(createDetailRow(VaadinIcon.MAP_MARKER, event.getLieu() + ", " + event.getVille()));
        detailsList.add(createDetailRow(VaadinIcon.TAG, event.getCategorie().getLabel()));

        // Places restantes (Icone user outline)
        String capacityText = availablePlaces + " places restantes sur " + event.getCapaciteMax();
        detailsList.add(createDetailRow(VaadinIcon.USER, capacityText));

        // 3. Bouton Action
        Button actionBtn = new Button("Réserver maintenant");
        actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        actionBtn.setWidthFull();

        // Logique bouton
        if (availablePlaces <= 0) {
            actionBtn.setText("Complet");
            actionBtn.setEnabled(false);
        } else {
            actionBtn.addClickListener(e -> {
                if(navigationManager.isAuthenticated()) {
                    navigationManager.goToReservation(event.getId());
                } else {
                    Notification.show("Connexion requise");
                    navigationManager.goToLogin();
                }
            });
        }

        // Texte d'aide sous le bouton
        Span helpText = new Span("Vous devrez vous connecter pour réserver");
        helpText.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.TextAlignment.CENTER);
        helpText.setWidthFull();

        // 4. Footer (Organisateur)
        Hr separator = new Hr();
        separator.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

        Span orgText = new Span();
        orgText.add(new Text("Organisé par "));
        Span orgName = new Span(event.getOrganisateur().getNomComplet());
        orgName.addClassName(LumoUtility.TextColor.HEADER); // Plus foncé
        orgText.add(orgName);
        orgText.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        card.add(header, detailsList, actionBtn, helpText, separator, orgText);
        return card;
    }

    // Helper pour créer les lignes avec icônes proprement
    private HorizontalLayout createDetailRow(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("18px"); // Taille modérée
        i.addClassName(LumoUtility.TextColor.SECONDARY); // Gris foncé

        Span s = new Span(text);
        s.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.TextColor.BODY);

        HorizontalLayout row = new HorizontalLayout(i, s);
        row.setAlignItems(Alignment.CENTER);
        row.setSpacing(true);
        return row;
    }
}