package ma.projet.events.repository;

import ma.projet.events.entity.Event;
import ma.projet.events.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 1. Méthode Magique : Trouver les événements par statut (ex: PUBLIE)
    List<Event> findByStatut(EventStatus statut);

    // 2. Méthode Magique : Trouver les événements d'un organisateur spécifique
    List<Event> findByOrganisateurId(Long organisateurId);

    // 3. Méthode Manuelle (@Query) : Moteur de recherche
    // On veut chercher un mot clé SOIT dans le titre, SOIT dans la description
    // ET on veut seulement les événements qui sont 'PUBLIE'
    @Query("SELECT e FROM Event e WHERE " +
            "(LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND e.statut = 'PUBLIE'")
    List<Event> search(@Param("keyword") String keyword);
}