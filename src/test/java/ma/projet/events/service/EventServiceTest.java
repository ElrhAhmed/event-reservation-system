package ma.projet. events.service;

import ma.projet.events.entity.*;
import ma.projet.events. exception.*;
import ma.projet.events. repository.*;
import org.junit. jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit. jupiter.api.Test;
import org.junit.jupiter.api. extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter. api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito. Mockito.*;

@ExtendWith(MockitoExtension. class)
@DisplayName("Tests de EventService avec Mockito")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private EventService eventService;

    // Données de test
    private User organisateur;
    private Event eventBrouillon;
    private Event eventPublie;

    @BeforeEach
    void setUp() {
        // Créer un organisateur
        organisateur = new User();
        organisateur.setId(1L);
        organisateur.setNom("Organisateur");
        organisateur. setPrenom("Test");
        organisateur.setEmail("org@test.com");
        organisateur. setRole(Role.ORGANIZER);

        // Créer un événement en BROUILLON (complet)
        eventBrouillon = new Event();
        eventBrouillon.setId(1L);
        eventBrouillon.setTitre("Concert Test");
        eventBrouillon.setDescription("Description complète");
        eventBrouillon.setCategorie(EventCategory.CONCERT);
        eventBrouillon.setDateDebut(LocalDateTime.now(). plusDays(10));
        eventBrouillon.setDateFin(LocalDateTime.now().plusDays(10). plusHours(3));
        eventBrouillon.setLieu("Test Venue");
        eventBrouillon.setVille("Casablanca");
        eventBrouillon.setCapaciteMax(100);
        eventBrouillon.setPrixUnitaire(50.0);
        eventBrouillon.setStatut(EventStatus.BROUILLON);
        eventBrouillon.setOrganisateur(organisateur);

        // Créer un événement PUBLIÉ
        eventPublie = new Event();
        eventPublie.setId(2L);
        eventPublie.setTitre("Théâtre Test");
        eventPublie.setDescription("Description");
        eventPublie.setCategorie(EventCategory.THEATRE);
        eventPublie.setDateDebut(LocalDateTime.now(). plusDays(5));
        eventPublie.setDateFin(LocalDateTime.now().plusDays(5).plusHours(2));
        eventPublie. setLieu("Théâtre Royal");
        eventPublie.setVille("Rabat");
        eventPublie. setCapaciteMax(200);
        eventPublie.setPrixUnitaire(100.0);
        eventPublie.setStatut(EventStatus. PUBLIE);
        eventPublie.setOrganisateur(organisateur);
    }

    // ==================== TESTS publishEvent() ====================

    @Test
    @DisplayName("✅ Doit publier un événement BROUILLON complet")
    void testPublishEvent_Success() {
        // ARRANGE
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventBrouillon));
        when(eventRepository.save(any(Event. class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Event result = eventService.publishEvent(1L, 1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(EventStatus.PUBLIE, result.getStatut());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'utilisateur n'est pas l'organisateur")
    void testPublishEvent_NotOrganizer() {
        // ARRANGE
        when(eventRepository.findById(1L)).thenReturn(Optional.of(eventBrouillon));

        // ACT & ASSERT
        assertThrows(UnauthorizedException.class, () -> {
            eventService.publishEvent(1L, 999L); // Autre user
        });

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'événement n'est pas en BROUILLON")
    void testPublishEvent_NotDraft() {
        // ARRANGE
        when(eventRepository.findById(2L)).thenReturn(Optional. of(eventPublie));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventService.publishEvent(2L, 1L);
        });

        assertTrue(exception.getMessage().contains("brouillon"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si des champs obligatoires manquent")
    void testPublishEvent_MissingFields() {
        // ARRANGE
        eventBrouillon.setDescription(null); // Champ manquant
        when(eventRepository.findById(1L)).thenReturn(Optional. of(eventBrouillon));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventService.publishEvent(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("Description"));
        verify(eventRepository, never()).save(any());
    }

    // ==================== TESTS cancelEvent() ====================

    @Test
    @DisplayName("✅ Doit annuler un événement et toutes ses réservations")
    void testCancelEvent_Success() {
        // ARRANGE
        when(eventRepository.findById(2L)).thenReturn(Optional. of(eventPublie));

        Reservation resa1 = new Reservation();
        resa1.setId(1L);
        resa1.setStatut(ReservationStatus.CONFIRMEE);

        Reservation resa2 = new Reservation();
        resa2.setId(2L);
        resa2.setStatut(ReservationStatus. CONFIRMEE);

        when(reservationRepository.findByEvenementId(2L)).thenReturn(Arrays. asList(resa1, resa2));
        when(eventRepository.save(any(Event.class))). thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Event result = eventService.cancelEvent(2L, 1L, "Problème technique");

        // ASSERT
        assertEquals(EventStatus.ANNULE, result.getStatut());
        verify(reservationRepository, times(2)).save(any(Reservation.class));
        assertEquals(ReservationStatus.ANNULEE, resa1.getStatut());
        assertEquals(ReservationStatus.ANNULEE, resa2.getStatut());
        assertTrue(resa1.getCommentaire().contains("Problème technique"));
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'événement est déjà annulé")
    void testCancelEvent_AlreadyCancelled() {
        // ARRANGE
        eventPublie.setStatut(EventStatus.ANNULE);
        when(eventRepository.findById(2L)).thenReturn(Optional.of(eventPublie));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventService.cancelEvent(2L, 1L, "Test");
        });

        assertTrue(exception.getMessage().contains("déjà annulé"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'événement est terminé")
    void testCancelEvent_Finished() {
        // ARRANGE
        eventPublie.setStatut(EventStatus. TERMINE);
        when(eventRepository. findById(2L)).thenReturn(Optional.of(eventPublie));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventService.cancelEvent(2L, 1L, "Test");
        });

        assertTrue(exception.getMessage().contains("terminé"));
        verify(eventRepository, never()).save(any());
    }

    // ==================== TESTS deleteEventSafely() ====================

    @Test
    @DisplayName("✅ Doit supprimer un événement sans réservation")
    void testDeleteEventSafely_Success() {
        // ARRANGE
        when(eventRepository.findById(1L)).thenReturn(Optional. of(eventBrouillon));
        when(reservationRepository.countByEvenementId(1L)).thenReturn(0L);

        // ACT
        assertDoesNotThrow(() -> {
            eventService.deleteEventSafely(1L, 1L);
        });

        // ASSERT
        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'événement a des réservations")
    void testDeleteEventSafely_HasReservations() {
        // ARRANGE
        when(eventRepository. findById(2L)).thenReturn(Optional. of(eventPublie));
        when(reservationRepository.countByEvenementId(2L)). thenReturn(5L);

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            eventService.deleteEventSafely(2L, 1L);
        });

        assertTrue(exception.getMessage().contains("5 réservation(s)"));
        verify(eventRepository, never()).deleteById(any());
    }

    // ==================== TESTS calculateAvailablePlaces() ====================

    @Test
    @DisplayName("✅ Doit calculer les places disponibles")
    void testCalculateAvailablePlaces() {
        // ARRANGE
        when(eventRepository.findById(2L)).thenReturn(Optional. of(eventPublie));
        when(reservationRepository.calculateTotalPlacesReserved(2L)).thenReturn(30);

        // ACT
        int placesDisponibles = eventService. calculateAvailablePlaces(2L);

        // ASSERT
        assertEquals(170, placesDisponibles); // 200 - 30
    }

    @Test
    @DisplayName("✅ Doit retourner 0 si aucune réservation")
    void testCalculateAvailablePlaces_NoReservations() {
        // ARRANGE
        when(eventRepository.findById(2L)).thenReturn(Optional.of(eventPublie));
        when(reservationRepository.calculateTotalPlacesReserved(2L)).thenReturn(null);

        // ACT
        int placesDisponibles = eventService.calculateAvailablePlaces(2L);

        // ASSERT
        assertEquals(200, placesDisponibles); // Capacité max
    }

    @Test
    @DisplayName("✅ Doit retourner 0 si surréservation (protection)")
    void testCalculateAvailablePlaces_Overbooking() {
        // ARRANGE
        when(eventRepository.findById(2L)).thenReturn(Optional.of(eventPublie));
        when(reservationRepository.calculateTotalPlacesReserved(2L)). thenReturn(250); // > capacité

        // ACT
        int placesDisponibles = eventService. calculateAvailablePlaces(2L);

        // ASSERT
        assertEquals(0, placesDisponibles); // Math.max(0, -50) = 0
    }

    // ==================== TESTS getPopularEvents() ====================

    @Test
    @DisplayName("✅ Doit retourner les événements les plus populaires")
    void testGetPopularEvents() {
        // ARRANGE
        Event event1 = new Event();
        event1.setId(1L);
        event1.setStatut(EventStatus.PUBLIE);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setStatut(EventStatus. PUBLIE);

        Event event3 = new Event();
        event3.setId(3L);
        event3.setStatut(EventStatus.PUBLIE);

        when(eventRepository.findByStatut(EventStatus.PUBLIE))
                .thenReturn(Arrays.asList(event1, event2, event3));

        when(reservationRepository.countByEvenementId(1L)).thenReturn(50L);
        when(reservationRepository.countByEvenementId(2L)).thenReturn(100L);
        when(reservationRepository.countByEvenementId(3L)).thenReturn(30L);

        // ACT
        List<Event> popular = eventService.getPopularEvents(2);

        // ASSERT
        assertEquals(2, popular.size());
        assertEquals(2L, popular.get(0). getId()); // Event2 (100 réservations)
        assertEquals(1L, popular.get(1). getId()); // Event1 (50 réservations)
    }

    // ==================== TESTS getOrganizerStatistics() ====================

    @Test
    @DisplayName("✅ Doit calculer les statistiques d'un organisateur")
    void testGetOrganizerStatistics() {
        // ARRANGE
        Event event1 = new Event();
        event1.setId(1L);
        event1.setStatut(EventStatus. PUBLIE);

        Event event2 = new Event();
        event2.setId(2L);
        event2.setStatut(EventStatus. BROUILLON);

        when(eventRepository.findByOrganisateurId(1L))
                .thenReturn(Arrays.asList(event1, event2));

        Reservation resa1 = new Reservation();
        resa1.setStatut(ReservationStatus. CONFIRMEE);
        resa1.setMontantTotal(100.0);

        when(reservationRepository.findByEvenementId(1L))
                .thenReturn(Collections.singletonList(resa1));
        when(reservationRepository.findByEvenementId(2L))
                .thenReturn(Collections.emptyList());

        when(reservationRepository.countByEvenementId(1L)). thenReturn(1L);
        when(reservationRepository.countByEvenementId(2L)).thenReturn(0L);

        // ACT
        Map<String, Object> stats = eventService.getOrganizerStatistics(1L);

        // ASSERT - ✅ CORRECTION : Cast explicite en Integer
        assertEquals(2, (Integer) stats.get("totalEvents"));
        assertEquals(1L, (Long) stats.get("publishedEvents"));
        assertEquals(1L, (Long) stats. get("draftEvents"));
        assertEquals(0L, (Long) stats.get("cancelledEvents"));
        assertEquals(0L, (Long) stats.get("finishedEvents"));
        assertEquals(100.0, (Double) stats.get("totalRevenue"), 0.01);
        assertEquals(1L, (Long) stats. get("totalReservations"));
    }

    // ==================== TESTS searchWithFilters() ====================

    @Test
    @DisplayName("✅ Doit filtrer par catégorie")
    void testSearchWithFilters_ByCategory() {
        // ARRANGE
        Event concert = new Event();
        concert. setStatut(EventStatus.PUBLIE);
        concert.setCategorie(EventCategory.CONCERT);

        Event theatre = new Event();
        theatre.setStatut(EventStatus.PUBLIE);
        theatre.setCategorie(EventCategory.THEATRE);

        when(eventRepository.findByStatut(EventStatus.PUBLIE))
                .thenReturn(Arrays.asList(concert, theatre));

        // ACT
        List<Event> results = eventService.searchWithFilters(
                EventCategory.CONCERT, null, null, null, null, null
        );

        // ASSERT
        assertEquals(1, results.size());
        assertEquals(EventCategory.CONCERT, results.get(0).getCategorie());
    }

    @Test
    @DisplayName("✅ Doit filtrer par ville")
    void testSearchWithFilters_ByCity() {
        // ARRANGE
        Event casaEvent = new Event();
        casaEvent.setStatut(EventStatus.PUBLIE);
        casaEvent.setVille("Casablanca");

        Event rabatEvent = new Event();
        rabatEvent.setStatut(EventStatus.PUBLIE);
        rabatEvent.setVille("Rabat");

        when(eventRepository.findByStatut(EventStatus.PUBLIE))
                .thenReturn(Arrays.asList(casaEvent, rabatEvent));

        // ACT
        List<Event> results = eventService.searchWithFilters(
                null, "Casablanca", null, null, null, null
        );

        // ASSERT
        assertEquals(1, results.size());
        assertEquals("Casablanca", results.get(0).getVille());
    }

    @Test
    @DisplayName("✅ Doit filtrer par prix max")
    void testSearchWithFilters_ByMaxPrice() {
        // ARRANGE
        Event cheapEvent = new Event();
        cheapEvent.setStatut(EventStatus.PUBLIE);
        cheapEvent.setPrixUnitaire(50.0);

        Event expensiveEvent = new Event();
        expensiveEvent. setStatut(EventStatus. PUBLIE);
        expensiveEvent.setPrixUnitaire(200.0);

        when(eventRepository.findByStatut(EventStatus.PUBLIE))
                .thenReturn(Arrays.asList(cheapEvent, expensiveEvent));

        // ACT
        List<Event> results = eventService.searchWithFilters(
                null, null, null, null, null, 100.0
        );

        // ASSERT
        assertEquals(1, results.size());
        assertTrue(results.get(0).getPrixUnitaire() <= 100.0);
    }
}