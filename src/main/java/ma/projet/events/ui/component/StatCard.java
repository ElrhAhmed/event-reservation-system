package ma.projet. events.ui.component;

import com.vaadin.flow.component.html.Div;
import com. vaadin.flow.component.html. Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com. vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Card pour afficher une statistique avec icône, valeur et label
 *
 * Exemple d'utilisation :
 * new StatCard(VaadinIcon.TICKET. create(), "24", "Réservations", "var(--festivent-primary)")
 */
public class StatCard extends VerticalLayout {

    private final Span valueSpan;
    private final Span labelSpan;

    /**
     * Constructeur complet
     *
     * @param icon Icône de la stat
     * @param value Valeur à afficher (nombre, montant, etc.)
     * @param label Label de la stat
     * @param color Couleur de l'icône et de la valeur
     */
    public StatCard(Icon icon, String value, String label, String color) {
        // Style de la card
        addClassName("festivent-card");
        setSpacing(false);
        setPadding(true);
        setWidthFull();

        // Layout icône + valeur
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        // Icône
        icon.setSize("32px");
        icon.getStyle().set("color", color);

        // Valeur
        valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", color);

        header.add(icon, valueSpan);

        // Label
        labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)")
                .set("margin-top", "var(--lumo-space-s)");

        add(header, labelSpan);
    }

    /**
     * Constructeur simplifié (sans icône)
     */
    public StatCard(String value, String label, String color) {
        addClassName("festivent-card");
        setSpacing(false);
        setPadding(true);
        setWidthFull();

        // Valeur
        valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "700")
                .set("color", color);

        // Label
        labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--festivent-text-secondary)")
                .set("margin-top", "var(--lumo-space-s)");

        add(valueSpan, labelSpan);
    }

    /**
     * Met à jour la valeur affichée
     */
    public void updateValue(String newValue) {
        valueSpan.setText(newValue);
    }

    /**
     * Met à jour le label
     */
    public void updateLabel(String newLabel) {
        labelSpan.setText(newLabel);
    }
}