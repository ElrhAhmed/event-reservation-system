package ma.projet.events.repository;

import ma.projet.events.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 1. Trouver toutes les réservations d'un client spécifique
    // (Pour la page "Mes Réservations")
    List<Reservation> findByUtilisateurId(Long userId);

    // 2. Trouver toutes les réservations pour un événement
    // (Utile pour calculer si la salle est pleine : on compte le total des places de cette liste)
    List<Reservation> findByEvenementId(Long eventId);
}