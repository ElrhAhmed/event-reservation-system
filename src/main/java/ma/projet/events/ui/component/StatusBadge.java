package ma.projet.events.ui.component;

import com.vaadin.flow.component.html. Span;
import ma.projet.events.entity.EventStatus;
import ma.projet.events.entity.ReservationStatus;

/**
 * Badge coloré pour afficher un statut (Event ou Reservation)
 * Utilise les couleurs définies dans les enums et le thème CSS
 *
 * Exemples d'utilisation :
 * - new StatusBadge(EventStatus. PUBLIE)
 * - new StatusBadge(ReservationStatus. CONFIRMEE)
 */
public class StatusBadge extends Span {

    /**
     * Constructeur pour EventStatus
     */
    public StatusBadge(EventStatus status) {
        setText(status.getLabel());
        applyEventStatusStyle(status);
    }

    /**
     * Constructeur pour ReservationStatus
     */
    public StatusBadge(ReservationStatus status) {
        setText(status.getLabel());
        applyReservationStatusStyle(status);
    }

    /**
     * Applique le style selon le statut de l'événement
     */
    private void applyEventStatusStyle(EventStatus status) {
        // Style de base
        getStyle()
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "600")
                .set("display", "inline-block")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");

        // Couleur selon le statut
        switch (status) {
            case BROUILLON -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-brouillon)")
                        .set("color", "white");
            }
            case PUBLIE -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-publie)")
                        . set("color", "white");
            }
            case ANNULE -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-annule)")
                        .set("color", "white");
            }
            case TERMINE -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-termine)")
                        . set("color", "white");
            }
        }
    }

    /**
     * Applique le style selon le statut de la réservation
     */
    private void applyReservationStatusStyle(ReservationStatus status) {
        // Style de base
        getStyle()
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("font-weight", "600")
                .set("display", "inline-block")
                .set("text-transform", "uppercase")
                .set("letter-spacing", "0.5px");

        // Couleur selon le statut
        switch (status) {
            case EN_ATTENTE -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-attente)")
                        .set("color", "white");
            }
            case CONFIRMEE -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-confirmee)")
                        .set("color", "white");
            }
            case ANNULEE -> {
                getStyle()
                        .set("background-color", "var(--festivent-status-annulee)")
                        .set("color", "white");
            }
        }
    }
}