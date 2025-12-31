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

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {





     //  Trouver tous les événements d'une catégorie

    List<Event> findByCategorie(EventCategory categorie);
    // ↑ Spring lit "findBy" + "Categorie" = recherche par le champ "categorie" de Event


     //  Trouver tous les événements d'un organisateur

    List<Event> findByOrganisateurId(Long organisateurId);



     // Trouver événements d'un organisateur avec un statut précis

    List<Event> findByOrganisateurIdAndStatut(Long organisateurId, EventStatus statut);



     // Trouver événements par statut

    List<Event> findByStatut(EventStatus statut);


     //Compter les événements d'une catégorie

    Long countByCategorie(EventCategory categorie);



     //Rechercher par lieu OU ville (contient le texte, insensible à la casse)

    List<Event> findByLieuContainingIgnoreCaseOrVilleContainingIgnoreCase(String lieu, String ville);



     // Rechercher par titre (contient le texte)

    List<Event> findByTitreContainingIgnoreCase(String titre);


     //LIGNE 8 : Rechercher par plage de prix

    List<Event> findByPrixUnitaireBetween(Double prixMin, Double prixMax);



    // ==================== MÉTHODES==================================================


     //Événements publiés entre deux dates

    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' " +
            "AND e.dateDebut BETWEEN :debut AND :fin")

    List<Event> findPublishedEventsBetweenDates(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );

    /**
     * Événements disponibles (PUBLIÉS + dans le futur)
     */
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' " +
            "AND e.dateDebut > CURRENT_TIMESTAMP")
    List<Event> findAvailableEvents();


    // Événements publiés d'une ville

    @Query("SELECT e FROM Event e WHERE e.ville = :ville AND e.statut = 'PUBLIE'")
    List<Event> findPublishedEventsByVille(@Param("ville") String ville);


     // Événements publiés d'une catégorie

    @Query("SELECT e FROM Event e WHERE e.categorie = :categorie AND e.statut = 'PUBLIE'")
    List<Event> findPublishedEventsByCategorie(@Param("categorie") EventCategory categorie);

    //Moteur de recherche global (DÉJÀ DANS VOTRE CODE)

    @Query("SELECT e FROM Event e WHERE " +
            "(LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND e.statut = 'PUBLIE'")

    List<Event> search(@Param("keyword") String keyword);
}