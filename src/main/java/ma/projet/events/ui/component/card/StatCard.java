package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class StatCard extends Div {

    private final Span titleSpan = new Span();
    private final Span valueSpan = new Span();
    private final Span subtitleSpan = new Span();
    private Icon icon;

    public StatCard(String title, String value) {
        this(title, value, null, null, null);
    }

    public StatCard(
            String title,
            String value,
            VaadinIcon iconType,
            String subtitle,
            String themeColor
    ) {
        addClassName("stat-card");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName("stat-card-content");

        if (iconType != null) {
            icon = iconType.create();
            icon.addClassName("stat-card-icon");
            content.add(icon);
        }

        titleSpan.setText(title);
        titleSpan.addClassName("stat-card-title");

        valueSpan.setText(value);
        valueSpan.addClassName("stat-card-value");

        content.add(titleSpan, valueSpan);

        if (subtitle != null) {
            subtitleSpan.setText(subtitle);
            subtitleSpan.addClassName("stat-card-subtitle");
            content.add(subtitleSpan);
        }

        if (themeColor != null) {
            getStyle().set("--stat-color", themeColor);
        }

        add(content);
    }

    /* =========================
       SETTERS DYNAMIQUES
       ========================= */

    public void setValue(String value) {
        valueSpan.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subtitleSpan.setText(subtitle);
        if (!getChildren().anyMatch(c -> c == subtitleSpan)) {
            add(subtitleSpan);
        }
    }

    public void setIcon(VaadinIcon iconType) {
        if (icon != null) {
            remove(icon);
        }
        icon = iconType.create();
        icon.addClassName("stat-card-icon");
        getChildren().findFirst().ifPresent(c -> ((Component) c).getElement().insertChild(0, icon.getElement()));
    }
}
