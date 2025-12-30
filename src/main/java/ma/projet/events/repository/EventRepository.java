package ma.projet.events.repository;

import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventCategory;
import ma.projet.events.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository // ← Indique à Spring que c'est un Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // ↑ JpaRepository<Event, Long> signifie :
    //   - On gère l'entité Event
    //   - L'ID est de type Long
    //   - Spring génère automatiquement : findById, save, delete, findAll, etc.

    // ==================== MÉTHODES MAGIQUES (Spring génère le SQL) ====================

    /**
     * LIGNE 1 : Trouver tous les événements d'une catégorie
     *
     * Exemple : findByCategorie(EventCategory.CONCERT)
     * SQL généré : SELECT * FROM events WHERE categorie = 'CONCERT'
     */
    List<Event> findByCategorie(EventCategory categorie);
    // ↑ Spring lit "findBy" + "Categorie" = recherche par le champ "categorie" de Event

    /**
     * LIGNE 2 : Trouver tous les événements d'un organisateur
     *
     * Exemple : findByOrganisateurId(5L)
     * SQL : SELECT * FROM events WHERE organisateur_id = 5
     */
    List<Event> findByOrganisateurId(Long organisateurId);
    // ↑ "OrganisateurId" = Spring comprend qu'il faut suivre la relation @ManyToOne
    //   vers User et chercher par l'ID de l'organisateur

    /**
     * LIGNE 3 : Trouver événements d'un organisateur avec un statut précis
     *
     * Exemple : findByOrganisateurIdAndStatut(5L, EventStatus.PUBLIE)
     * SQL : SELECT * FROM events WHERE organisateur_id = 5 AND statut = 'PUBLIE'
     */
    List<Event> findByOrganisateurIdAndStatut(Long organisateurId, EventStatus statut);
    // ↑ "And" = ET logique (les 2 conditions doivent être vraies)

    /**
     * LIGNE 4 : Trouver événements par statut
     *
     * Exemple : findByStatut(EventStatus.PUBLIE)
     * SQL : SELECT * FROM events WHERE statut = 'PUBLIE'
     */
    List<Event> findByStatut(EventStatus statut);

    /**
     * LIGNE 5 : Compter les événements d'une catégorie
     *
     * Exemple : countByCategorie(EventCategory. CONCERT)
     * SQL : SELECT COUNT(*) FROM events WHERE categorie = 'CONCERT'
     * Retourne : un nombre (exemple : 15)
     */
    Long countByCategorie(EventCategory categorie);
    // ↑ Retourne Long (pas List) car c'est un comptage

    /**
     * LIGNE 6 : Rechercher par lieu OU ville (contient le texte, insensible à la casse)
     *
     * Exemple : findByLieuContainingIgnoreCaseOrVilleContainingIgnoreCase("casa", "casa")
     * SQL : SELECT * FROM events
     *       WHERE LOWER(lieu) LIKE '%casa%'
     *       OR LOWER(ville) LIKE '%casa%'
     */
    List<Event> findByLieuContainingIgnoreCaseOrVilleContainingIgnoreCase(String lieu, String ville);
    // ↑ Décomposition du nom :
    //   - findBy = recherche
    //   - Lieu = champ "lieu"
    //   - Containing = LIKE '%texte%' (contient)
    //   - IgnoreCase = insensible à la casse
    //   - Or = OU logique
    //   - Ville = champ "ville"
    //   - Containing = LIKE '%texte%'
    //   - IgnoreCase = insensible à la casse

    /**
     * LIGNE 7 : Rechercher par titre (contient le texte)
     *
     * Exemple : findByTitreContainingIgnoreCase("rock")
     * SQL : SELECT * FROM events WHERE LOWER(titre) LIKE '%rock%'
     */
    List<Event> findByTitreContainingIgnoreCase(String titre);

    /**
     * LIGNE 8 : Rechercher par plage de prix
     *
     * Exemple : findByPrixUnitaireBetween(50.0, 150.0)
     * SQL : SELECT * FROM events WHERE prix_unitaire BETWEEN 50.0 AND 150.0
     */
    List<Event> findByPrixUnitaireBetween(Double prixMin, Double prixMax);
    // ↑ "Between" = ENTRE deux valeurs (inclus)

    // ==================== MÉTHODES @Query (On écrit le SQL nous-mêmes) ====================

    /**
     * LIGNE 9 : Événements publiés entre deux dates
     *
     * Pourquoi @Query ? Car c'est une condition complexe (PUBLIE + date entre X et Y)
     *
     * Exemple : findPublishedEventsBetweenDates(01/01/2026, 31/01/2026)
     */
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' " +
            "AND e.dateDebut BETWEEN :debut AND :fin")
    // ↑ JPQL (pas SQL pur) :
    //   - "Event e" = alias pour l'entité Event
    //   - "e.statut" = accès au champ statut
    //   - ":debut" = paramètre nommé (lié à @Param("debut"))
    //   - "BETWEEN" = entre deux valeurs
    List<Event> findPublishedEventsBetweenDates(
            @Param("debut") LocalDateTime debut,
            // ↑ @Param("debut") lie ce paramètre à :debut dans la requête
            @Param("fin") LocalDateTime fin
    );

    /**
     * LIGNE 10 : Événements disponibles (PUBLIÉS + dans le futur)
     *
     * Pourquoi @Query ? Car on compare avec la date actuelle (CURRENT_TIMESTAMP)
     *
     * Exemple : findAvailableEvents()
     * Retourne SEULEMENT les événements réservables
     */
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' " +
            "AND e.dateDebut > CURRENT_TIMESTAMP")
    // ↑ CURRENT_TIMESTAMP = date/heure actuelle en base de données
    //   dateDebut > CURRENT_TIMESTAMP = événements futurs uniquement
    List<Event> findAvailableEvents();

    /**
     * LIGNE 11 : Événements publiés d'une ville
     *
     * Exemple : findPublishedEventsByVille("Casablanca")
     */
    @Query("SELECT e FROM Event e WHERE e.ville = :ville AND e.statut = 'PUBLIE'")
    List<Event> findPublishedEventsByVille(@Param("ville") String ville);

    /**
     * LIGNE 12 : Événements publiés d'une catégorie
     *
     * Exemple : findPublishedEventsByCategorie(EventCategory.CONCERT)
     */
    @Query("SELECT e FROM Event e WHERE e.categorie = :categorie AND e.statut = 'PUBLIE'")
    List<Event> findPublishedEventsByCategorie(@Param("categorie") EventCategory categorie);

    /**
     * LIGNE 13 : Moteur de recherche global (DÉJÀ DANS VOTRE CODE)
     *
     * Cherche dans le titre OU la description
     * Exemple : search("rock")
     */
    @Query("SELECT e FROM Event e WHERE " +
            "(LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND e.statut = 'PUBLIE'")
    // ↑ LOWER() = convertit en minuscules
    //   CONCAT('%', :keyword, '%') = ajoute % avant et après (LIKE '%rock%')
    List<Event> search(@Param("keyword") String keyword);
}