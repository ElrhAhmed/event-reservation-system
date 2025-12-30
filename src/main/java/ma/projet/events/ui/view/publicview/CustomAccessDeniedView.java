package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

@Tag(Tag.DIV)
@AnonymousAllowed
// Cible spécifiquement l'exception de sécurité pour prendre la priorité
public class CustomAccessDeniedView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

    public CustomAccessDeniedView() {
        setSizeFull(); // Important pour le centrage
        setPadding(false);
        setSpacing(false);
        // On délègue l'affichage
        add(new ErrorPageContent("403", "Accès refusé"));
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        return HttpServletResponse.SC_FORBIDDEN;
    }
}