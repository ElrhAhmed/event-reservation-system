package ma.projet.events.ui. component;

import com.vaadin.flow.component.button.Button;
import com.vaadin. flow.component.button.ButtonVariant;
import com.vaadin.flow.component. dialog.Dialog;
import com. vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html. Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Dialog de confirmation personnalisé avec style Festivent
 *
 * Exemple d'utilisation :
 * FestiventConfirmDialog.show(
 *     "Supprimer l'événement",
 *     "Êtes-vous sûr de vouloir supprimer cet événement ?  Cette action est irréversible.",
 *     "Supprimer",
 *     () -> deleteEvent()
 * );
 */
public class FestiventConfirmDialog extends Dialog {

    /**
     * Constructeur complet
     */
    public FestiventConfirmDialog(String title, String message, String confirmButtonText, Runnable onConfirm) {
        // Configuration du dialog
        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("450px");

        // Titre
        H3 titleHeader = new H3(title);
        titleHeader.getStyle()
                .set("margin", "0")
                .set("color", "var(--festivent-text-primary)");

        // Message
        Paragraph messageParagraph = new Paragraph(message);
        messageParagraph.getStyle()
                .set("color", "var(--festivent-text-secondary)");

        // Boutons
        Button cancelButton = new Button("Annuler", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmButton = new Button(confirmButtonText, e -> {
            onConfirm.run();
            close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, confirmButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        buttons.getStyle().set("margin-top", "var(--lumo-space-l)");

        // Layout du dialog
        VerticalLayout layout = new VerticalLayout(titleHeader, messageParagraph, buttons);
        layout.setPadding(true);
        layout.setSpacing(true);

        add(layout);
    }

    /**
     * Méthode statique pour afficher rapidement un dialog
     */
    public static void show(String title, String message, String confirmButtonText, Runnable onConfirm) {
        FestiventConfirmDialog dialog = new FestiventConfirmDialog(title, message, confirmButtonText, onConfirm);
        dialog.open();
    }

    /**
     * Variante pour suppression (bouton rouge par défaut)
     */
    public static void showDelete(String title, String message, Runnable onConfirm) {
        show(title, message, "Supprimer", onConfirm);
    }
}