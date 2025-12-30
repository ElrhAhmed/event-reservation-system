package ma.projet.events.config;

import ma.projet.events.entity.*;
import ma.projet.events.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (userRepository.count() > 0) {
                System.out.println("⚠️ Données déjà présentes — initialisation ignorée.");
                return;
            }

            System.out.println("🚀 Initialisation FESTIVENT avec images...");

            /* ===================== USERS ===================== */

            User admin = createUser("Admin", "System", "admin@event.ma", "admin123", Role.ADMIN, passwordEncoder);
            User org1  = createUser("Organisateur", "Alpha", "organizer1@event.ma", "org123", Role.ORGANIZER, passwordEncoder);
            User org2  = createUser("Organisateur", "Beta", "organizer2@event.ma", "org123", Role.ORGANIZER, passwordEncoder);
            User c1    = createUser("Client", "One", "client1@event.ma", "client123", Role.CLIENT, passwordEncoder);
            User c2    = createUser("Client", "Two", "client2@event.ma", "client2@event.ma", Role.CLIENT, passwordEncoder);

            userRepository.saveAll(List.of(admin, org1, org2, c1, c2));

            /* ===================== EVENTS ===================== */

            List<String> villes = List.of("Casablanca", "Rabat", "Marrakech", "Tanger", "Fès");

            List<EventCategory> categories = List.of(
                    EventCategory.CONCERT,
                    EventCategory.THEATRE,
                    EventCategory.CONFERENCE,
                    EventCategory.SPORT,
                    EventCategory.AUTRE
            );

            List<EventStatus> statuts = List.of(
                    EventStatus.BROUILLON,
                    EventStatus.PUBLIE,
                    EventStatus.ANNULE,
                    EventStatus.TERMINE
            );

            List<Event> events = new ArrayList<>();
            int index = 1;

            for (EventCategory cat : categories) {
                for (int i = 0; i < 3; i++) {

                    Event e = new Event();
                    e.setTitre(cat.name() + " Event " + index);
                    e.setDescription("Description de test pour " + cat.name());
                    e.setCategorie(cat);
                    e.setVille(villes.get(index % villes.size()));
                    e.setLieu("Lieu " + index);
                    e.setCapaciteMax(100 + index * 20);
                    e.setPrixUnitaire((double) (50 + (index * 25) % 450));
                    e.setStatut(statuts.get(index % statuts.size()));
                    e.setDateDebut(LocalDateTime.now().plusDays(index * 3));
                    e.setDateFin(LocalDateTime.now().plusDays(index * 3).plusHours(4));
                    e.setOrganisateur(index % 2 == 0 ? org1 : org2);

                    // 🎯 IMAGE SELON CATÉGORIE
                    e.setImageUrl(getImageForCategory(cat));

                    events.add(e);
                    index++;
                }
            }

            eventRepository.saveAll(events);

            /* ===================== RESERVATIONS ===================== */

            Random random = new Random();
            List<Reservation> reservations = new ArrayList<>();

            for (int i = 1; i <= 20; i++) {

                Event evt = events.get(random.nextInt(events.size()));
                User user = (i % 2 == 0) ? c1 : c2;

                Reservation r = new Reservation();
                r.setEvenement(evt);
                r.setUtilisateur(user);
                r.setNombrePlaces(1 + random.nextInt(10));
                r.setStatut(ReservationStatus.values()[i % ReservationStatus.values().length]);
                r.setDateReservation(LocalDateTime.now().minusDays(random.nextInt(30)));
                r.setMontantTotal(r.getNombrePlaces() * evt.getPrixUnitaire());
                r.setCodeReservation("RES-" + String.format("%04d", i));

                reservations.add(r);
            }

            reservationRepository.saveAll(reservations);

            System.out.println("✅ Initialisation terminée avec images !");
        };
    }

    /* ===================== UTILS ===================== */

    private User createUser(String nom, String prenom, String email,
                            String rawPassword, Role role, PasswordEncoder encoder) {
        User u = new User();
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setPassword(encoder.encode(rawPassword));
        u.setRole(role);
        u.setActif(true);
        u.setDateInscription(LocalDateTime.now());
        return u;
    }

    private String getImageForCategory(EventCategory category) {
        return switch (category) {
            case CONCERT -> "/images/events/concert.jpg";
            case THEATRE -> "/images/events/theatre.jpg";
            case CONFERENCE -> "/images/events/conference.jpg";
            case SPORT -> "/images/events/sport.jpg";
            case AUTRE -> "/images/events/autre.jpg";
        };
    }
}
