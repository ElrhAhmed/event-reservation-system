package ma.projet.events.config;

import ma.projet.events.entity.*;
import ma.projet.events.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public DataInit(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("⏳ DÉBUT DU TEST ULTIME...");

        // 1. Essai de création d'un utilisateur
        User u = new User();
        u.setNom("Test");
        u.setPrenom("Admin");
        u.setEmail("test@test.com");
        u.setPassword("12345678");
        u.setRole(Role.ADMIN);

        userRepository.save(u);
        System.out.println("✅ Utilisateur sauvegardé avec l'ID : " + u.getId());

        // 2. Essai de création d'un événement lié à cet utilisateur
        Event e = new Event();
        e.setTitre("Mon Concert Test");
        e.setDateDebut(LocalDateTime.now().plusDays(10));
        e.setDateFin(LocalDateTime.now().plusDays(11));
        e.setLieu("Salle de test");
        e.setVille("Casablanca"); // Ajout du champ ville requis
        e.setCapaciteMax(100);
        e.setPrixUnitaire(50.0); // Ajout du prix requis
        e.setCategorie(EventCategory.CONCERT);
        e.setOrganisateur(u); // Relation User -> Event

        eventRepository.save(e);
        System.out.println("✅ Événement sauvegardé avec l'ID : " + e.getId());

        System.out.println("🎉 TOUT EST OPÉRATIONNEL !");
    }
}