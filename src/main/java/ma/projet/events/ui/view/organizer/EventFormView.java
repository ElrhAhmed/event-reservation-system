package ma.projet.events.ui.view.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.entity.User;
import ma.projet.events.security.SecurityService;
import ma.projet.events.service.EventService;
import ma.projet.events.service.UserService;
import ma.projet.events.ui.component.common.ConfirmDialogUtil;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.component.form.EventForm;
import ma.projet.events.ui.layout.OrganizerLayout;
import ma.projet.events.ui.navigation.NavigationManager;
import ma.projet.events.ui.util.DateFormatter;

import java.util.Optional;

@Route(value = "organizer/event/edit/:eventID", layout = OrganizerLayout.class)
@RouteAlias(value = "organizer/event/new", layout = OrganizerLayout.class)
@PageTitle("Éditer Événement | FESTIVENT")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class EventFormView extends VerticalLayout implements BeforeEnterObserver {

    private final EventService eventService;
    private final UserService userService;
    private final SecurityService securityService;
    private final NavigationManager navigationManager;

    private final EventForm form;
    private Event currentEvent;
    private boolean isNew = true;

    public EventFormView(EventService eventService,
                         UserService userService,
                         SecurityService securityService,
                         NavigationManager navigationManager) {
        this.eventService = eventService;
        this.userService = userService;
        this.securityService = securityService;
        this.navigationManager = navigationManager;

        // Configuration de la page (Fond gris, centrage)
        addClassName(LumoUtility.Background.CONTRAST_5);
        setSizeFull();
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // --- CARTE DU FORMULAIRE ---
        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("800px"); // Largeur contrôlée
        card.setWidthFull();
        card.setPadding(true);
        card.setSpacing(true);

        // Style Carte
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.MEDIUM,
                LumoUtility.BorderRadius.LARGE
        );

        // Header dans la carte
        H2 title = new H2("Détails de l'événement");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Margin.Bottom.NONE, LumoUtility.Margin.Top.NONE);

        Span subtitle = new Span("Remplissez les informations ci-dessous pour créer ou modifier votre événement.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        // Formulaire
        form = new EventForm();
        form.addClassName(LumoUtility.Padding.Horizontal.NONE); // Enlève le padding latéral interne

        // Listeners
        form.addSaveDraftListener(this::saveDraft);
        form.addPublishListener(this::publish);
        form.addPreviewListener(this::preview);
        form.addCancelListener(e -> navigationManager.goToMyEvents());

        card.add(title, subtitle, new Hr(), form);

        // On rend la carte scrollable si l'écran est trop petit en hauteur
        card.getStyle().set("overflow-y", "auto");

        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> eventIdParam = event.getRouteParameters().get("eventID");

        if (eventIdParam.isPresent()) {
            // MODE ÉDITION
            try {
                Long id = Long.parseLong(eventIdParam.get());
                currentEvent = eventService.getEventById(id);

                // Sécurité : Vérifier que l'événement appartient bien à l'organisateur connecté
                User user = getCurrentUser();
                if (!currentEvent.getOrganisateur().getId().equals(user.getId()) && !user.isAdmin()) {
                    showError("Accès refusé : Cet événement ne vous appartient pas.");
                    event.forwardTo("organizer/events");
                    return;
                }

                isNew = false;
            } catch (Exception e) {
                showError("Événement introuvable");
                event.forwardTo("organizer/events");
                return;
            }
        } else {
            // MODE CRÉATION
            currentEvent = new Event();
            currentEvent.setStatut(EventStatus.BROUILLON);
            isNew = true;
        }

        form.setEvent(currentEvent);
    }

    private User getCurrentUser() {
        var userDetails = securityService.getAuthenticatedUser();
        return userService.getUserByEmail(userDetails.getUsername());
    }

    // --- LOGIQUE MÉTIER ---

    private void saveDraft(EventForm.SaveDraftEvent event) {
        try {
            User user = getCurrentUser();
            if (isNew) {
                eventService.createEvent(event.getEvent(), user.getId());
                showSuccess("Brouillon créé avec succès");
            } else {
                eventService.updateEvent(currentEvent.getId(), event.getEvent(), user.getId());
                showSuccess("Modifications enregistrées");
            }
            navigationManager.goToMyEvents();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void publish(EventForm.PublishEvent event) {
        // Validation stricte UI via le Binder
        if (!form.writeBeanIfValid(currentEvent)) {
            showError("Veuillez remplir correctement tous les champs obligatoires avant de publier.");
            return;
        }

        ConfirmDialogUtil.show("Confirmer la publication ?",
                "L'événement sera immédiatement visible par les clients.", () -> {
                    try {
                        User user = getCurrentUser();

                        // 1. Sauvegarde d'abord
                        Event savedEvent;
                        if (isNew) {
                            savedEvent = eventService.createEvent(currentEvent, user.getId());
                        } else {
                            savedEvent = eventService.updateEvent(currentEvent.getId(), currentEvent, user.getId());
                        }

                        // 2. Publication
                        eventService.publishEvent(savedEvent.getId(), user.getId());

                        showSuccess("Événement publié en ligne !");
                        navigationManager.goToMyEvents();

                    } catch (Exception e) {
                        showError("Impossible de publier : " + e.getMessage());
                    }
                });
    }

    private void preview(EventForm.PreviewEvent event) {
        // On récupère les données "dirty" du formulaire pour l'aperçu
        Event previewEvent = new Event();
        form.writeBeanIfValid(previewEvent);
        previewEvent.setStatut(currentEvent.getStatut() != null ? currentEvent.getStatut() : EventStatus.BROUILLON);

        showPreviewDialog(previewEvent);
    }

    // --- DIALOGUE APERÇU ---
    private void showPreviewDialog(Event event) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Prévisualisation");
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(false);

        if (event.getImageUrl() != null && !event.getImageUrl().isBlank()) {
            Image image = new Image(event.getImageUrl(), "Cover");
            image.setWidthFull();
            image.setHeight("200px");
            image.getStyle().set("object-fit", "cover");
            image.addClassName(LumoUtility.BorderRadius.MEDIUM);
            content.add(image);
        }

        H3 title = new H3(event.getTitre() != null ? event.getTitre() : "Sans titre");
        StatusBadge badge = new StatusBadge(event.getStatut().getLabel(), event.getStatut().getColor());

        HorizontalLayout details = new HorizontalLayout(
                createDetailItem(VaadinIcon.CALENDAR, DateFormatter.format(event.getDateDebut())),
                createDetailItem(VaadinIcon.MAP_MARKER, event.getVille())
        );
        details.setSpacing(true);

        Paragraph desc = new Paragraph(event.getDescription() != null ? event.getDescription() : "Aucune description");
        desc.addClassName(LumoUtility.TextColor.SECONDARY);

        content.add(title, badge, details, desc);

        Button close = new Button("Fermer", e -> dialog.close());
        dialog.getFooter().add(close);
        dialog.add(content);
        dialog.open();
    }

    private HorizontalLayout createDetailItem(VaadinIcon icon, String text) {
        Icon i = icon.create();
        i.setSize("18px");
        i.addClassName(LumoUtility.TextColor.SECONDARY);
        return new HorizontalLayout(i, new Span(text != null ? text : "--"));
    }

    private void showSuccess(String msg) {
        Notification.show(msg, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String msg) {
        Notification.show(msg, 5000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}