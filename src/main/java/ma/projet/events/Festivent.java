package ma.projet.events;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot. autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@Theme("festivent")
@EnableScheduling
public class Festivent implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication. run(Festivent.class, args);
    }
}