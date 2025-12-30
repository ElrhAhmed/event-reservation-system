package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class ErrorPageContent extends VerticalLayout {

    public ErrorPageContent(String code, String titleText) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName(LumoUtility.Background.CONTRAST_5);

        VerticalLayout card = new VerticalLayout();
        card.setMaxWidth("500px");
        card.setWidth("90%");
        card.setPadding(true);
        card.setSpacing(true);
        card.setAlignItems(Alignment.CENTER);
        card.addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.LARGE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.Vertical.XLARGE,
                LumoUtility.Padding.Horizontal.LARGE,
                LumoUtility.TextAlignment.CENTER
        );

        Icon icon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE_O);
        icon.setSize("5em");
        icon.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

        H1 title = new H1(titleText);
        title.addClassNames(LumoUtility.FontSize.XXXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.HEADER);

        Paragraph description = new Paragraph("La page est indisponible ou vous n'avez pas la permission d'y accéder.");
        description.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY);

        if ("404".equals(code)) {
            icon.setColor("var(--lumo-primary-color)");
        } else {
            icon.setColor("var(--lumo-error-color)");
            title.addClassName(LumoUtility.TextColor.ERROR);
        }

        Button homeBtn = new Button("Retour à l'accueil", new Icon(VaadinIcon.ARROW_LEFT));
        homeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        homeBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        card.add(icon, title, description, homeBtn);
        add(card);
    }
}