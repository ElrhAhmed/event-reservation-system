package ma.projet.events.service;

import ma.projet.events. entity.*;
import ma.projet.events. exception.*;
import ma.projet.events. repository. EventRepository;
import ma.projet. events.repository.ReservationRepository;
import ma.projet.events.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction. annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream. Collectors;

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

    /**
     * Créer une réservation avec TOUTES les vérifications métier
     *
     * @param eventId ID de l'événement
     * @param userId ID de l'utilisateur
     * @param nombrePlacesDemande Nombre de places à réserver
     * @return La réservation créée
     * @throws ResourceNotFoundException Si événement ou utilisateur introuvable
     * @throws BusinessException Si règles métier non respectées
     */
    @Transactional
    public Reservation reserverTicket(Long eventId, Long userId, int nombrePlacesDemande) {

        // ========== VÉRIFICATIONS DE BASE ==========

        // 1. Vérifier que l'événement existe
        Event event = eventRepository. findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        // 2. Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // ========== RÈGLES MÉTIER CRITIQUES ==========

        // ❌ RÈGLE 1 : Minimum 1 place
        if (nombrePlacesDemande < 1) {
            throw new BusinessException("Le nombre de places doit être au moins 1");
        }

        // ❌ RÈGLE 2 : Maximum 10 places par réservation (CAHIER DES CHARGES)
        if (nombrePlacesDemande > 10) {
            throw new BusinessException("Impossible de réserver plus de 10 places par réservation.  Vous avez demandé " + nombrePlacesDemande + " places.");
        }

        // ✅ D'ABORD : Vérifier les statuts spécifiques (TERMINÉ, ANNULÉ)
        // ❌ RÈGLE 3 : L'événement ne doit pas être TERMINÉ
        if (event.getStatut() == EventStatus.TERMINE) {
            throw new BusinessException("Cet événement est terminé, réservation impossible");
        }

        // ❌ RÈGLE 4 : L'événement ne doit pas être ANNULÉ
        if (event.getStatut() == EventStatus.ANNULE) {
            throw new BusinessException("Cet événement a été annulé");
        }

        // ✅ ENSUITE : Vérifier que l'événement est PUBLIÉ (attrape BROUILLON et autres)
        // ❌ RÈGLE 5 : L'événement doit être PUBLIÉ
        if (event.getStatut() != EventStatus.PUBLIE) {
            throw new BusinessException("Cet événement n'est pas encore disponible à la réservation (Statut : " + event.getStatut().getLabel() + ")");
        }

        // ❌ RÈGLE 6 : L'événement doit être dans le futur
        if (event.getDateDebut(). isBefore(LocalDateTime.now())) {
            throw new BusinessException("Impossible de réserver : cet événement a déjà commencé");
        }

        // ========== VÉRIFICATION DE LA DISPONIBILITÉ ==========

        // Calculer le nombre de places déjà réservées (OPTIMISÉ avec repository)
        Integer placesDejaReservees = reservationRepository.calculateTotalPlacesReserved(eventId);
        if (placesDejaReservees == null) {
            placesDejaReservees = 0;
        }

        // Calculer les places restantes
        int placesRestantes = event.getCapaciteMax() - placesDejaReservees;

        // ❌ RÈGLE 7 : Vérifier qu'il reste assez de places
        if (nombrePlacesDemande > placesRestantes) {
            throw new BusinessException(
                    String.format("Désolé, impossible de réserver %d place(s).  Il ne reste que %d place(s) disponible(s) sur %d.",
                            nombrePlacesDemande, placesRestantes, event.getCapaciteMax())
            );
        }

        // ========== VÉRIFICATION DOUBLON (OPTIONNEL MAIS RECOMMANDÉ) ==========

        // Vérifier si l'utilisateur n'a pas déjà réservé pour cet événement
        Optional<Reservation> reservationExistante = reservationRepository
                .findByUtilisateurIdAndEvenementId(userId, eventId);

        if (reservationExistante.isPresent() &&
                reservationExistante. get().getStatut() != ReservationStatus.ANNULEE) {
            throw new ConflictException(
                    "Vous avez déjà une réservation pour cet événement (Code : " +
                            reservationExistante. get().getCodeReservation() + ")"
            );
        }

        // ========== CRÉATION DE LA RÉSERVATION ==========

        Reservation reservation = new Reservation();
        reservation.setEvenement(event);
        reservation.setUtilisateur(user);
        reservation.setNombrePlaces(nombrePlacesDemande);
        reservation. setDateReservation(LocalDateTime.now());
        reservation.setStatut(ReservationStatus. CONFIRMEE); // Directement confirmée

        // ❌ RÈGLE 8 : Calcul automatique du montant total
        if (event.getPrixUnitaire() != null) {
            double montantTotal = event.getPrixUnitaire() * nombrePlacesDemande;
            reservation. setMontantTotal(montantTotal);
        } else {
            reservation.setMontantTotal(0.0);
        }

        // ❌ RÈGLE 9 : Génération du code unique au format EVT-XXXXX (CAHIER DES CHARGES)
        String codeUnique = generateUniqueReservationCode();
        reservation.setCodeReservation(codeUnique);

        // Sauvegarder et retourner
        Reservation savedReservation = reservationRepository.save(reservation);

        System.out.println("✅ Réservation créée avec succès : " + codeUnique +
                " | " + nombrePlacesDemande + " place(s) | " +
                savedReservation.getMontantTotal() + " DH");

        return savedReservation;
    }

    // ==================== ANNULER UNE RÉSERVATION ====================

    /**
     * Annuler une réservation avec vérification de la règle des 48h
     *
     * @param reservationId ID de la réservation
     * @param userId ID de l'utilisateur (pour vérifier les droits)
     * @throws ResourceNotFoundException Si réservation introuvable
     * @throws UnauthorizedException Si l'utilisateur n'est pas le propriétaire
     * @throws BusinessException Si règles métier non respectées
     */
    @Transactional
    public void annulerReservation(Long reservationId, Long userId) {
        // 1. Vérifier que la réservation existe
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable avec l'ID : " + reservationId));

        // 2.  Vérifier que c'est bien l'utilisateur propriétaire
        if (! reservation.getUtilisateur(). getId().equals(userId)) {
            throw new UnauthorizedException("Vous ne pouvez annuler que vos propres réservations");
        }

        // 3. Vérifier que la réservation n'est pas déjà annulée
        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // ❌ RÈGLE MÉTIER CRITIQUE : Annulation possible jusqu'à 48h avant l'événement
        LocalDateTime limiteAnnulation = reservation.getEvenement()
                .getDateDebut()
                .minusHours(48);

        if (LocalDateTime.now().isAfter(limiteAnnulation)) {
            long heuresRestantes = java.time.temporal.ChronoUnit.HOURS.between(
                    LocalDateTime.now(),
                    reservation.getEvenement().getDateDebut()
            );
            throw new BusinessException(
                    String.format("Impossible d'annuler : vous devez annuler au moins 48h avant l'événement. Il ne reste que %d heure(s).",
                            heuresRestantes)
            );
        }

        // Annulation effective
        reservation.setStatut(ReservationStatus.ANNULEE);
        reservation.setCommentaire("Annulée par l'utilisateur le " + LocalDateTime.now());
        reservationRepository.save(reservation);

        System.out.println("✅ Réservation " + reservation.getCodeReservation() + " annulée avec succès");
    }

    // ==================== CONFIRMER UNE RÉSERVATION ====================

    /**
     * Confirmer une réservation EN_ATTENTE
     */
    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        if (reservation.getStatut() != ReservationStatus.EN_ATTENTE) {
            throw new BusinessException("Seule une réservation en attente peut être confirmée");
        }

        reservation.setStatut(ReservationStatus. CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    // ==================== RÉCUPÉRATION DE RÉSERVATIONS ====================

    /**
     * Obtenir toutes les réservations d'un utilisateur
     */
    public List<Reservation> findUserReservations(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur introuvable avec l'ID : " + userId
                ));
        return reservationRepository.findByUtilisateurId(userId);
    }

    /**
     * Obtenir une réservation par son code
     */
    public Reservation getReservationByCode(String code) {
        return reservationRepository.findByCodeReservation(code)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune réservation trouvée avec le code : " + code));
    }

    /**
     * Obtenir toutes les réservations d'un événement
     */
    public List<Reservation> findEventReservations(Long eventId) {
        return reservationRepository. findByEvenementId(eventId);
    }

    /**
     * Obtenir les réservations avec filtres
     */
    public List<Reservation> getReservationsWithFilters(Long userId, Long eventId, ReservationStatus statut) {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                . filter(r -> userId == null || r.getUtilisateur().getId(). equals(userId))
                .filter(r -> eventId == null || r.getEvenement().getId().equals(eventId))
                .filter(r -> statut == null || r.getStatut(). equals(statut))
                . collect(Collectors.toList());
    }

    // ==================== RÉCAPITULATIF ====================

    /**
     * Obtenir un récapitulatif détaillé d'une réservation
     */
    public Map<String, Object> getReservationSummary(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        Map<String, Object> summary = new HashMap<>();
        summary.put("code", reservation.getCodeReservation());
        summary.put("eventTitle", reservation.getEvenement(). getTitre());
        summary.put("eventDate", reservation.getEvenement().getDateDebut());
        summary.put("eventLocation", reservation.getEvenement(). getLieu());
        summary.put("eventCity", reservation.getEvenement(). getVille());
        summary. put("nombrePlaces", reservation.getNombrePlaces());
        summary.put("montantTotal", reservation.getMontantTotal());
        summary. put("statut", reservation.getStatut());
        summary.put("statutLabel", reservation.getStatut(). getLabel());
        summary.put("dateReservation", reservation.getDateReservation());
        summary. put("clientName", reservation.getUtilisateur().getNomComplet());
        summary.put("clientEmail", reservation.getUtilisateur().getEmail());
        summary.put("isAnnulable", reservation.isAnnulable());
        summary.put("heuresAvantLimite", reservation.getHeuresAvantLimiteAnnulation());

        return summary;
    }

    // ==================== STATISTIQUES ====================

    /**
     * Statistiques globales des réservations (pour admin)
     */
    public Map<String, Object> getReservationStatistics() {
        List<Reservation> allReservations = reservationRepository. findAll();

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
                .filter(r -> r.getStatut() == ReservationStatus. CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
        stats. put("totalRevenue", totalRevenue);

        return stats;
    }

    /**
     * Vérifier si un utilisateur a déjà réservé pour un événement
     */
    public boolean hasUserReservedEvent(Long userId, Long eventId) {
        return reservationRepository. findByUtilisateurIdAndEvenementId(userId, eventId)
                .filter(r -> r.getStatut() != ReservationStatus.ANNULEE)
                .isPresent();
    }

    // ==================== MÉTHODE PRIVÉE : GÉNÉRATION CODE ====================

    /**
     * Générer un code de réservation unique au format EVT-XXXXX
     * Exemple : EVT-12345, EVT-98765
     */
    private String generateUniqueReservationCode() {
        String code;
        int tentatives = 0;
        final int MAX_TENTATIVES = 100;

        do {
            // Générer un nombre aléatoire entre 0 et 99999
            Random random = new Random();
            int number = random.nextInt(100000);

            // Formater avec 5 chiffres : EVT-00123, EVT-45678, etc.
            code = String.format("EVT-%05d", number);

            tentatives++;

            // Sécurité : éviter boucle infinie
            if (tentatives >= MAX_TENTATIVES) {
                throw new BusinessException("Impossible de générer un code unique après " + MAX_TENTATIVES + " tentatives");
            }

        } while (reservationRepository.findByCodeReservation(code).isPresent());

        return code;
    }

    /**
     * Statistiques des réservations pour un événement spécifique
     *
     * @param eventId ID de l'événement
     * @return Map contenant les statistiques
     */
    public Map<String, Object> getReservationStatisticsByEvent(Long eventId) {
        // Vérifier que l'événement existe
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable"));

        List<Reservation> reservations = reservationRepository.findByEvenementId(eventId);

        Map<String, Object> stats = new HashMap<>();
        stats. put("eventId", eventId);
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
                .filter(r -> r.getStatut() == ReservationStatus. CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        stats.put("totalPlaces", totalPlaces);

        double totalRevenue = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .filter(r -> r.getMontantTotal() != null)
                . mapToDouble(Reservation::getMontantTotal)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }
}