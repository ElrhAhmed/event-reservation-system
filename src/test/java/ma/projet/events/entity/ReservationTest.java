package ma.projet.events.entity;

import org.junit.jupiter. api.BeforeEach;
import org. junit.jupiter.api.DisplayName;
import org.junit. jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter. api.Assertions.*;

// @DisplayName donne un nom lisible à la classe de tests
@DisplayName("Tests de l'entité Reservation")
class ReservationTest {

    // Variables utilisées dans tous les tests
    private Event event;
    private User user;
    private Reservation reservation;

    // @BeforeEach : exécuté AVANT chaque test
    // Prépare les données communes
    @BeforeEach
    void setUp() {
        // ARRANGE : Créer un événement de test
        event = new Event();
        event.setId(1L);
        event.setTitre("Concert Rock");
        event.setDateDebut(LocalDateTime.now(). plusDays(5)); // Dans 5 jours
        event.setDateFin(LocalDateTime.now(). plusDays(5).plusHours(3));
        event.setCapaciteMax(100);
        event.setPrixUnitaire(50.0);
        event.setStatut(EventStatus.PUBLIE);

        // ARRANGE : Créer un utilisateur de test
        user = new User();
        user.setId(1L);
        user.setNom("Dupont");
        user.setPrenom("Jean");
        user. setEmail("jean. dupont@test.com");
        user.setRole(Role.CLIENT);

        // ARRANGE : Créer une réservation de test
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCodeReservation("EVT-12345");
        reservation.setNombrePlaces(3);
        reservation.setMontantTotal(150.0);
        reservation.setEvenement(event);
        reservation.setUtilisateur(user);
        reservation.setStatut(ReservationStatus. CONFIRMEE);
        reservation.setDateReservation(LocalDateTime.now());
    }

    // ==================== TEST 1 : Calcul Prix Unitaire ====================

    @Test
    @DisplayName("Doit calculer le prix unitaire correctement")
    void testGetPrixUnitaire() {
        // ACT : Calculer le prix unitaire
        Double prixUnitaire = reservation.getPrixUnitaire();

        // ASSERT : Vérifier que 150 / 3 = 50
        assertEquals(50.0, prixUnitaire, 0.01);
        // 0.01 = tolérance pour les doubles (éviter erreurs d'arrondi)
    }

    // ==================== TEST 2 : Réservation Annulable ====================

    @Test
    @DisplayName("Doit être annulable si événement dans plus de 48h")
    void testIsAnnulable_AvantLimite48h() {
        // ARRANGE : Événement dans 5 jours (> 48h)
        event.setDateDebut(LocalDateTime.now().plusDays(5));
        reservation.setEvenement(event);
        reservation.setStatut(ReservationStatus. CONFIRMEE);

        // ACT : Vérifier si annulable
        boolean annulable = reservation.isAnnulable();

        // ASSERT : Doit être annulable
        assertTrue(annulable, "La réservation doit être annulable 5 jours avant");
    }

    @Test
    @DisplayName("Ne doit PAS être annulable si événement dans moins de 48h")
    void testIsAnnulable_ApresLimite48h() {
        // ARRANGE : Événement dans 24h (< 48h)
        event.setDateDebut(LocalDateTime.now().plusHours(24));
        reservation. setEvenement(event);
        reservation.setStatut(ReservationStatus.CONFIRMEE);

        // ACT
        boolean annulable = reservation. isAnnulable();

        // ASSERT : Ne doit PAS être annulable
        assertFalse(annulable, "La réservation ne doit PAS être annulable 24h avant");
    }

    @Test
    @DisplayName("Ne doit PAS être annulable si déjà annulée")
    void testIsAnnulable_DejaAnnulee() {
        // ARRANGE : Réservation déjà annulée
        event.setDateDebut(LocalDateTime.now().plusDays(10)); // Date OK
        reservation.setEvenement(event);
        reservation.setStatut(ReservationStatus. ANNULEE); // ← Déjà annulée

        // ACT
        boolean annulable = reservation.isAnnulable();

        // ASSERT
        assertFalse(annulable, "Une réservation annulée ne peut pas être annulée");
    }

    // ==================== TEST 3 : Heures Avant Limite ====================

    @Test
    @DisplayName("Doit calculer les heures restantes avant limite d'annulation")
    void testGetHeuresAvantLimiteAnnulation() {
        // ARRANGE : Événement dans exactement 50 heures
        event.setDateDebut(LocalDateTime.now().plusHours(50));
        reservation. setEvenement(event);

        // ACT
        long heuresRestantes = reservation.getHeuresAvantLimiteAnnulation();

        // ASSERT : 50h - 48h = 2h restantes
        assertEquals(2, heuresRestantes, "Il doit rester 2h avant la limite");
    }

    @Test
    @DisplayName("Doit retourner 0 si limite dépassée")
    void testGetHeuresAvantLimiteAnnulation_Depassee() {
        // ARRANGE : Événement dans 24h (limite dépassée)
        event. setDateDebut(LocalDateTime. now().plusHours(24));
        reservation. setEvenement(event);

        // ACT
        long heuresRestantes = reservation.getHeuresAvantLimiteAnnulation();

        // ASSERT : Doit retourner 0
        assertEquals(0, heuresRestantes);
    }

    // ==================== TEST 4 : Statuts ====================

    @Test
    @DisplayName("Doit identifier une réservation confirmée")
    void testIsConfirmee() {
        // ARRANGE
        reservation.setStatut(ReservationStatus.CONFIRMEE);

        // ACT & ASSERT
        assertTrue(reservation. isConfirmee());
        assertFalse(reservation.isAnnulee());
        assertFalse(reservation.isEnAttente());
    }

    @Test
    @DisplayName("Doit identifier une réservation annulée")
    void testIsAnnulee() {
        // ARRANGE
        reservation.setStatut(ReservationStatus.ANNULEE);

        // ACT & ASSERT
        assertTrue(reservation. isAnnulee());
        assertFalse(reservation.isConfirmee());
    }

    // ==================== TEST 5 : Résumé ====================

    @Test
    @DisplayName("Doit générer un résumé correct")
    void testGetResume() {
        // ACT
        String resume = reservation. getResume();

        // ASSERT : Vérifier que le résumé contient les infos importantes
        assertNotNull(resume);
        assertTrue(resume. contains("EVT-12345"), "Le résumé doit contenir le code");
        assertTrue(resume. contains("3 place(s)"), "Le résumé doit contenir le nombre de places");
        assertTrue(resume. contains("Concert Rock"), "Le résumé doit contenir le titre");
        assertTrue(resume.contains("Confirmée"), "Le résumé doit contenir le statut");
    }

    // ==================== TEST 6 : Format Code ====================

    @Test
    @DisplayName("Le code réservation doit respecter le format EVT-XXXXX")
    void testCodeReservationFormat() {
        // ACT & ASSERT
        assertNotNull(reservation.getCodeReservation());
        assertTrue(
                reservation.getCodeReservation().matches("EVT-\\d{5}"),
                "Le code doit être au format EVT-12345"
        );
    }

    // ==================== TEST 7 : Validation Nombre Places ====================

    @Test
    @DisplayName("Le nombre de places doit être entre 1 et 10")
    void testNombrePlacesValide() {
        // ACT
        Integer nombrePlaces = reservation.getNombrePlaces();

        // ASSERT
        assertNotNull(nombrePlaces);
        assertTrue(nombrePlaces >= 1, "Au moins 1 place");
        assertTrue(nombrePlaces <= 10, "Maximum 10 places");
    }
}