package ma.projet.events.config;

import ma.projet.events.entity.*;
import ma.projet.events.repository. EventRepository;
import ma.projet. events.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * Initialise des données de test au démarrage de l'application
 * Version CORRIGÉE selon les entités réelles
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, EventRepository eventRepository) {
        return args -> {
            // Vérifier si des données existent déjà
            if (userRepository.count() > 0) {
                System.out.println("✅ Données déjà présentes, pas d'initialisation");
                return;
            }

            System.out.println("🔄 Initialisation des données de test...");

            // ========== UTILISATEURS ==========

            // Organisateur 1
            User organizer1 = new User();
            organizer1.setNom("Benali");
            organizer1.setPrenom("Ahmed");
            organizer1.setEmail("ahmed.benali@festivent.ma");
            organizer1.setPassword("Password123!");  // ← Mot de passe temporaire (min 8 car)
            organizer1.setRole(Role.ORGANIZER);
            organizer1.setActif(true);
            organizer1.setTelephone("0612345678");
            userRepository.save(organizer1);

            // Organisateur 2
            User organizer2 = new User();
            organizer2.setNom("Alaoui");
            organizer2.setPrenom("Fatima");
            organizer2.setEmail("fatima.alaoui@festivent.ma");
            organizer2.setPassword("Password123!");
            organizer2.setRole(Role.ORGANIZER);
            organizer2.setActif(true);
            organizer2.setTelephone("0698765432");
            userRepository. save(organizer2);

            // Client 1
            User client1 = new User();
            client1.setNom("Idrissi");
            client1.setPrenom("Youssef");
            client1.setEmail("youssef.idrissi@gmail.com");
            client1.setPassword("Password123!");
            client1.setRole(Role. CLIENT);
            client1.setActif(true);
            client1.setTelephone("0655443322");
            userRepository.save(client1);

            // Admin
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setEmail("admin@festivent.ma");
            admin.setPassword("Admin123!");
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            userRepository.save(admin);

            // ========== ÉVÉNEMENTS ==========

            // Événement 1 : Concert Jazz
            Event event1 = new Event();
            event1.setTitre("Festival Jazz à Rabat");
            event1.setDescription("Venez découvrir les meilleurs artistes de jazz du Maroc et d'ailleurs dans un cadre exceptionnel.  Soirée musicale inoubliable avec des performances live de groupes reconnus internationalement.");
            event1.setCategorie(EventCategory.CONCERT);
            event1.setDateDebut(LocalDateTime.now().plusDays(15));
            event1.setDateFin(LocalDateTime.now().plusDays(15).plusHours(4));
            event1.setLieu("Théâtre Mohammed V");
            event1.setVille("Rabat");
            event1.setPrixUnitaire(250.0);
            event1.setCapaciteMax(500);
            event1.setStatut(EventStatus.PUBLIE);
            event1.setOrganisateur(organizer1);
            event1.setImageUrl(null);
            eventRepository.save(event1);

            // Événement 2 : Conférence Tech
            Event event2 = new Event();
            event2.setTitre("Conférence Tech Innovation 2025");
            event2.setDescription("Conférence internationale sur les nouvelles technologies et l'innovation.  Intervenants de renommée mondiale, ateliers pratiques et networking.  Thèmes :  IA, Blockchain, IoT, Cybersécurité.");
            event2.setCategorie(EventCategory. CONFERENCE);
            event2.setDateDebut(LocalDateTime. now().plusDays(30));
            event2.setDateFin(LocalDateTime.now().plusDays(30).plusHours(8));
            event2.setLieu("Sofitel Casablanca Tour Blanche");
            event2.setVille("Casablanca");
            event2.setPrixUnitaire(450.0);
            event2.setCapaciteMax(300);
            event2.setStatut(EventStatus.PUBLIE);
            event2.setOrganisateur(organizer2);
            event2.setImageUrl(null);
            eventRepository.save(event2);

            // Événement 3 :  Théâtre
            Event event3 = new Event();
            event3.setTitre("Spectacle de théâtre :  Le Bourgeois Gentilhomme");
            event3.setDescription("Adaptation moderne de la célèbre pièce de Molière par la troupe nationale. Une comédie brillante qui fait rire et réfléchir.  Mise en scène innovante avec des décors somptueux.");
            event3.setCategorie(EventCategory. THEATRE);
            event3.setDateDebut(LocalDateTime. now().plusDays(20));
            event3.setDateFin(LocalDateTime.now().plusDays(20).plusHours(2));
            event3.setLieu("Théâtre National Mohammed V");
            event3.setVille("Rabat");
            event3.setPrixUnitaire(150.0);
            event3.setCapaciteMax(400);
            event3.setStatut(EventStatus.PUBLIE);
            event3.setOrganisateur(organizer1);
            event3.setImageUrl(null);
            eventRepository.save(event3);

            // Événement 4 : Sport
            Event event4 = new Event();
            event4.setTitre("Match de Football : WAC vs Raja");
            event4.setTitre("Match de Football : WAC vs Raja");
            event4.setDescription("Derby historique de Casablanca.  Ambiance garantie dans le stade Mohamed V rempli de supporters passionnés. Venez encourager votre équipe favorite !");
            event4.setCategorie(EventCategory.SPORT);
            event4.setDateDebut(LocalDateTime.now().plusDays(10));
            event4.setDateFin(LocalDateTime.now().plusDays(10).plusHours(2));
            event4.setLieu("Stade Mohamed V");
            event4.setVille("Casablanca");
            event4.setPrixUnitaire(100.0);
            event4.setCapaciteMax(8000);
            event4.setStatut(EventStatus.PUBLIE);
            event4.setOrganisateur(organizer2);
            event4.setImageUrl(null);
            eventRepository.save(event4);

            // Événement 5 :  Autre (Festival culturel)
            Event event5 = new Event();
            event5.setTitre("Festival des Arts de la Rue");
            event5.setDescription("Trois jours de spectacles gratuits dans les rues de Marrakech. Jongleurs, acrobates, musiciens et artistes du monde entier. Animations pour toute la famille.");
            event5.setCategorie(EventCategory. AUTRE);
            event5.setDateDebut(LocalDateTime.now().plusDays(25));
            event5.setDateFin(LocalDateTime.now().plusDays(27));
            event5.setLieu("Place Jemaa el-Fna");
            event5.setVille("Marrakech");
            event5.setPrixUnitaire(0.0);  // Gratuit
            event5.setCapaciteMax(5000);
            event5.setStatut(EventStatus.PUBLIE);
            event5.setOrganisateur(organizer1);
            event5.setImageUrl(null);
            eventRepository.save(event5);

            // Événement 6 : Concert (variété)
            Event event6 = new Event();
            event6.setTitre("Concert Saad Lamjarred - Tournée Nationale");
            event6.setDescription("Le célèbre artiste marocain Saad Lamjarred revient sur scène avec son nouveau spectacle. Plus de 2 heures de show avec ses plus grands tubes et des surprises inédites.");
            event6.setCategorie(EventCategory. CONCERT);
            event6.setDateDebut(LocalDateTime.now().plusDays(40));
            event6.setDateFin(LocalDateTime.now().plusDays(40).plusHours(3));
            event6.setLieu("Complexe Mohammed V");
            event6.setVille("Rabat");
            event6.setPrixUnitaire(300.0);
            event6.setCapaciteMax(12000);
            event6.setStatut(EventStatus.PUBLIE);
            event6.setOrganisateur(organizer2);
            event6.setImageUrl(null);
            eventRepository.save(event6);

            System.out.println("✅ Données initialisées avec succès !");
            System.out.println("   - " + userRepository.count() + " utilisateurs créés");
            System.out. println("   - " + eventRepository.count() + " événements créés");
            System.out. println("⚠️  Mots de passe temporaires (seront encodés en Phase 10)");
        };
    }
}