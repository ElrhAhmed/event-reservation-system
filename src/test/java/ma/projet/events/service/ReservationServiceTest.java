package ma.projet.events.service;

import ma.projet.events. entity.*;
import ma.projet.events. exception.*;
import ma.projet.events.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api. DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org. mockito.Mock;
import org.mockito.junit.jupiter. MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito. Mockito.*;

@ExtendWith(MockitoExtension. class)
@DisplayName("Tests de ReservationService avec Mockito")
class ReservationServiceTest {

    // @Mock crée des "faux" repositories
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    // @InjectMocks crée le service et injecte les mocks
    @InjectMocks
    private ReservationService reservationService;

    // Données de test
    private Event eventPublie;
    private User client;

    @BeforeEach
    void setUp() {
        // Préparer un événement publié dans 10 jours
        eventPublie = new Event();
        eventPublie.setId(1L);
        eventPublie.setTitre("Concert Test");
        eventPublie.setDescription("Description test");
        eventPublie.setCategorie(EventCategory.CONCERT);
        eventPublie.setDateDebut(LocalDateTime.now().plusDays(10));
        eventPublie.setDateFin(LocalDateTime.now().plusDays(10).plusHours(3));
        eventPublie.setLieu("Test Venue");
        eventPublie.setVille("Casablanca");
        eventPublie.setCapaciteMax(100);
        eventPublie.setPrixUnitaire(50.0);
        eventPublie.setStatut(EventStatus. PUBLIE);

        // Préparer un client
        client = new User();
        client.setId(1L);
        client.setNom("Test");
        client.setPrenom("User");
        client.setEmail("test@example.com");
        client.setRole(Role.CLIENT);
    }

    // ==================== TEST 1 : Réservation Valide ====================

    @Test
    @DisplayName("Doit créer une réservation valide avec toutes les vérifications")
    void testReserverTicket_Success() {
        // ARRANGE
        when(eventRepository.findById(1L)). thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(reservationRepository.calculateTotalPlacesReserved(1L)).thenReturn(20);
        when(reservationRepository.findByUtilisateurIdAndEvenementId(1L, 1L))
                .thenReturn(Optional.empty());
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation resa = invocation.getArgument(0);
                    resa.setId(1L);
                    return resa;
                });

        // ACT
        Reservation result = reservationService.reserverTicket(1L, 1L, 5);

        // ASSERT
        assertNotNull(result);
        assertEquals(5, result.getNombrePlaces());
        assertEquals(250.0, result.getMontantTotal()); // 5 × 50 DH
        assertEquals(ReservationStatus.CONFIRMEE, result.getStatut());
        assertNotNull(result.getCodeReservation());
        assertTrue(result.getCodeReservation().startsWith("EVT-"));
        assertTrue(result.getCodeReservation().matches("EVT-\\d{5}"));

        // Vérifier que save() a été appelé exactement 1 fois
        verify(reservationRepository, times(1)).save(any(Reservation.class));

        // Vérifier que findById a été appelé pour event et user
        verify(eventRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
    }

    // ==================== TEST 2 : Maximum 10 Places ====================

    @Test
    @DisplayName("❌ Doit rejeter une réservation de plus de 10 places")
    void testReserverTicket_Max10Places() {
        // ARRANGE
        when(eventRepository. findById(1L)).thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.reserverTicket(1L, 1L, 11)
        );

        assertTrue(exception.getMessage().contains("10 places"));

        // Vérifier que save() n'a JAMAIS été appelé
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 3 : Minimum 1 Place ====================

    @Test
    @DisplayName("❌ Doit rejeter une réservation de 0 place")
    void testReserverTicket_Min1Place() {
        // ARRANGE
        when(eventRepository. findById(1L)).thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException. class,
                () -> reservationService.reserverTicket(1L, 1L, 0)
        );

        assertTrue(exception.getMessage().contains("au moins 1"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 4 : Événement BROUILLON ====================

    @Test
    @DisplayName("❌ Doit rejeter une réservation sur événement BROUILLON")
    void testReserverTicket_EventBrouillon() {
        // ARRANGE
        eventPublie.setStatut(EventStatus.BROUILLON);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService. reserverTicket(1L, 1L, 3)
        );

        assertTrue(exception.getMessage().contains("pas encore disponible"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 5 : Événement TERMINÉ ====================

    @Test
    @DisplayName("❌ Doit rejeter une réservation sur événement TERMINÉ")
    void testReserverTicket_EventTermine() {
        // ARRANGE
        eventPublie.setStatut(EventStatus.TERMINE);
        when(eventRepository. findById(1L)).thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException. class,
                () -> reservationService.reserverTicket(1L, 1L, 3)
        );

        assertTrue(exception.getMessage().contains("terminé"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 6 : Événement ANNULÉ ====================

    @Test
    @DisplayName("❌ Doit rejeter une réservation sur événement ANNULÉ")
    void testReserverTicket_EventAnnule() {
        // ARRANGE
        eventPublie.setStatut(EventStatus.ANNULE);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventPublie));
        when(userRepository. findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.reserverTicket(1L, 1L, 3)
        );

        assertTrue(exception.getMessage().contains("annulé"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 7 : Capacité Dépassée ====================

    @Test
    @DisplayName("❌ Doit rejeter si pas assez de places disponibles")
    void testReserverTicket_CapaciteDepassee() {
        // ARRANGE
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)). thenReturn(Optional.of(client));

        // Simuler que 95 places sont déjà réservées (capacité = 100)
        when(reservationRepository.calculateTotalPlacesReserved(1L)).thenReturn(95);

        // ACT & ASSERT : Réserver 10 places (95 + 10 > 100)
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.reserverTicket(1L, 1L, 10)
        );

        assertTrue(exception.getMessage().contains("5 place(s) disponible(s)"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 8 : Événement Introuvable ====================

    @Test
    @DisplayName("❌ Doit lancer ResourceNotFoundException si événement introuvable")
    void testReserverTicket_EventNotFound() {
        // ARRANGE
        when(eventRepository.findById(99L)).thenReturn(Optional. empty());

        // ACT & ASSERT
        assertThrows(
                ResourceNotFoundException. class,
                () -> reservationService.reserverTicket(99L, 1L, 3)
        );

        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 9 : Utilisateur Introuvable ====================

    @Test
    @DisplayName("❌ Doit lancer ResourceNotFoundException si utilisateur introuvable")
    void testReserverTicket_UserNotFound() {
        // ARRANGE
        when(eventRepository.findById(1L)). thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.reserverTicket(1L, 99L, 3)
        );

        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 10 : Doublon (déjà réservé) ====================

    @Test
    @DisplayName("❌ Doit rejeter si l'utilisateur a déjà réservé cet événement")
    void testReserverTicket_DejaReserve() {
        // ARRANGE
        when(eventRepository.findById(1L)). thenReturn(Optional.of(eventPublie));
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(reservationRepository.calculateTotalPlacesReserved(1L)).thenReturn(20);

        // Simuler qu'une réservation existe déjà
        Reservation existante = new Reservation();
        existante.setId(10L);
        existante.setStatut(ReservationStatus. CONFIRMEE);
        when(reservationRepository.findByUtilisateurIdAndEvenementId(1L, 1L))
                .thenReturn(Optional.of(existante));

        // ACT & ASSERT
        ConflictException exception = assertThrows(
                ConflictException. class,
                () -> reservationService.reserverTicket(1L, 1L, 3)
        );

        assertTrue(exception.getMessage().contains("déjà une réservation"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 11 : Annulation Valide ====================

    @Test
    @DisplayName("✅ Doit annuler une réservation (> 48h avant)")
    void testAnnulerReservation_Success() {
        // ARRANGE : Événement dans 5 jours (> 48h)
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUtilisateur(client);
        reservation.setEvenement(eventPublie);
        reservation.setStatut(ReservationStatus.CONFIRMEE);
        reservation.setCodeReservation("EVT-12345");

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // ACT
        assertDoesNotThrow(() -> {
            reservationService.annulerReservation(1L, 1L);
        });

        // ASSERT
        verify(reservationRepository, times(1)). save(any(Reservation.class));
        assertEquals(ReservationStatus.ANNULEE, reservation.getStatut());
    }

    // ==================== TEST 12 : Annulation < 48h ====================

    @Test
    @DisplayName("❌ Doit rejeter annulation si < 48h avant événement")
    void testAnnulerReservation_MoinsDe48h() {
        // ARRANGE : Événement dans 24h (< 48h)
        Event eventProche = new Event();
        eventProche.setId(1L);
        eventProche.setDateDebut(LocalDateTime.now(). plusHours(24));
        eventProche.setDateFin(LocalDateTime.now().plusHours(27));

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUtilisateur(client);
        reservation.setEvenement(eventProche);
        reservation. setStatut(ReservationStatus.CONFIRMEE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService. annulerReservation(1L, 1L)
        );

        assertTrue(exception.getMessage().contains("48h"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 13 : Annulation Déjà Annulée ====================

    @Test
    @DisplayName("❌ Doit rejeter annulation si déjà annulée")
    void testAnnulerReservation_DejaAnnulee() {
        // ARRANGE
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUtilisateur(client);
        reservation.setEvenement(eventPublie);
        reservation.setStatut(ReservationStatus. ANNULEE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // ACT & ASSERT
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> reservationService.annulerReservation(1L, 1L)
        );

        assertTrue(exception.getMessage().contains("déjà annulée"));
        verify(reservationRepository, never()). save(any());
    }

    // ==================== TEST 14 : Annulation Pas Propriétaire ====================

    @Test
    @DisplayName("❌ Doit rejeter annulation si pas le propriétaire")
    void testAnnulerReservation_PasProprietaire() {
        // ARRANGE
        User autreUser = new User();
        autreUser.setId(2L);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUtilisateur(autreUser); // Autre user
        reservation.setEvenement(eventPublie);
        reservation.setStatut(ReservationStatus.CONFIRMEE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // ACT & ASSERT
        UnauthorizedException exception = assertThrows(
                UnauthorizedException. class,
                () -> reservationService.annulerReservation(1L, 1L) // User 1 essaie d'annuler
        );

        assertTrue(exception. getMessage().contains("vos propres réservations"));
        verify(reservationRepository, never()).save(any());
    }

    // ==================== TEST 15 : Obtenir Réservation par Code ====================

    @Test
    @DisplayName("✅ Doit récupérer une réservation par code")
    void testGetReservationByCode_Success() {
        // ARRANGE
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation. setCodeReservation("EVT-12345");

        when(reservationRepository.findByCodeReservation("EVT-12345"))
                .thenReturn(Optional.of(reservation));

        // ACT
        Reservation result = reservationService.getReservationByCode("EVT-12345");

        // ASSERT
        assertNotNull(result);
        assertEquals("EVT-12345", result. getCodeReservation());
        verify(reservationRepository, times(1)).findByCodeReservation("EVT-12345");
    }

    // ==================== TEST 16 : Code Introuvable ====================

    @Test
    @DisplayName("❌ Doit lancer exception si code introuvable")
    void testGetReservationByCode_NotFound() {
        // ARRANGE
        when(reservationRepository.findByCodeReservation("EVT-99999"))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(
                ResourceNotFoundException. class,
                () -> reservationService.getReservationByCode("EVT-99999")
        );
    }

    // ==================== TESTS getReservationsByUser() ====================

    @Test
    @DisplayName("✅ Doit récupérer toutes les réservations d'un utilisateur")
    void testGetReservationsByUser_Success() {
        // ARRANGE
        Reservation resa1 = new Reservation();
        resa1.setId(1L);
        resa1.setUtilisateur(client);
        resa1.setCodeReservation("EVT-11111");
        resa1.setStatut(ReservationStatus. CONFIRMEE);

        Reservation resa2 = new Reservation();
        resa2.setId(2L);
        resa2.setUtilisateur(client);
        resa2.setCodeReservation("EVT-22222");
        resa2.setStatut(ReservationStatus.ANNULEE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(reservationRepository.findByUtilisateurId(1L))
                .thenReturn(Arrays.asList(resa1, resa2));

        // ACT
        List<Reservation> results = reservationService.findUserReservations(1L);

        // ASSERT
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getUtilisateur().equals(client)));
        verify(reservationRepository, times(1)).findByUtilisateurId(1L);
    }

    @Test
    @DisplayName("✅ Doit retourner liste vide si aucune réservation")
    void testGetReservationsByUser_Empty() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(reservationRepository.findByUtilisateurId(1L))
                .thenReturn(Collections.emptyList());

        // ACT
        List<Reservation> results = reservationService.findUserReservations(1L);

        // ASSERT
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("❌ Doit lancer exception si utilisateur introuvable")
    void testGetReservationsByUser_UserNotFound() {
        // ARRANGE
        when(userRepository. findById(99L)).thenReturn(Optional. empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.findUserReservations(99L);
        });
    }

// ==================== TESTS getReservationStatistics() ====================

    @Test
    @DisplayName("✅ Doit calculer les statistiques globales des réservations")
    void testGetReservationStatistics() {
        // ARRANGE
        Reservation resa1 = new Reservation();
        resa1.setStatut(ReservationStatus. CONFIRMEE);
        resa1.setNombrePlaces(3);
        resa1.setMontantTotal(150.0);

        Reservation resa2 = new Reservation();
        resa2.setStatut(ReservationStatus.CONFIRMEE);
        resa2. setNombrePlaces(5);
        resa2.setMontantTotal(250.0);

        Reservation resa3 = new Reservation();
        resa3. setStatut(ReservationStatus.ANNULEE);
        resa3.setNombrePlaces(2);
        resa3.setMontantTotal(100.0);

        when(reservationRepository.findAll())
                .thenReturn(Arrays.asList(resa1, resa2, resa3));

        // ACT
        Map<String, Object> stats = reservationService.getReservationStatistics();

        // ASSERT
        assertNotNull(stats);
        assertEquals(3, (Integer) stats.get("totalReservations"));
        assertEquals(2L, (Long) stats.get("confirmedReservations"));
        assertEquals(1L, (Long) stats.get("cancelledReservations"));
        assertEquals(0L, (Long) stats.get("pendingReservations"));
        assertEquals(400.0, (Double) stats. get("totalRevenue"), 0.01); // 150 + 250
    }

    @Test
    @DisplayName("✅ Doit calculer stats par événement")
    void testGetReservationStatisticsByEvent() {
        // ARRANGE
        Reservation resa1 = new Reservation();
        resa1.setEvenement(eventPublie);
        resa1.setStatut(ReservationStatus. CONFIRMEE);
        resa1.setNombrePlaces(5);

        Reservation resa2 = new Reservation();
        resa2.setEvenement(eventPublie);
        resa2.setStatut(ReservationStatus.CONFIRMEE);
        resa2.setNombrePlaces(3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventPublie));
        when(reservationRepository. findByEvenementId(1L))
                .thenReturn(Arrays.asList(resa1, resa2));

        // ACT
        Map<String, Object> stats = reservationService.getReservationStatisticsByEvent(1L);

        // ASSERT
        assertNotNull(stats);
        assertEquals(2, (Integer) stats.get("totalReservations"));
        assertEquals(8, (Integer) stats.get("totalPlaces"));
        assertEquals(2L, (Long) stats.get("reservationsConfirmees"));
    }

// ==================== TESTS confirmReservation() (si applicable) ====================

    @Test
    @DisplayName("✅ Doit confirmer une réservation EN_ATTENTE")
    void testConfirmReservation_Success() {
        // ARRANGE
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation. setUtilisateur(client);
        reservation.setEvenement(eventPublie);
        reservation.setStatut(ReservationStatus.EN_ATTENTE);
        reservation.setCodeReservation("EVT-12345");

        when(reservationRepository.findById(1L)). thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Reservation result = reservationService.confirmReservation(1L);

        // ASSERT
        assertEquals(ReservationStatus.CONFIRMEE, result.getStatut());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("❌ Doit rejeter si déjà confirmée")
    void testConfirmReservation_AlreadyConfirmed() {
        // ARRANGE
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStatut(ReservationStatus. CONFIRMEE);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.confirmReservation(1L);
        });

        assertTrue(exception.getMessage(). contains("déjà confirmée") ||
                exception.getMessage().contains("EN_ATTENTE"));
        verify(reservationRepository, never()).save(any());
    }

// ==================== TESTS generateRecapitulatif() ====================

    @Test
    @DisplayName("✅ Doit générer un récapitulatif complet")
    void testGenerateRecapitulatif_Success() {
        // ARRANGE
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCodeReservation("EVT-12345");
        reservation.setUtilisateur(client);
        reservation.setEvenement(eventPublie);
        reservation.setNombrePlaces(3);
        reservation.setMontantTotal(150.0);
        reservation.setStatut(ReservationStatus. CONFIRMEE);
        reservation.setDateReservation(LocalDateTime.now());

        when(reservationRepository. findById(1L)).thenReturn(Optional.of(reservation));

        // ACT
        Map<String, Object> recap = reservationService.getReservationSummary(1L);

        // ASSERT
        assertNotNull(recap);
        assertEquals("EVT-12345", recap.get("code"));
        assertEquals("Concert Test", recap.get("eventTitle"));
        assertEquals(3, recap.get("nombrePlaces"));
        assertEquals(150.0, (Double) recap.get("montantTotal"), 0.01);
        assertEquals("Confirmée", recap.get("statutLabel"));
        assertNotNull(recap.get("dateReservation"));
        assertNotNull(recap.get("clientName"));
        assertTrue(recap.get("annulable") instanceof Boolean);
    }
}

