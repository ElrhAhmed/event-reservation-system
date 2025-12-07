package ma.projet.events.repository;

import ma.projet.events.entity.Reservation;
import ma. projet.events.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ==================== MÉTHODES DE BASE ====================

    /**
     * Trouver toutes les réservations d'un utilisateur
     * Utilisé dans : ReservationService.findUserReservations()
     */
    List<Reservation> findByUtilisateurId(Long utilisateurId);

    /**
     * Trouver toutes les réservations d'un événement
     * Utilisé dans : ReservationService.findEventReservations()
     */
    List<Reservation> findByEvenementId(Long evenementId);

    /**
     * Trouver une réservation par son code unique
     * Utilisé dans : ReservationService.getReservationByCode()
     */
    Optional<Reservation> findByCodeReservation(String codeReservation);

    // ==================== MÉTHODES AVEC FILTRES ====================

    /**
     * Trouver les réservations d'un événement avec un statut donné
     * Exemple : Toutes les réservations CONFIRMÉES d'un événement
     */
    List<Reservation> findByEvenementIdAndStatut(Long evenementId, ReservationStatus statut);

    /**
     * Trouver les réservations d'un utilisateur avec un statut donné
     * Exemple : Toutes les réservations CONFIRMÉES d'un utilisateur
     */
    List<Reservation> findByUtilisateurIdAndStatut(Long userId, ReservationStatus statut);

    /**
     * Vérifier si un utilisateur a déjà réservé pour un événement
     * Utilisé dans : ReservationService.reserverTicket() pour éviter doublons
     */
    Optional<Reservation> findByUtilisateurIdAndEvenementId(Long userId, Long eventId);

    // ==================== MÉTHODES AVEC @Query ====================

    /**
     * CRITIQUE : Calculer le nombre total de places réservées pour un événement
     * (en excluant les réservations annulées)
     *
     * Utilisé dans : ReservationService.reserverTicket() pour vérifier disponibilité
     *
     * COALESCE retourne 0 si la somme est NULL (aucune réservation)
     *
     * @param eventId ID de l'événement
     * @return Nombre total de places réservées (0 si aucune)
     */
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r " +
            "WHERE r.evenement.id = :eventId " +
            "AND r.statut != 'ANNULEE'")
    Integer calculateTotalPlacesReserved(@Param("eventId") Long eventId);

    /**
     * Calculer le montant total dépensé par un utilisateur
     * (seulement les réservations CONFIRMÉES)
     *
     * Utilisé dans : UserService.getUserStatistics()
     *
     * @param userId ID de l'utilisateur
     * @return Montant total dépensé (0. 0 si aucune réservation)
     */
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r " +
            "WHERE r.utilisateur.id = :userId " +
            "AND r.statut = 'CONFIRMEE'")
    Double calculateTotalAmountByUser(@Param("userId") Long userId);

    // ==================== MÉTHODES DE COMPTAGE ====================

    /**
     * Compter le nombre de réservations d'un utilisateur
     * Utilisé dans : UserService.getUserStatistics()
     */
    Long countByUtilisateurId(Long userId);

    /**
     * Compter le nombre de réservations d'un événement
     * Utilisé dans : EventService.deleteEventSafely()
     */
    Long countByEvenementId(Long eventId);

    /**
     * Compter les réservations par statut (pour statistiques globales)
     */
    Long countByStatut(ReservationStatus statut);

    // ==================== RECHERCHE PAR DATE ====================

    /**
     * Trouver les réservations effectuées entre deux dates
     * Utilisé pour : Rapports, statistiques mensuelles
     */
    List<Reservation> findByDateReservationBetween(LocalDateTime dateDebut, LocalDateTime dateFin);

    /**
     * Trouver les réservations pour des événements se déroulant entre deux dates
     * Utile pour : Afficher les événements à venir d'un utilisateur
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r. evenement.dateDebut BETWEEN :dateDebut AND :dateFin " +
            "AND r.statut != 'ANNULEE'")
    List<Reservation> findByEventDateBetween(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    // ==================== MÉTHODES AVANCÉES ====================

    /**
     * Trouver les réservations confirmées d'un utilisateur triées par date d'événement
     * Utilisé dans : Dashboard utilisateur pour "Mes prochains événements"
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r. utilisateur.id = :userId " +
            "AND r.statut = 'CONFIRMEE' " +
            "AND r.evenement.dateDebut > CURRENT_TIMESTAMP " +
            "ORDER BY r. evenement.dateDebut ASC")
    List<Reservation> findUpcomingReservationsByUser(@Param("userId") Long userId);

    /**
     * Trouver les réservations qui arrivent bientôt (dans les X jours)
     * Utilisé pour : Envoyer des rappels par email
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.statut = 'CONFIRMEE' " +
            "AND r.evenement.dateDebut BETWEEN CURRENT_TIMESTAMP AND :dateLimit")
    List<Reservation> findReservationsComingSoon(@Param("dateLimit") LocalDateTime dateLimit);

    /**
     * Obtenir les statistiques par événement (pour organisateur)
     * Retourne : eventId, nombre de réservations, total places, revenu total
     */
    @Query("SELECT r.evenement.id, COUNT(r), SUM(r.nombrePlaces), SUM(r.montantTotal) " +
            "FROM Reservation r " +
            "WHERE r.evenement.organisateur.id = :organizerId " +
            "AND r.statut = 'CONFIRMEE' " +
            "GROUP BY r.evenement.id")
    List<Object[]> getStatisticsByOrganizer(@Param("organizerId") Long organizerId);

    /**
     * Vérifier s'il existe des réservations non annulées pour un événement
     * Utilisé dans : EventService.deleteEventSafely()
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.evenement.id = :eventId " +
            "AND r.statut != 'ANNULEE'")
    boolean hasActiveReservations(@Param("eventId") Long eventId);

    /**
     * Obtenir les X dernières réservations d'un utilisateur
     * Utilisé dans : Dashboard pour afficher l'historique
     */
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.utilisateur.id = :userId " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findRecentReservationsByUser(@Param("userId") Long userId);

    /**
     * Calculer le taux de remplissage d'un événement
     * Retourne le pourcentage de places réservées (0-100)
     */
    @Query("SELECT (CAST(SUM(r.nombrePlaces) AS double) / e.capaciteMax) * 100 " +
            "FROM Reservation r " +
            "JOIN r.evenement e " +
            "WHERE r.evenement.id = :eventId " +
            "AND r.statut != 'ANNULEE'")
    Double calculateFillRate(@Param("eventId") Long eventId);
}