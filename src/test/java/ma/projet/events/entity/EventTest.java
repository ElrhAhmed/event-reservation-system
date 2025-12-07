package ma.projet.events.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api. DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de l'entité Event")
class EventTest {

    private Event event;
    private User organisateur;

    @BeforeEach
    void setUp() {
        // Créer un organisateur
        organisateur = new User();
        organisateur.setId(1L);
        organisateur.setNom("Organisateur");
        organisateur.setPrenom("Test");
        organisateur.setEmail("org@test.com");
        organisateur.setRole(Role. ORGANIZER);

        // Créer un événement
        event = new Event();
        event.setId(1L);
        event.setTitre("Concert de Rock");
        event.setDescription("Un super concert de rock");
        event.setCategorie(EventCategory.CONCERT);
        event. setDateDebut(LocalDateTime. now().plusDays(10));
        event.setDateFin(LocalDateTime.now().plusDays(10).plusHours(3));
        event.setLieu("Stade Mohammed V");
        event.setVille("Casablanca");
        event.setCapaciteMax(5000);
        event.setPrixUnitaire(150.0);
        event.setStatut(EventStatus.PUBLIE);
        event.setOrganisateur(organisateur);
        event.setImageUrl("https://example.com/image.jpg");
    }

    // ==================== TEST 1 : Méthode isReservable ====================

    @Test
    @DisplayName("Un événement PUBLIÉ dans le futur doit être réservable")
    void testIsReservable_PublieEtFutur() {
        // ARRANGE
        event.setStatut(EventStatus.PUBLIE);
        event.setDateDebut(LocalDateTime.now().plusDays(5));

        // ACT
        boolean reservable = event.isReservable();

        // ASSERT
        assertTrue(reservable, "Un événement publié dans le futur doit être réservable");
    }

    @Test
    @DisplayName("Un événement BROUILLON ne doit PAS être réservable")
    void testIsReservable_Brouillon() {
        // ARRANGE
        event.setStatut(EventStatus.BROUILLON);
        event.setDateDebut(LocalDateTime.now(). plusDays(5));

        // ACT
        boolean reservable = event.isReservable();

        // ASSERT
        assertFalse(reservable, "Un événement en brouillon ne doit pas être réservable");
    }

    @Test
    @DisplayName("Un événement PUBLIÉ mais passé ne doit PAS être réservable")
    void testIsReservable_PublieMaisPasse() {
        // ARRANGE
        event.setStatut(EventStatus.PUBLIE);
        event.setDateDebut(LocalDateTime.now().minusDays(1)); // Hier

        // ACT
        boolean reservable = event.isReservable();

        // ASSERT
        assertFalse(reservable, "Un événement passé ne doit pas être réservable");
    }

    // ==================== TEST 2 : Méthode isModifiable ====================

    @Test
    @DisplayName("Un événement non TERMINÉ doit être modifiable")
    void testIsModifiable_NonTermine() {
        // ARRANGE
        event.setStatut(EventStatus.PUBLIE);

        // ACT
        boolean modifiable = event.isModifiable();

        // ASSERT
        assertTrue(modifiable, "Un événement non terminé doit être modifiable");
    }

    @Test
    @DisplayName("Un événement TERMINÉ ne doit PAS être modifiable")
    void testIsModifiable_Termine() {
        // ARRANGE
        event.setStatut(EventStatus.TERMINE);

        // ACT
        boolean modifiable = event.isModifiable();

        // ASSERT
        assertFalse(modifiable, "Un événement terminé ne doit pas être modifiable");
    }

    // ==================== TEST 3 : Méthode isTermine ====================

    @Test
    @DisplayName("Doit détecter un événement avec statut TERMINÉ")
    void testIsTermine_StatutTermine() {
        // ARRANGE
        event.setStatut(EventStatus.TERMINE);

        // ACT
        boolean termine = event.isTermine();

        // ASSERT
        assertTrue(termine);
    }

    @Test
    @DisplayName("Doit détecter un événement dont la date de fin est passée")
    void testIsTermine_DatePassee() {
        // ARRANGE : Créer un événement qui s'est terminé il y a 1h
        event.setStatut(EventStatus.PUBLIE);

        // ✅ CORRECTION : Définir dateDebut ET dateFin dans le passé
        LocalDateTime dateDebutPassee = LocalDateTime.now().minusHours(3); // Il y a 3h
        LocalDateTime dateFinPassee = LocalDateTime.now().minusHours(1);   // Il y a 1h

        event.setDateDebut(dateDebutPassee);
        event.setDateFin(dateFinPassee); // OK car dateFin > dateDebut

        // ACT
        boolean termine = event.isTermine();

        // ASSERT
        assertTrue(termine, "Un événement dont la date de fin est passée est terminé");
    }

    // ==================== TEST 4 : Méthode getJoursRestants ====================

    @Test
    @DisplayName("Doit calculer correctement les jours restants")
    void testGetJoursRestants() {
        // ARRANGE
        event.setDateDebut(LocalDateTime.now().plusDays(7));

        // ACT
        long joursRestants = event.getJoursRestants();

        // ASSERT
        assertEquals(7, joursRestants, "Il doit rester 7 jours");
    }

    @Test
    @DisplayName("Doit retourner un nombre négatif si l'événement est passé")
    void testGetJoursRestants_Passe() {
        // ARRANGE
        event.setDateDebut(LocalDateTime.now().minusDays(3));

        // ACT
        long joursRestants = event. getJoursRestants();

        // ASSERT
        assertTrue(joursRestants < 0, "Les jours restants doivent être négatifs");
    }

    // ==================== TEST 5 : Méthode getDureeEnHeures ====================

    @Test
    @DisplayName("Doit calculer la durée de l'événement en heures")
    void testGetDureeEnHeures() {
        // ARRANGE
        LocalDateTime debut = LocalDateTime.of(2026, 1, 15, 20, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 1, 15, 23, 0);
        event.setDateDebut(debut);
        event.setDateFin(fin);

        // ACT
        long duree = event.getDureeEnHeures();

        // ASSERT
        assertEquals(3, duree, "La durée doit être de 3 heures");
    }

    // ==================== TEST 6 : Validation des Dates ====================

    @Test
    @DisplayName("setDateFin doit rejeter une date avant dateDebut")
    void testSetDateFin_AvantDateDebut() {
        // ARRANGE
        LocalDateTime debut = LocalDateTime.of(2026, 1, 15, 20, 0);
        LocalDateTime finInvalide = LocalDateTime.of(2026, 1, 15, 18, 0); // 2h avant

        event.setDateDebut(debut);

        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, () -> {
            event.setDateFin(finInvalide);
        }, "setDateFin doit rejeter une date avant dateDebut");
    }

    @Test
    @DisplayName("setDateFin doit accepter une date après dateDebut")
    void testSetDateFin_ApresDateDebut() {
        // ARRANGE
        LocalDateTime debut = LocalDateTime.of(2026, 1, 15, 20, 0);
        LocalDateTime finValide = LocalDateTime.of(2026, 1, 15, 23, 0);

        event.setDateDebut(debut);

        // ACT & ASSERT
        assertDoesNotThrow(() -> {
            event.setDateFin(finValide);
        });
        assertEquals(finValide, event.getDateFin());
    }

    // ==================== TEST 7 : Méthode isReadyToPublish ====================

    @Test
    @DisplayName("Un événement complet doit être prêt à publier")
    void testIsReadyToPublish_Complet() {
        // ARRANGE : événement avec toutes les infos
        event.setStatut(EventStatus.PUBLIE);

        // ACT
        boolean ready = event.isReadyToPublish();

        // ASSERT
        assertTrue(ready, "Un événement complet doit être prêt à publier");
    }

    @Test
    @DisplayName("Un événement BROUILLON incomplet doit être accepté")
    void testIsReadyToPublish_BrouillonIncomplet() {
        // ARRANGE : événement en brouillon sans description
        event.setStatut(EventStatus.BROUILLON);
        event.setDescription(null);

        // ACT
        boolean ready = event.isReadyToPublish();

        // ASSERT
        assertTrue(ready, "Un brouillon peut être incomplet");
    }

    // ==================== TEST 8 : Validation des Champs ====================

    @Test
    @DisplayName("Les champs obligatoires doivent être présents")
    void testChampsObligatoires() {
        // ASSERT
        assertNotNull(event.getTitre());
        assertNotNull(event. getDateDebut());
        assertNotNull(event.getDateFin());
        assertNotNull(event.getLieu());
        assertNotNull(event.getVille());
        assertNotNull(event.getCapaciteMax());
        assertNotNull(event.getPrixUnitaire());
        assertNotNull(event.getStatut());
    }

    @Test
    @DisplayName("La capacité maximale doit être positive")
    void testCapaciteMaxPositive() {
        // ACT
        Integer capacite = event.getCapaciteMax();

        // ASSERT
        assertNotNull(capacite);
        assertTrue(capacite > 0, "La capacité doit être positive");
    }

    @Test
    @DisplayName("Le prix unitaire doit être positif ou nul")
    void testPrixUnitairePositif() {
        // ACT
        Double prix = event.getPrixUnitaire();

        // ASSERT
        assertNotNull(prix);
        assertTrue(prix >= 0, "Le prix doit être positif ou nul");
    }

    // ==================== TEST 9 : Enums ====================

    @Test
    @DisplayName("Les enums doivent avoir des labels")
    void testEnumsLabels() {
        // ASSERT
        assertNotNull(event.getStatut(). getLabel());
        assertNotNull(event.getStatut().getColor());
        assertNotNull(event.getCategorie().getLabel());
        assertNotNull(event.getCategorie().getIcon());
    }
}