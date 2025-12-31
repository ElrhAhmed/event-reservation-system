package ma.projet.events;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot. autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entrée de l'application Festivent
 *
 * @Theme active le thème personnalisé dans frontend/themes/festivent/
 * @EnableScheduling active les jobs planifiés (EventService. checkAndMarkFinishedEvents)
 */
@SpringBootApplication
@Theme(value = "festivent")
@EnableScheduling
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication. run(Festivent.class, args);
    }
}