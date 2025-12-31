package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class StatCard extends Div {

    // On garde des références vers les textes pour pouvoir les modifier (setters)
    private final Span valSpan;
    private final Span subSpan;

    // Constructeur principal
    public StatCard(String title, String value, VaadinIcon icon, String subtitle, String colorHex) {
        addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.Gap.MEDIUM,
                LumoUtility.Position.RELATIVE,
                LumoUtility.Overflow.HIDDEN
        );

        // Bordure latérale colorée
        if(colorHex != null) {
            getStyle().set("border-left", "5px solid " + colorHex);
        }

        // 1. Icone (Dans un cercle coloré léger)
        Div iconBox = new Div();
        iconBox.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER,
                LumoUtility.BorderRadius.MEDIUM
        );
        iconBox.setWidth("48px");
        iconBox.setHeight("48px");

        if (colorHex != null) {
            iconBox.getStyle().set("background-color", colorHex + "20"); // 12% opacité
        }

        if (icon != null) {
            Icon i = icon.create();
            i.setSize("24px");
            if (colorHex != null) i.getStyle().set("color", colorHex);
            iconBox.add(i);
        }

        // 2. Textes
        VerticalLayout info = new VerticalLayout();
        info.setPadding(false);
        info.setSpacing(false);

        // Valeur (ex: "120")
        valSpan = new Span(value);
        valSpan.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.FontWeight.BOLD, LumoUtility.LineHeight.SMALL);

        // Titre (ex: "Réservations")
        Span lblSpan = new Span(title);
        lblSpan.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY, LumoUtility.FontWeight.MEDIUM);

        // Sous-titre (ex: "+5% cette semaine")
        subSpan = new Span(subtitle != null ? subtitle : "");
        subSpan.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.TERTIARY);
        subSpan.setVisible(subtitle != null && !subtitle.isEmpty());

        info.add(valSpan, lblSpan, subSpan);

        add(iconBox, info);
    }



    // Constructeur simple utilisé par certaines vues
    public StatCard(String title, String value) {
        this(title, value, null, null, null);
    }



    public void setValue(String value) {
        valSpan.setText(value);
    }

    public void setSubtitle(String subtitle) {
        subSpan.setText(subtitle);
        subSpan.setVisible(subtitle != null && !subtitle.isEmpty());
    }
}