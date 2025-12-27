package ma.projet.events.ui.layout;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AuthLayout extends AppLayout {

    public AuthLayout() {

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        wrapper.getStyle()
                .set("background-color", "#f9fafb");

        // 🔑 AppLayout injectera automatiquement la vue ici
        setContent(wrapper);
    }
}
