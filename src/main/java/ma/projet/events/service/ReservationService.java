package ma.projet.events.service;

import ma.projet.events.entity.*;
import ma.projet.events.exception.*;
import ma.projet.events.repository.EventRepository;
import ma.projet.events.repository.ReservationRepository;
import ma.projet.events.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              EventRepository eventRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    // ==================== MÉTHODE PRINCIPALE : RÉSERVER UN TICKET ====================

    @Transactional
    public Reservation reserverTicket(Long eventId, Long userId, int nombrePlacesDemande) {

        // ========== VÉRIFICATIONS DE BASE ==========

        // 1. Vérifier que l'événement existe
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        // 2. Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // ========== RÈGLES MÉTIER CRITIQUES ==========

        // RÈGLE 1 : Minimum 1 place
        if (nombrePlacesDemande < 1) {
            throw new BusinessException("Le nombre de places doit être au moins 1");
        }

        // RÈGLE 2 : Maximum 10 places par réservation
        if (nombrePlacesDemande > 10) {
            throw new BusinessException("Impossible de réserver plus de 10 places par réservation.");
        }

        // RÈGLE 3 : L'événement ne doit pas être TERMINE
        if (event.getStatut() == EventStatus.TERMINE) {
            throw new BusinessException("Cet événement est terminé, réservation impossible");
        }

        // RÈGLE 4 : L'événement ne doit pas être ANNULE
        if (event.getStatut() == EventStatus.ANNULE) {
            throw new BusinessException("Cet événement a été annulé");
        }

        // RÈGLE 5 : L'événement doit être PUBLIE
        if (event.getStatut() != EventStatus.PUBLIE) {
            throw new BusinessException("Cet événement n'est pas encore disponible à la réservation.");
        }

        // RÈGLE 6 : L'événement doit être dans le futur
        if (event.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Impossible de réserver : cet événement a déjà commencé");
        }

        // ========== VÉRIFICATION DE LA DISPONIBILITÉ ==========

        Integer placesDejaReservees = reservationRepository.calculateTotalPlacesReserved(eventId);
        if (placesDejaReservees == null) {
            placesDejaReservees = 0;
        }

        int placesRestantes = event.getCapaciteMax() - placesDejaReservees;

        // RÈGLE 7 : Vérifier qu'il reste assez de places
        if (nombrePlacesDemande > placesRestantes) {
            throw new BusinessException(
                    String.format("Désolé, impossible de réserver %d place(s). Il ne reste que %d place(s) disponible(s).",
                            nombrePlacesDemande, placesRestantes)
            );
        }

        // ========== VÉRIFICATION DOUBLON (CORRIGÉ) ==========

        // On récupère la LISTE des réservations existantes pour ce couple user/event
        List<Reservation> existingReservations = reservationRepository
                .findByUtilisateurIdAndEvenementId(userId, eventId);

        // On vérifie s'il y a AU MOINS UNE réservation active (non annulée)
        Optional<Reservation> reservationActive = existingReservations.stream()
                .filter(r -> r.getStatut() != ReservationStatus.ANNULEE)
                .findFirst();

        if (reservationActive.isPresent()) {
            throw new ConflictException(
                    "Vous avez déjà une réservation active pour cet événement (Code : " +
                            reservationActive.get().getCodeReservation() + ")"
            );
        }

        // ========== CRÉATION DE LA RÉSERVATION ==========

        Reservation reservation = new Reservation();
        reservation.setEvenement(event);
        reservation.setUtilisateur(user);
        reservation.setNombrePlaces(nombrePlacesDemande);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(ReservationStatus.CONFIRMEE);

        // RÈGLE 8 : Calcul automatique du montant total
        if (event.getPrixUnitaire() != null) {
            double montantTotal = event.getPrixUnitaire() * nombrePlacesDemande;
            reservation.setMontantTotal(montantTotal);
        } else {
            reservation.setMontantTotal(0.0);
        }

        // RÈGLE 9 : Génération du code unique
        String codeUnique = generateUniqueReservationCode();
        reservation.setCodeReservation(codeUnique);

        // Sauvegarder et retourner
        Reservation savedReservation = reservationRepository.save(reservation);

        System.out.println("✅ Réservation créée avec succès : " + codeUnique);

        return savedReservation;
    }

    // ==================== ANNULER UNE RÉSERVATION ====================

    @Transactional
    public void annulerReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'ID : " + reservationId));

        if (!reservation.getUtilisateur().getId().equals(userId)) {
            throw new UnauthorizedException("Vous ne pouvez annuler que vos propres réservations");
        }

        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // Règle 48h
        LocalDateTime limiteAnnulation = reservation.getEvenement()
                .getDateDebut()
                .minusHours(48);

        if (LocalDateTime.now().isAfter(limiteAnnulation)) {
            long heuresRestantes = java.time.temporal.ChronoUnit.HOURS.between(
                    LocalDateTime.now(),
                    reservation.getEvenement().getDateDebut()
            );
            // Pour l'affichage positif (0 si négatif)
            heuresRestantes = Math.max(0, heuresRestantes);

            throw new BusinessException(
                    "Impossible d'annuler : le délai de 48h avant l'événement est dépassé."
            );
        }

        reservation.setStatut(ReservationStatus.ANNULEE);
        reservation.setCommentaire("Annulée par l'utilisateur le " + LocalDateTime.now());
        reservationRepository.save(reservation);

        System.out.println("✅ Réservation " + reservation.getCodeReservation() + " annulée avec succès");
    }

    // ==================== CONFIRMER UNE RÉSERVATION ====================

    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        if (reservation.getStatut() != ReservationStatus.EN_ATTENTE) {
            throw new BusinessException("Seule une réservation en attente peut être confirmée");
        }

        reservation.setStatut(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    // ==================== RÉCUPÉRATION DE RÉSERVATIONS ====================

    public List<Reservation> findUserReservations(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userId));
        return reservationRepository.findByUtilisateurId(userId);
    }

    public Reservation getReservationByCode(String code) {
        return reservationRepository.findByCodeReservation(code)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune réservation trouvée avec le code : " + code));
    }

    public List<Reservation> findEventReservations(Long eventId) {
        return reservationRepository.findByEvenementId(eventId);
    }

    public List<Reservation> getReservationsWithFilters(Long userId, Long eventId, ReservationStatus statut) {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .filter(r -> userId == null || r.getUtilisateur().getId().equals(userId))
                .filter(r -> eventId == null || r.getEvenement().getId().equals(eventId))
                .filter(r -> statut == null || r.getStatut().equals(statut))
                .collect(Collectors.toList());
    }

    // ==================== RÉCAPITULATIF ====================

    public Map<String, Object> getReservationSummary(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        Map<String, Object> summary = new HashMap<>();
        summary.put("code", reservation.getCodeReservation());
        summary.put("eventTitle", reservation.getEvenement().getTitre());
        summary.put("eventDate", reservation.getEvenement().getDateDebut());
        summary.put("eventLocation", reservation.getEvenement().getLieu());
        summary.put("eventCity", reservation.getEvenement().getVille());
        summary.put("nombrePlaces", reservation.getNombrePlaces());
        summary.put("montantTotal", reservation.getMontantTotal());
        summary.put("statut", reservation.getStatut());
        summary.put("statutLabel", reservation.getStatut().getLabel());
        summary.put("dateReservation", reservation.getDateReservation());
        summary.put("clientName", reservation.getUtilisateur().getNomComplet());
        summary.put("clientEmail", reservation.getUtilisateur().getEmail());

        // Calcul pour isAnnulable
        boolean isAnnulable = reservation.getStatut() != ReservationStatus.ANNULEE &&
                LocalDateTime.now().isBefore(reservation.getEvenement().getDateDebut().minusHours(48));

        summary.put("isAnnulable", isAnnulable);

        return summary;
    }

    // ==================== STATISTIQUES ====================

    public Map<String, Object> getReservationStatistics() {
        List<Reservation> allReservations = reservationRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReservations", allReservations.size());

        long confirmed = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .count();
        stats.put("confirmedReservations", confirmed);

        long cancelled = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.ANNULEE)
                .count();
        stats.put("cancelledReservations", cancelled);

        long pending = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE)
                .count();
        stats.put("pendingReservations", pending);

        double totalRevenue = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    /**
     * Vérifier si un utilisateur a déjà réservé pour un événement (CORRIGÉ)
     */
    public boolean hasUserReservedEvent(Long userId, Long eventId) {
        return reservationRepository.findByUtilisateurIdAndEvenementId(userId, eventId)
                .stream()
                .anyMatch(r -> r.getStatut() != ReservationStatus.ANNULEE);
    }

    // ==================== MÉTHODE PRIVÉE : GÉNÉRATION CODE ====================

    private String generateUniqueReservationCode() {
        String code;
        int tentatives = 0;
        final int MAX_TENTATIVES = 100;

        do {
            Random random = new Random();
            int number = random.nextInt(100000);
            code = String.format("EVT-%05d", number);

            tentatives++;
            if (tentatives >= MAX_TENTATIVES) {
                throw new BusinessException("Erreur lors de la génération du code unique.");
            }

        } while (reservationRepository.findByCodeReservation(code).isPresent());

        return code;
    }

    public Map<String, Object> getReservationStatisticsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable"));

        List<Reservation> reservations = reservationRepository.findByEvenementId(eventId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("eventId", eventId);
        stats.put("eventTitle", event.getTitre());
        stats.put("totalReservations", reservations.size());

        long confirmed = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .count();
        stats.put("reservationsConfirmees", confirmed);

        long cancelled = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.ANNULEE)
                .count();
        stats.put("reservationsAnnulees", cancelled);

        int totalPlaces = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        stats.put("totalPlaces", totalPlaces);

        double totalRevenue = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
}