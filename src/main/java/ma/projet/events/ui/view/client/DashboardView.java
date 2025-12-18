package ma.projet.events.ui.view.client;

import com.vaadin.flow.component. html.H2;
import com.vaadin.flow.component. html.Paragraph;
import com.vaadin.flow.component. orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import ma.projet.events.ui.layout.MainLayout;

/**
 * Tableau de bord CLIENT (version test Phase 2)
 * Sera remplacé par le vrai Dashboard en Phase 5
 */
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Tableau de bord");
        title.getStyle().set("color", "var(--festivent-primary)");

        Paragraph description = new Paragraph(
                "Bienvenue sur votre espace personnel Festivent"
        );

        add(title, description);
    }
}