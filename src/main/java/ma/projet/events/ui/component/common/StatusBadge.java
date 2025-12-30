package ma.projet.events.ui.component.common;

import com.vaadin.flow.component.html.Span;

public class StatusBadge extends Span {

    public StatusBadge(String label, String colorHex) {
        setText(label);
        getStyle()
                .set("background-color", colorHex)
                .set("color", "#ffffff")
                .set("padding", "4px 10px")
                .set("border-radius", "12px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600")
                .set("white-space", "nowrap");
    }
}
