package ma.projet.events.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public final class VaadinUtils {

    private VaadinUtils() {
        // util class
    }

    /* =========================
       TITRES
    ========================= */

    public static H1 createPageTitle(String text) {
        H1 title = new H1(text);
        title.getStyle()
                .set("margin", "0 0 1rem 0");
        return title;
    }

    public static H2 createSectionTitle(String text) {
        H2 title = new H2(text);
        title.getStyle()
                .set("margin", "1.5rem 0 1rem 0");
        return title;
    }

    /* =========================
       LAYOUTS
    ========================= */

    public static VerticalLayout createCenteredLayout(Component... components) {
        VerticalLayout layout = new VerticalLayout(components);
        layout.setAlignItems(VerticalLayout.Alignment.CENTER);
        layout.setJustifyContentMode(VerticalLayout.JustifyContentMode.CENTER);
        layout.setSizeFull();
        return layout;
    }

    public static HorizontalLayout createHorizontal(Component... components) {
        HorizontalLayout layout = new HorizontalLayout(components);
        layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        layout.setSpacing(true);
        return layout;
    }

    /* =========================
       STYLES HELPERS
    ========================= */

    public static void applyCardStyle(Component component) {
        component.getElement().getClassList().add("fest-card");
    }
}
