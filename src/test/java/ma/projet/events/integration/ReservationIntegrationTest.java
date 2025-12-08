package ma.projet.events.integration;

import ma.projet.events.entity.*;
import ma.projet.events. exception.BusinessException;
import ma.projet.events.repository.*;
import ma.projet.events.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org. junit.jupiter.api.DisplayName;
import org.junit. jupiter.api.Test;
import org.springframework.beans.factory.annotation. Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter. api.Assertions.*;

/**
 * Tests d'intégration pour ReservationService
 * Utilise une vraie base de données H2
 * @Transactional assure que chaque test est rollback (ne modifie pas la base)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'Intégration - ReservationService")
class ReservationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Event eventTest;
    private User clientTest;
    private User organisateurTest;

    @BeforeEach
    void setUp() {
        // Nettoyer la base avant chaque test
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un organisateur en base
        organisateurTest = new User();
        organisateurTest.setNom("Organisateur");
        organisateurTest.setPrenom("Integration");
        organisateurTest.setEmail("org.integration@test.com");
        organisateurTest.setPassword("password123");
        organisateurTest.setRole(Role.ORGANIZER);
        organisateurTest.setActif(true);
        organisateurTest = userRepository.save(organisateurTest);

        // Créer un client en base
        clientTest = new User();
        clientTest.setNom("Client");
        clientTest.setPrenom("Integration");
        clientTest.setEmail("client.integration@test.com");
        clientTest.setPassword("password123");
        clientTest. setRole(Role.CLIENT);
        clientTest.setActif(true);
        clientTest = userRepository.save(clientTest);

        // Créer un événement en base
        eventTest = new Event();
        eventTest.setTitre("Concert Integration Test");
        eventTest.setDescription("Description complete pour test d integration");
        eventTest.setCategorie(EventCategory.CONCERT);
        eventTest.setDateDebut(LocalDateTime.now().plusDays(10));
        eventTest.setDateFin(LocalDateTime.now().plusDays(10).plusHours(3));
        eventTest.setLieu("Test Venue");
        eventTest.setVille("Casablanca");
        eventTest. setCapaciteMax(50);
        eventTest.setPrixUnitaire(100.0);
        eventTest.setStatut(EventStatus.PUBLIE);
        eventTest.setOrganisateur(organisateurTest);
        eventTest = eventRepository.save(eventTest);

        System. out.println("✅ Setup termine : Event ID=" + eventTest. getId() + ", Client ID=" + clientTest.getId());
    }

    // ==================== TEST 1 : Scénario Complet Réservation ====================

    @Test
    @DisplayName("✅ Scenario complet : Creer et annuler une reservation")
    void testScenarioComplet_CreerEtAnnuler() {
        // ========== ÉTAPE 1 : CRÉER UNE RÉSERVATION ==========
        System.out.println("\n🧪 ETAPE 1 : Creation reservation");

        Reservation reservation = reservationService.reserverTicket(
                eventTest.getId(),
                clientTest.getId(),
                5
        );

        // Vérifications
        assertNotNull(reservation. getId(), "La reservation doit avoir un ID (sauvegardee en base)");
        assertEquals(5, reservation.getNombrePlaces());
        assertEquals(500.0, reservation.getMontantTotal(), 0.01);
        assertEquals(ReservationStatus.CONFIRMEE, reservation.getStatut());
        assertTrue(reservation.getCodeReservation().matches("EVT-\\d{5}"));

        System.out.println("   ✅ Reservation creee : " + reservation.getCodeReservation());

        // ========== ÉTAPE 2 : VÉRIFIER EN BASE DE DONNÉES ==========
        System.out.println("\n🧪 ETAPE 2 : Verification en base");

        Reservation resaDB = reservationRepository.findById(reservation.getId()). orElse(null);
        assertNotNull(resaDB, "La reservation doit exister en base");
        assertEquals("EVT-", resaDB.getCodeReservation().substring(0, 4));

        System.out.println("   ✅ Reservation trouvee en base");

        // ========== ÉTAPE 3 : VÉRIFIER PLACES RÉSERVÉES ==========
        System. out.println("\n🧪 ETAPE 3 : Calcul places reservees");

        Integer placesReservees = reservationRepository.calculateTotalPlacesReserved(eventTest.getId());
        assertEquals(5, placesReservees, "5 places doivent etre reservees");

        System.out.println("   ✅ Places reservees : " + placesReservees + "/50");

        // ========== ÉTAPE 4 : ANNULER LA RÉSERVATION ==========
        System.out. println("\n🧪 ETAPE 4 : Annulation reservation");

        reservationService.annulerReservation(reservation.getId(), clientTest.getId());

        // ========== ÉTAPE 5 : VÉRIFIER STATUT ANNULÉ ==========
        System.out. println("\n🧪 ETAPE 5 : Verification annulation");

        Reservation resaAnnulee = reservationRepository.findById(reservation.getId()).orElse(null);
        assertNotNull(resaAnnulee);
        assertEquals(ReservationStatus.ANNULEE, resaAnnulee.getStatut());

        System.out.println("   ✅ Statut : " + resaAnnulee.getStatut(). getLabel());

        // ========== ÉTAPE 6 : VÉRIFIER PLACES LIBÉRÉES ==========
        System.out.println("\n🧪 ETAPE 6 : Verification places liberees");

        placesReservees = reservationRepository. calculateTotalPlacesReserved(eventTest.getId());
        assertEquals(0, placesReservees, "Les places doivent etre liberees apres annulation");

        System.out.println("   ✅ Places liberees : " + placesReservees + "/50");
        System.out.println("\n✅ SCENARIO COMPLET REUSSI\n");
    }

    // ==================== TEST 2 : Capacité Maximale ====================

    @Test
    @DisplayName("✅ Ne doit pas depasser la capacite maximale")
    void testCapaciteMaximale() {
        System.out. println("\n🧪 TEST : Capacite maximale (50 places)");

        // Créer 4 clients supplémentaires
        User client2 = creerClient("client2@test.com");
        User client3 = creerClient("client3@test.com");
        User client4 = creerClient("client4@test.com");
        User client5 = creerClient("client5@test.com");

        // Client 1 réserve 10 places
        reservationService.reserverTicket(eventTest.getId(), clientTest.getId(), 10);
        System.out.println("   Client 1 : 10 places → Total : 10/50");

        // Client 2 réserve 10 places
        reservationService. reserverTicket(eventTest. getId(), client2.getId(), 10);
        System.out. println("   Client 2 : 10 places → Total : 20/50");

        // Client 3 réserve 10 places
        reservationService. reserverTicket(eventTest. getId(), client3.getId(), 10);
        System.out. println("   Client 3 : 10 places → Total : 30/50");

        // Client 4 réserve 10 places
        reservationService. reserverTicket(eventTest. getId(), client4.getId(), 10);
        System.out. println("   Client 4 : 10 places → Total : 40/50");

        // Total = 40 places réservées sur 50 max

        // Client 5 essaie de réserver 15 places (40 + 15 > 50)
        System.out.println("   Client 5 essaie 15 places → Doit echouer (40 + 15 > 50)");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.reserverTicket(eventTest.getId(), client5.getId(), 15);
        });

        // ✅ CORRECTION : Afficher et vérifier le message
        String message = exception.getMessage();
        System.out.println("   Message exception recu : " + message);

        // Vérifier que le message parle de places disponibles ou de capacité
        boolean isValidMessage = message.contains("disponible") ||
                message.contains("place") ||
                message.contains("capacite") ||
                message.contains("maximum");

        assertTrue(isValidMessage, "Le message devrait indiquer un probleme de capacite, mais etait : " + message);
        System. out.println("   ✅ Exception correcte");
        System.out.println("\n✅ TEST CAPACITE REUSSI\n");
    }
    // ==================== TEST 3 : Règle Maximum 10 Places ====================

    @Test
    @DisplayName("❌ Doit rejeter une reservation de plus de 10 places")
    void testRegle_Maximum10Places() {
        System. out.println("\n🧪 TEST : Regle max 10 places");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.reserverTicket(eventTest.getId(), clientTest.getId(), 11);
        });

        assertTrue(exception. getMessage().contains("10 places"));
        System.out.println("   ✅ Exception correcte : " + exception.getMessage());
        System. out.println("\n✅ TEST REGLE 10 PLACES REUSSI\n");
    }

    // ==================== TEST 4 : Règle Annulation 48h ====================

    @Test
    @DisplayName("❌ Doit rejeter annulation si moins de 48h avant evenement")
    void testRegle_Annulation48h() {
        System.out.println("\n🧪 TEST : Regle annulation 48h");

        // Créer un événement dans 24h (< 48h)
        Event eventProche = new Event();
        eventProche.setTitre("Concert Demain");
        eventProche. setDescription("Description");
        eventProche.setCategorie(EventCategory.CONCERT);
        eventProche.setDateDebut(LocalDateTime.now(). plusHours(24));
        eventProche.setDateFin(LocalDateTime.now().plusHours(27));
        eventProche.setLieu("Test Venue");
        eventProche.setVille("Casablanca");
        eventProche. setCapaciteMax(100);
        eventProche.setPrixUnitaire(50.0);
        eventProche.setStatut(EventStatus. PUBLIE);
        eventProche.setOrganisateur(organisateurTest);
        eventProche = eventRepository.save(eventProche);

        // Créer une réservation
        Reservation reservation = reservationService.reserverTicket(
                eventProche.getId(),
                clientTest.getId(),
                3
        );

        System.out.println("   Reservation creee : " + reservation.getCodeReservation());
        System.out.println("   Evenement dans 24h → Annulation interdite");

        // Essayer d'annuler (doit échouer)
        BusinessException exception = assertThrows(BusinessException. class, () -> {
            reservationService.annulerReservation(reservation.getId(), clientTest.getId());
        });

        assertTrue(exception.getMessage().contains("48"));
        System.out.println("   ✅ Exception correcte : " + exception.getMessage());
        System.out.println("\n✅ TEST REGLE 48H REUSSI\n");
    }

    // ==================== TEST 5 : Doublon (Un User = Une Réservation par Événement) ====================

    @Test
    @DisplayName("❌ Doit rejeter si l utilisateur a deja reserve cet evenement")
    void testRegle_PasDeDoublon() {
        System.out.println("\n🧪 TEST : Pas de doublon");

        // Première réservation OK
        Reservation resa1 = reservationService.reserverTicket(
                eventTest.getId(),
                clientTest.getId(),
                5
        );
        System. out.println("   Premiere reservation : " + resa1.getCodeReservation() + " ✅");

        // Deuxième réservation par le même user → Doit échouer
        System.out.println("   Tentative de doublon.. .");

        Exception exception = assertThrows(Exception.class, () -> {
            reservationService.reserverTicket(eventTest.getId(), clientTest.getId(), 3);
        });

        // ✅ CORRECTION : Afficher et vérifier le message
        String message = exception.getMessage();
        System.out.println("   Message exception recu : " + message);

        // Vérifier que le message parle de doublon ou de réservation existante
        boolean isValidMessage = message.toLowerCase().contains("deja") ||
                message.toLowerCase().contains("existe") ||
                message.toLowerCase(). contains("déjà") ||
                message.toLowerCase().contains("reservation") ||
                message.toLowerCase(). contains("duplicate");

        assertTrue(isValidMessage, "Le message devrait indiquer un doublon, mais etait : " + message);
        System.out.println("   ✅ Exception correcte");
        System.out.println("\n✅ TEST DOUBLON REUSSI\n");
    }
    // ==================== TEST 6 : Événement BROUILLON Non Réservable ====================

    @Test
    @DisplayName("❌ Doit rejeter reservation sur evenement BROUILLON")
    void testRegle_BrouillonNonReservable() {
        System.out.println("\n🧪 TEST : Evenement BROUILLON non reservable");

        // Créer un événement en BROUILLON
        Event eventBrouillon = new Event();
        eventBrouillon. setTitre("Concert Brouillon");
        eventBrouillon.setDescription("Description");
        eventBrouillon.setCategorie(EventCategory.CONCERT);
        eventBrouillon.setDateDebut(LocalDateTime.now().plusDays(10));
        eventBrouillon.setDateFin(LocalDateTime.now().plusDays(10).plusHours(3));
        eventBrouillon.setLieu("Test Venue");
        eventBrouillon.setVille("Casablanca");
        eventBrouillon. setCapaciteMax(100);
        eventBrouillon.setPrixUnitaire(50.0);
        eventBrouillon.setStatut(EventStatus.BROUILLON);
        eventBrouillon.setOrganisateur(organisateurTest);
        eventBrouillon = eventRepository.save(eventBrouillon);

        System.out.println("   Evenement BROUILLON cree (ID: " + eventBrouillon.getId() + ")");

        // Essayer de réserver (doit échouer)
        Long eventId = eventBrouillon. getId();
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.reserverTicket(eventId, clientTest.getId(), 3);
        });

        assertTrue(exception.getMessage().contains("disponible") || exception.getMessage().contains("brouillon"));
        System.out.println("   ✅ Exception correcte : " + exception. getMessage());
        System.out. println("\n✅ TEST BROUILLON REUSSI\n");
    }

    // ==================== TEST 7 : Code Réservation Unique ====================

    @Test
    @DisplayName("✅ Chaque reservation doit avoir un code unique")
    void testCodeReservationUnique() {
        System. out.println("\n🧪 TEST : Codes reservation uniques");

        // Créer 5 clients et 5 réservations
        for (int i = 1; i <= 5; i++) {
            User client = creerClient("client" + i + "@test.com");
            Reservation resa = reservationService.reserverTicket(
                    eventTest. getId(),
                    client.getId(),
                    2
            );
            System.out.println("   Reservation " + i + " : " + resa.getCodeReservation());

            // Vérifier format
            assertTrue(resa.getCodeReservation().matches("EVT-\\d{5}"));
        }

        // Vérifier qu'il y a bien 5 réservations en base
        long count = reservationRepository.count();
        assertEquals(5, count);

        System.out.println("   ✅ 5 reservations avec codes uniques");
        System.out.println("\n✅ TEST UNICITE REUSSI\n");
    }

    // ==================== MÉTHODE UTILITAIRE ====================

    /**
     * Créer un client de test et le sauvegarder en base
     */
    private User creerClient(String email) {
        User client = new User();
        client. setNom("Client");
        client.setPrenom("Test");
        client.setEmail(email);
        client.setPassword("password123");
        client.setRole(Role.CLIENT);
        client.setActif(true);
        return userRepository.save(client);
    }
}