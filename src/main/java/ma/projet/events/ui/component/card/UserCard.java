package ma.projet.events.ui.component.card;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.ui.component.common.StatusBadge;
import ma.projet.events.ui.util.DateFormatter;

public class UserCard extends Div {

    public UserCard(User user) {
        addClassName("user-card");

        // Nom + Email
        H4 name = new H4(user.getNomComplet());
        Span email = new Span(user.getEmail());
        email.addClassName("user-email");

        // Badge rôle
        StatusBadge roleBadge = createRoleBadge(user.getRole());

        // Badge statut
        StatusBadge statusBadge = user.isActif()
                ? new StatusBadge("Actif", "#28A745")
                : new StatusBadge("Inactif", "#DC3545");

        HorizontalLayout badges = new HorizontalLayout(roleBadge, statusBadge);

        // Date inscription
        Span dateInscription = new Span(
                "Inscrit le " + DateFormatter.format(user.getDateInscription())
        );
        dateInscription.addClassName("user-date");

        VerticalLayout content = new VerticalLayout(
                name,
                email,
                badges,
                dateInscription
        );

        content.setPadding(false);
        content.setSpacing(false);

        add(content);
    }

    private StatusBadge createRoleBadge(Role role) {
        return switch (role) {
            case ADMIN -> new StatusBadge("Admin", "#DC3545");
            case ORGANIZER -> new StatusBadge("Organisateur", "#0D6EFD");
            case CLIENT -> new StatusBadge("Client", "#0DCAF0");
        };
    }
}
