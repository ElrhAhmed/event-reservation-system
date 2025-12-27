package ma.projet.events.config;

import ma.projet.events.entity.*;
import ma.projet.events. repository.*;
import org.springframework. boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Chargeur de données de test pour l'application EventReserve.
 * Crée des utilisateurs, événements et réservations au démarrage.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Vérifier si des données existent déjà
            if (userRepository.count() > 0) {
                System.out.println("✅ Base de données déjà initialisée");
                System.out.println("   - " + userRepository.count() + " utilisateurs");
                System.out.println("   - " + eventRepository.count() + " événements");
                System.out.println("   - " + reservationRepository.count() + " réservations");
                return;
            }

            System.out.println("🔄 Chargement des données de test...");

            // ============================================
            // 1. CRÉER DES UTILISATEURS
            // ============================================

            // Organisateur 1
            User organizer1 = new User();
            organizer1.setNom("Alami");
            organizer1.setPrenom("Youssef");
            organizer1.setEmail("youssef@events.ma");
            organizer1.setPassword(passwordEncoder.encode("password"));
            organizer1.setTelephone("0612345678");
            organizer1.setRole(Role.ORGANIZER);
            organizer1.setActif(true);
            organizer1 = userRepository.save(organizer1);

            // Organisateur 2
            User organizer2 = new User();
            organizer2.setNom("Bennani");
            organizer2.setPrenom("Fatima");
            organizer2.setEmail("fatima@events.ma");
            organizer2.setPassword(passwordEncoder.encode("password"));
            organizer2.setTelephone("0623456789");
            organizer2.setRole(Role.ORGANIZER);
            organizer2.setActif(true);
            organizer2 = userRepository. save(organizer2);

            // Client
            User client = new User();
            client.setNom("Client");
            client.setPrenom("Test");
            client.setEmail("client@test.ma");
            client.setPassword(passwordEncoder.encode("password"));
            client.setTelephone("0634567890");
            client.setRole(Role.CLIENT);
            client.setActif(true);
            client = userRepository.save(client);

            // Admin
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setEmail("admin@events.ma");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setTelephone("0645678901");
            admin.setRole(Role. ADMIN);
            admin.setActif(true);
            admin = userRepository.save(admin);

            System.out.println("   ✅ 4 utilisateurs créés");

            // ============================================
            // 2. CRÉER DES ÉVÉNEMENTS
            // ============================================

            // Événement 1:  Festival Gnaoua
            Event event1 = createEvent(
                    "Festival Gnaoua 2026",
                    "Le plus grand festival de musique Gnaoua et musiques du monde.  Une expérience unique mêlant tradition et modernité avec des artistes internationaux.",
                    EventCategory.CONCERT,
                    6, 3,
                    "Place Moulay Hassan", "Essaouira",
                    5000, 200.00,
                    "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800",
                    organizer1
            );
            event1 = eventRepository.save(event1);

            // Événement 2: TEDx Casablanca
            Event event2 = createEvent(
                    "TEDx Casablanca 2026",
                    "Ideas worth spreading - Une journée dédiée à l'innovation, l'entrepreneuriat et les idées qui changent le monde.  Intervenants inspirants et networking.",
                    EventCategory.CONFERENCE,
                    3, 9,
                    "Twin Center", "Casablanca",
                    500, 350.00,
                    "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=800",
                    organizer1
            );
            event2 = eventRepository.save(event2);

            // Événement 3: Marathon
            Event event3 = createEvent(
                    "Marathon de Casablanca 2026",
                    "Course internationale de 42km à travers la ville blanche. Parcours panoramique le long de la corniche avec vue sur l'océan Atlantique.",
                    EventCategory. SPORT,
                    4, 6,
                    "Corniche Ain Diab", "Casablanca",
                    3000, 150.00,
                    "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?w=800",
                    organizer2
            );
            event3 = eventRepository.save(event3);

            // Événement 4: Théâtre
            Event event4 = createEvent(
                    "Le Bourgeois Gentilhomme",
                    "Comédie-ballet de Molière dans une mise en scène moderne et innovante. Une représentation exceptionnelle avec des acteurs renommés.",
                    EventCategory. THEATRE,
                    2, 2,
                    "Théâtre Mohammed V", "Rabat",
                    600, 180.00,
                    "https://images.unsplash.com/photo-1503095396549-807759245b35?w=800",
                    organizer2
            );
            event4 = eventRepository.save(event4);

            // Événement 5: Festival Timitar
            Event event5 = createEvent(
                    "Festival Timitar 2026",
                    "Célébration de la culture amazighe avec musique, arts et gastronomie.  Trois jours de festivités avec des artistes locaux et internationaux.",
                    EventCategory.AUTRE,
                    7, 72,
                    "Place Al Mouahidine", "Agadir",
                    10000, 80.00,
                    "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=800",
                    organizer1
            );
            event5 = eventRepository.save(event5);

            // Événement 6: Concert Saad Lamjarred
            Event event6 = createEvent(
                    "Saad Lamjarred Live",
                    "Soirée exceptionnelle avec la star marocaine Saad Lamjarred. Un show spectaculaire avec les plus grands hits et des surprises exclusives.",
                    EventCategory. CONCERT,
                    5, 3,
                    "Stade Mohammed V", "Casablanca",
                    8000, 400.00,
                    "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800",
                    organizer2
            );
            event6 = eventRepository.save(event6);

            // Événement 7: Portes Ouvertes (GRATUIT)
            Event event7 = createEvent(
                    "Journée Portes Ouvertes - Arts",
                    "Découvrez les artistes locaux de Marrakech lors de cette journée gratuite. Expositions, ateliers et démonstrations en direct.",
                    EventCategory.AUTRE,
                    1, 9,
                    "Place Jemaa el-Fna", "Marrakech",
                    2000, 0.00,  // GRATUIT
                    "https://images.unsplash.com/photo-1518684079-3c830dcef090?w=800",
                    organizer1
            );
            event7 = eventRepository.save(event7);

            // Événement 8: Business Summit
            Event event8 = createEvent(
                    "Morocco Business Summit",
                    "Le rendez-vous annuel des décideurs économiques marocains.  Panels, workshops et opportunités de networking avec les leaders du secteur.",
                    EventCategory. CONFERENCE,
                    4, 24,
                    "Sofitel Rabat Jardin des Roses", "Rabat",
                    800, 500.00,
                    "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
                    organizer2
            );
            event8 = eventRepository.save(event8);

            // Événement 9: Derby
            Event event9 = createEvent(
                    "WAC vs Raja - Derby de Casablanca",
                    "Le derby le plus attendu de l'année !  Ambiance électrique garantie au stade Mohammed V pour ce choc historique du football marocain.",
                    EventCategory.SPORT,
                    3, 2,
                    "Stade Mohammed V", "Casablanca",
                    45000, 120.00,
                    "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800",
                    organizer1
            );
            event9 = eventRepository.save(event9);

            // Événement 10: Théâtre Tanger
            Event event10 = createEvent(
                    "La Visite de la Vieille Dame",
                    "Drame de Friedrich Dürrenmatt dans une adaptation marocaine. Une pièce bouleversante sur la justice et la morale.",
                    EventCategory. THEATRE,
                    2, 2,
                    "Institut Français de Tanger", "Tanger",
                    300, 100.00,
                    "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=800",
                    organizer2
            );
            event10 = eventRepository.save(event10);

            System.out.println("   ✅ 10 événements créés");

            // ============================================
            // 3. CRÉER DES RÉSERVATIONS
            // ============================================

            // Réservation 1
            Reservation res1 = new Reservation();
            res1.setCodeReservation("RES-2026-0001");
            res1.setNombrePlaces(2);
            res1.setMontantTotal(400.00);
            res1.setStatut(ReservationStatus.CONFIRMEE);
            res1.setCommentaire("Réservation client test");
            res1.setUtilisateur(client);
            res1.setEvenement(event1);
            reservationRepository.save(res1);

            // Réservation 2
            Reservation res2 = new Reservation();
            res2.setCodeReservation("RES-2026-0002");
            res2.setNombrePlaces(4);
            res2.setMontantTotal(1400.00);
            res2.setStatut(ReservationStatus.CONFIRMEE);
            res2.setCommentaire("Réservation groupe");
            res2.setUtilisateur(client);
            res2.setEvenement(event2);
            reservationRepository.save(res2);

            // Réservation 3
            Reservation res3 = new Reservation();
            res3.setCodeReservation("RES-2026-0003");
            res3.setNombrePlaces(1);
            res3.setMontantTotal(150.00);
            res3.setStatut(ReservationStatus.CONFIRMEE);
            res3.setUtilisateur(client);
            res3.setEvenement(event3);
            reservationRepository.save(res3);

            // Réservation 4 (EN ATTENTE)
            Reservation res4 = new Reservation();
            res4.setCodeReservation("RES-2026-0004");
            res4.setNombrePlaces(3);
            res4.setMontantTotal(540.00);
            res4.setStatut(ReservationStatus.EN_ATTENTE);
            res4.setCommentaire("En attente de confirmation");
            res4.setUtilisateur(client);
            res4.setEvenement(event4);
            reservationRepository.save(res4);

            // Réservation 5
            Reservation res5 = new Reservation();
            res5.setCodeReservation("RES-2026-0005");
            res5.setNombrePlaces(2);
            res5.setMontantTotal(160.00);
            res5.setStatut(ReservationStatus.CONFIRMEE);
            res5.setUtilisateur(client);
            res5.setEvenement(event5);
            reservationRepository.save(res5);

            System.out.println("   ✅ 5 réservations créées");

            // ============================================
            // RÉSUMÉ
            // ============================================
            System.out.println("\n✅ ========================================");
            System.out.println("✅ Données de test chargées avec succès!");
            System.out.println("✅ ========================================");
            System.out.println("\n📧 Comptes créés:");
            System.out.println("   - Organisateur 1: youssef@events.ma / password");
            System.out.println("   - Organisateur 2: fatima@events.ma / password");
            System.out.println("   - Client: client@test.ma / password");
            System.out.println("   - Admin: admin@events.ma / password");
            System.out.println("\n🎉 " + eventRepository.count() + " événements créés");
            System.out. println("🎫 " + reservationRepository.count() + " réservations créées");
            System.out.println("👥 " + userRepository.count() + " utilisateurs créés");
            System.out.println("\n✅ ========================================\n");
        };
    }

    /**
     * Méthode utilitaire pour créer un événement.
     */
    private Event createEvent(
            String titre, String description, EventCategory categorie,
            int monthsFromNow, int durationHours,
            String lieu, String ville,
            int capacite, double prix, String imageUrl,
            User organisateur
    ) {
        Event event = new Event();
        event.setTitre(titre);
        event.setDescription(description);
        event.setCategorie(categorie);

        // Dates futures dynamiques
        LocalDateTime dateDebut = LocalDateTime.now().plusMonths(monthsFromNow);
        LocalDateTime dateFin = dateDebut.plusHours(durationHours);

        event.setDateDebut(dateDebut);
        event.setDateFin(dateFin);
        event.setLieu(lieu);
        event.setVille(ville);
        event.setCapaciteMax(capacite);
        event.setPrixUnitaire(prix);
        event.setImageUrl(imageUrl);
        event.setStatut(EventStatus.PUBLIE);  // ✅ IMPORTANT
        event.setOrganisateur(organisateur);

        return event;
    }
}