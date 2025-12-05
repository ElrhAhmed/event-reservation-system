package ma.projet.events.config;

import ma.projet.events.entity.*;
import ma.projet.events.repository.EventRepository;
import ma.projet.events.repository.ReservationRepository;
import ma.projet.events.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // Important pour le hash
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInit(UserRepository userRepository,
                    EventRepository eventRepository,
                    ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        // Si la base est déjà remplie, on ne fait rien
        if (userRepository.count() > 0) return;

        System.out.println("⏳ DÉBUT DU CHARGEMENT DES DONNÉES DE TEST...");

        // --- 1. CRÉATION DES UTILISATEURS (5 min) ---
        // Admin
        User admin = new User();
        admin.setNom("Admin"); admin.setPrenom("Super");
        admin.setEmail("admin@event.ma");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        // Organizers
        User org1 = createUser("Organizer", "Un", "organizer1@event.ma", Role.ORGANIZER);
        User org2 = createUser("Organizer", "Deux", "organizer2@event.ma", Role.ORGANIZER);

        // Clients
        User client1 = createUser("Client", "Un", "client1@event.ma", Role.CLIENT);
        User client2 = createUser("Client", "Deux", "client2@event.ma", Role.CLIENT);

        System.out.println("✅ Utilisateurs créés");

        // --- 2. CRÉATION DES ÉVÉNEMENTS (15 min) ---
        List<String> villes = Arrays.asList("Casablanca", "Rabat", "Marrakech", "Tanger", "Fès");
        Random random = new Random();

        for (int i = 1; i <= 15; i++) {
            Event event = new Event();
            event.setTitre("Événement " + i);
            event.setDescription("Description complète de l'événement culturel numéro " + i);

            // Catégories variées
            if (i <= 3) event.setCategorie(EventCategory.CONCERT);
            else if (i <= 6) event.setCategorie(EventCategory.THEATRE);
            else if (i <= 9) event.setCategorie(EventCategory.CONFERENCE);
            else if (i <= 12) event.setCategorie(EventCategory.SPORT);
            else event.setCategorie(EventCategory.AUTRE);

            // Dates et Statuts variés
            if (i % 5 == 0) {
                event.setStatut(EventStatus.TERMINE);
                event.setDateDebut(LocalDateTime.now().minusDays(10));
                event.setDateFin(LocalDateTime.now().minusDays(9));
            } else if (i % 4 == 0) {
                event.setStatut(EventStatus.ANNULE);
                event.setDateDebut(LocalDateTime.now().plusDays(20));
                event.setDateFin(LocalDateTime.now().plusDays(20));
            } else if (i % 3 == 0) {
                event.setStatut(EventStatus.BROUILLON); // Pas encore visible
                event.setDateDebut(LocalDateTime.now().plusDays(30));
                event.setDateFin(LocalDateTime.now().plusDays(30));
            } else {
                event.setStatut(EventStatus.PUBLIE); // Disponible
                event.setDateDebut(LocalDateTime.now().plusDays(i * 2));
                event.setDateFin(LocalDateTime.now().plusDays(i * 2).plusHours(4));
            }

            event.setLieu("Salle " + i);
            event.setVille(villes.get(random.nextInt(villes.size())));
            event.setCapaciteMax(50 + random.nextInt(450)); // Entre 50 et 500
            event.setPrixUnitaire(50.0 + random.nextInt(450)); // Prix entre 50 et 500

            // Assigner un organisateur au hasard
            event.setOrganisateur(random.nextBoolean() ? org1 : org2);

            eventRepository.save(event);

            // --- 3. CRÉATION DES RÉSERVATIONS (Pour les événements publiés) ---
            if (event.getStatut() == EventStatus.PUBLIE) {
                createReservation(event, client1, 2);
                createReservation(event, client2, 3);
            }
        }
        System.out.println("✅ Événements et Réservations créés");
        System.out.println("🎉 DONNÉES CHARGÉES AVEC SUCCÈS !");
    }

    private User createUser(String nom, String prenom, String email, Role role) {
        User u = new User();
        u.setNom(nom); u.setPrenom(prenom);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password")); // Mot de passe par défaut
        u.setRole(role);
        return userRepository.save(u);
    }

    private void createReservation(Event event, User user, int places) {
        Reservation r = new Reservation();
        r.setEvenement(event);
        r.setUtilisateur(user);
        r.setNombrePlaces(places);
        r.setMontantTotal(event.getPrixUnitaire() * places);
        r.setDateReservation(LocalDateTime.now());
        r.setStatut(ReservationStatus.CONFIRMEE);
        r.setCodeReservation("EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservationRepository.save(r);
    }
}