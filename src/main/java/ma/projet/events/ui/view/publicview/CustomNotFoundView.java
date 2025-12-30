package ma.projet.events.ui.view.publicview;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

@Tag(Tag.DIV)
@AnonymousAllowed
// Extends RouteNotFoundError : C'est la clé pour écraser la page "Available routes" de Vaadin
public class CustomNotFoundView extends RouteNotFoundError {

    public CustomNotFoundView() {
        // On délègue l'affichage à notre composant visuel commun
        getElement().appendChild(new ErrorPageContent("404", "Page introuvable").getElement());
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        return HttpServletResponse.SC_NOT_FOUND;
    }
}