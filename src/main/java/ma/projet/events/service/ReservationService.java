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

    // ==================== 1. CLIENT : RÉSERVER (Mise en attente) ====================

    @Transactional
    public Reservation reserverTicket(Long eventId, Long userId, int nombrePlacesDemande) {

        // --- A. Vérifications ---
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // --- B. Règles Métier ---
        if (nombrePlacesDemande < 1) throw new BusinessException("Le nombre de places doit être au moins 1");
        if (nombrePlacesDemande > 10) throw new BusinessException("Impossible de réserver plus de 10 places.");

        if (event.getStatut() == EventStatus.TERMINE) throw new BusinessException("Cet événement est terminé.");
        if (event.getStatut() == EventStatus.ANNULE) throw new BusinessException("Cet événement a été annulé.");
        if (event.getStatut() != EventStatus.PUBLIE) throw new BusinessException("Cet événement n'est pas encore disponible.");
        if (event.getDateDebut().isBefore(LocalDateTime.now())) throw new BusinessException("L'événement a déjà commencé.");

        // --- C. Disponibilité (Inclut En Attente + Confirmée) ---
        // Votre Repository gère cela correctement via la requête (statut != ANNULEE)
        Integer placesDejaReservees = reservationRepository.calculateTotalPlacesReserved(eventId);
        if (placesDejaReservees == null) placesDejaReservees = 0;

        int placesRestantes = event.getCapaciteMax() - placesDejaReservees;

        if (nombrePlacesDemande > placesRestantes) {
            throw new BusinessException(
                    String.format("Impossible de réserver %d place(s). Il ne reste que %d place(s) disponible(s) (y compris celles en attente de validation).",
                            nombrePlacesDemande, placesRestantes)
            );
        }

        // --- D. Vérification Doublon ---
        // On bloque si l'utilisateur a déjà une réservation active (En attente ou Confirmée)
        boolean aDejaUneReservation = reservationRepository
                .findByUtilisateurIdAndEvenementId(userId, eventId).stream()
                .anyMatch(r -> r.getStatut() != ReservationStatus.ANNULEE);

        if (aDejaUneReservation) {
            throw new ConflictException("Vous avez déjà une demande en cours pour cet événement.");
        }

        // --- E. Création (Statut EN_ATTENTE) ---
        Reservation reservation = new Reservation();
        reservation.setEvenement(event);
        reservation.setUtilisateur(user);
        reservation.setNombrePlaces(nombrePlacesDemande);
        reservation.setDateReservation(LocalDateTime.now());

        // IMPORTANT : Statut par défaut "En attente" pour validation organisateur
        reservation.setStatut(ReservationStatus.EN_ATTENTE);

        // Calcul montant
        double prix = (event.getPrixUnitaire() != null) ? event.getPrixUnitaire() : 0.0;
        reservation.setMontantTotal(prix * nombrePlacesDemande);

        // Génération Code
        reservation.setCodeReservation(generateUniqueReservationCode());

        Reservation saved = reservationRepository.save(reservation);
        System.out.println("⏳ Demande créée (En attente) : " + saved.getCodeReservation());

        return saved;
    }

    // ==================== 2. ORGANISATEUR : CONFIRMER ====================

    @Transactional
    public Reservation confirmReservation(Long reservationId, Long userIdAppelant) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        // SÉCURITÉ : On vérifie que c'est bien l'organisateur de l'événement qui confirme
        if (!reservation.getEvenement().getOrganisateur().getId().equals(userIdAppelant)) {
            throw new UnauthorizedException("Seul l'organisateur peut confirmer cette réservation.");
        }

        if (reservation.getStatut() == ReservationStatus.CONFIRMEE) {
            throw new BusinessException("Cette réservation est déjà confirmée.");
        }

        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Impossible de confirmer une réservation annulée.");
        }

        // Seules les réservations en attente peuvent être validées
        if (reservation.getStatut() != ReservationStatus.EN_ATTENTE) {
            throw new BusinessException("Seule une réservation en attente peut être confirmée.");
        }

        reservation.setStatut(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    // ==================== 3. ANNULER (Client) ou REFUSER (Organisateur) ====================

    @Transactional
    public void annulerReservation(Long reservationId, Long userIdAppelant) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        boolean isClient = reservation.getUtilisateur().getId().equals(userIdAppelant);
        boolean isOrganisateur = reservation.getEvenement().getOrganisateur().getId().equals(userIdAppelant);

        if (!isClient && !isOrganisateur) {
            throw new UnauthorizedException("Action non autorisée.");
        }

        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // Règle 48h : Uniquement pour le CLIENT et si la réservation est déjà CONFIRMÉE
        // Si elle est "En attente", le client peut annuler sans frais/délai.
        if (isClient && reservation.getStatut() == ReservationStatus.CONFIRMEE) {
            LocalDateTime limite = reservation.getEvenement().getDateDebut().minusHours(48);
            if (LocalDateTime.now().isAfter(limite)) {
                throw new BusinessException("Délai d'annulation dépassé (48h avant l'événement).");
            }
        }

        reservation.setStatut(ReservationStatus.ANNULEE);

        if (isOrganisateur) {
            reservation.setCommentaire("Refusée par l'organisateur.");
        } else {
            reservation.setCommentaire("Annulée par le client.");
        }

        reservationRepository.save(reservation);
        System.out.println("❌ Réservation annulée/refusée : " + reservation.getCodeReservation());
    }

    // ==================== 4. LECTURE & FILTRES (Rien ne manque) ====================

    public List<Reservation> findUserReservations(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User introuvable"));
        return reservationRepository.findByUtilisateurId(userId);
    }

    public List<Reservation> findEventReservations(Long eventId) {
        return reservationRepository.findByEvenementId(eventId);
    }

    public Reservation getReservationByCode(String code) {
        return reservationRepository.findByCodeReservation(code)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune réservation trouvée avec le code : " + code));
    }

    public List<Reservation> getReservationsWithFilters(Long userId, Long eventId, ReservationStatus statut) {
        return reservationRepository.findAll().stream()
                .filter(r -> userId == null || r.getUtilisateur().getId().equals(userId))
                .filter(r -> eventId == null || r.getEvenement().getId().equals(eventId))
                .filter(r -> statut == null || r.getStatut().equals(statut))
                .collect(Collectors.toList());
    }

    public boolean hasUserReservedEvent(Long userId, Long eventId) {
        return reservationRepository.findByUtilisateurIdAndEvenementId(userId, eventId)
                .stream()
                .anyMatch(r -> r.getStatut() != ReservationStatus.ANNULEE);
    }

    // ==================== 5. RÉSUMÉS & STATISTIQUES ====================

    public Map<String, Object> getReservationSummary(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable"));

        Map<String, Object> summary = new HashMap<>();
        summary.put("code", r.getCodeReservation());
        summary.put("eventTitle", r.getEvenement().getTitre());
        summary.put("eventDate", r.getEvenement().getDateDebut());
        summary.put("eventLocation", r.getEvenement().getLieu());
        summary.put("nombrePlaces", r.getNombrePlaces());
        summary.put("montantTotal", r.getMontantTotal());
        summary.put("statut", r.getStatut());
        summary.put("statutLabel", r.getStatut().toString());
        summary.put("clientName", r.getUtilisateur().getNomComplet());

        boolean isAnnulable = r.getStatut() != ReservationStatus.ANNULEE &&
                LocalDateTime.now().isBefore(r.getEvenement().getDateDebut().minusHours(48));
        summary.put("isAnnulable", isAnnulable);

        return summary;
    }

    public Map<String, Object> getReservationStatistics() {
        List<Reservation> all = reservationRepository.findAll();
        return calculateStats(all);
    }

    public Map<String, Object> getReservationStatisticsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable"));

        List<Reservation> reservations = reservationRepository.findByEvenementId(eventId);
        Map<String, Object> stats = calculateStats(reservations);

        stats.put("eventId", eventId);
        stats.put("eventTitle", event.getTitre());

        return stats;
    }

    // Méthode privée pour centraliser le calcul des stats
    private Map<String, Object> calculateStats(List<Reservation> list) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReservations", list.size());

        long confirmed = list.stream().filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE).count();
        stats.put("confirmedReservations", confirmed);
        stats.put("reservationsConfirmees", confirmed); // Alias

        long pending = list.stream().filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE).count();
        stats.put("pendingReservations", pending);
        stats.put("reservationsEnAttente", pending); // Alias

        long cancelled = list.stream().filter(r -> r.getStatut() == ReservationStatus.ANNULEE).count();
        stats.put("cancelledReservations", cancelled);
        stats.put("reservationsAnnulees", cancelled); // Alias

        double revenue = list.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();
        stats.put("totalRevenue", revenue);

        // Total places occupées (Confirmées + En attente) pour voir le remplissage réel
        int totalPlaces = list.stream()
                .filter(r -> r.getStatut() != ReservationStatus.ANNULEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        stats.put("totalPlacesOccupees", totalPlaces);

        return stats;
    }

    // ==================== 6. UTILITAIRE ====================

    private String generateUniqueReservationCode() {
        String code;
        int tentatives = 0;
        do {
            code = String.format("EVT-%05d", new Random().nextInt(100000));
            tentatives++;
            if (tentatives > 100) throw new BusinessException("Erreur génération code unique.");
        } while (reservationRepository.findByCodeReservation(code).isPresent());
        return code;
    }
}