package ma.projet.events.service;

import ma.projet.events.entity.*;
import ma.projet.events. exception.*;
import ma.projet. events.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        ReservationRepository reservationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }


    @Transactional
    public Event createEvent(Event event, Long organisateurId) {
        User organisateur = userRepository.findById(organisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisateur introuvable"));

        // ✅ AJOUTER : Vérification du rôle
        if (! organisateur.isOrganizer() && !organisateur.isAdmin()) {
            throw new UnauthorizedException(
                    "Seuls les organisateurs et administrateurs peuvent créer des événements"
            );
        }

        event.setOrganisateur(organisateur);
        event.setStatut(EventStatus.BROUILLON);

        return eventRepository.save(event);
    }


    /**
     * Récupérer tous les événements
     */
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Récupérer un événement par ID
     */
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + id));
    }

    /**
     * Récupérer les événements d'un organisateur
     */
    public List<Event> getEventsByOrganisateur(Long organisateurId) {
        return eventRepository. findByOrganisateurId(organisateurId);
    }

    /**
     * Rechercher des événements par mot-clé
     */
    public List<Event> searchEvents(String keyword) {
        return eventRepository. search(keyword);
    }

    /**
     * Mettre à jour un événement
     */
    @Transactional
    public Event updateEvent(Long eventId, Event updatedEvent, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable"));

        // ✅ AJOUTER : Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // ✅ MODIFIER : Autoriser organisateur OU admin
        if (!event. getOrganisateur().getId(). equals(userId) && !user. isAdmin()) {
            throw new UnauthorizedException(
                    "Seul le créateur ou un administrateur peut modifier cet événement"
            );
        }

        // Vérifier que l'événement est modifiable
        if (!event.isModifiable()) {
            throw new BusinessException("Un événement terminé ne peut pas être modifié");
        }

        // Mettre à jour les champs (code existant)
        if (updatedEvent.getTitre() != null) {
            event.setTitre(updatedEvent.getTitre());
        }
        if (updatedEvent.getDescription() != null) {
            event. setDescription(updatedEvent.getDescription());
        }
        if (updatedEvent.getDateDebut() != null) {
            event.setDateDebut(updatedEvent.getDateDebut());
        }
        if (updatedEvent.getDateFin() != null) {
            event. setDateFin(updatedEvent.getDateFin());
        }
        if (updatedEvent.getLieu() != null) {
            event.setLieu(updatedEvent.getLieu());
        }
        if (updatedEvent.getVille() != null) {
            event.setVille(updatedEvent.getVille());
        }
        if (updatedEvent.getCapaciteMax() != null) {
            event.setCapaciteMax(updatedEvent.getCapaciteMax());
        }
        if (updatedEvent.getPrixUnitaire() != null) {
            event.setPrixUnitaire(updatedEvent.getPrixUnitaire());
        }
        if (updatedEvent.getImageUrl() != null) {
            event.setImageUrl(updatedEvent.getImageUrl());
        }
        if (updatedEvent.getCategorie() != null) {
            event.setCategorie(updatedEvent.getCategorie());
        }

        return eventRepository.save(event);
    }

    // ==================== MÉTHODE 1 : PUBLIER UN ÉVÉNEMENT ====================

    /**
     * Publier un événement (BROUILLON → PUBLIÉ)
     * Valide que toutes les informations obligatoires sont présentes
     *
     * @param eventId ID de l'événement
     * @param userId ID de l'utilisateur (doit être l'organisateur)
     * @return L'événement publié
     */
    @Transactional
    public Event publishEvent(Long eventId, Long userId) {
        // 1. Récupérer l'événement
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        // 2.  Vérifier les droits (seul l'organisateur peut publier)
        if (!event. getOrganisateur().getId(). equals(userId)) {
            throw new UnauthorizedException("Seul l'organisateur peut publier cet événement");
        }

        // 3.  Vérifier que l'événement est en BROUILLON
        if (event.getStatut() != EventStatus.BROUILLON) {
            throw new BusinessException(
                    "Seul un événement en brouillon peut être publié.  Statut actuel : " +
                            event.getStatut().getLabel()
            );
        }

        // 4. Valider que toutes les informations obligatoires sont présentes
        List<String> champsManquants = new ArrayList<>();

        if (event.getTitre() == null || event.getTitre().isBlank()) {
            champsManquants.add("Titre");
        }
        if (event.getDescription() == null || event.getDescription().isBlank()) {
            champsManquants.add("Description");
        }
        if (event.getDateDebut() == null) {
            champsManquants.add("Date de début");
        }
        if (event.getDateFin() == null) {
            champsManquants.add("Date de fin");
        }
        if (event.getLieu() == null || event.getLieu().isBlank()) {
            champsManquants.add("Lieu");
        }
        if (event.getVille() == null || event. getVille().isBlank()) {
            champsManquants.add("Ville");
        }
        if (event.getCapaciteMax() == null || event.getCapaciteMax() <= 0) {
            champsManquants.add("Capacité maximale");
        }
        if (event.getPrixUnitaire() == null || event.getPrixUnitaire() < 0) {
            champsManquants.add("Prix unitaire");
        }

        if (!champsManquants.isEmpty()) {
            throw new BusinessException(
                    "Impossible de publier : les champs suivants sont manquants ou invalides : " +
                            String.join(", ", champsManquants)
            );
        }

        // 5. Publier
        event.setStatut(EventStatus.PUBLIE);
        Event eventPublie = eventRepository. save(event);

        System.out.println("✅ Événement publié : " + event.getTitre() + " (ID: " + eventId + ")");

        return eventPublie;
    }

    // ==================== MÉTHODE 2 : ANNULER UN ÉVÉNEMENT ====================

    /**
     * Annuler un événement
     * Annule automatiquement toutes les réservations associées
     *
     * @param eventId ID de l'événement
     * @param userId ID de l'utilisateur (doit être l'organisateur)
     * @param raisonAnnulation Raison de l'annulation (pour informer les clients)
     * @return L'événement annulé
     */
    @Transactional
    public Event cancelEvent(Long eventId, Long userId, String raisonAnnulation) {
        // 1. Récupérer l'événement
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        // 2. Vérifier les droits
        if (!event.getOrganisateur().getId().equals(userId)) {
            throw new UnauthorizedException("Seul l'organisateur peut annuler cet événement");
        }

        // 3. Vérifier que l'événement n'est pas déjà annulé
        if (event.getStatut() == EventStatus. ANNULE) {
            throw new BusinessException("Cet événement est déjà annulé");
        }

        // 4. Vérifier que l'événement n'est pas terminé
        if (event.getStatut() == EventStatus. TERMINE) {
            throw new BusinessException("Impossible d'annuler un événement terminé");
        }

        // 5. Annuler toutes les réservations de cet événement
        List<Reservation> reservations = reservationRepository.findByEvenementId(eventId);
        for (Reservation resa : reservations) {
            if (resa.getStatut() != ReservationStatus.ANNULEE) {
                resa.setStatut(ReservationStatus.ANNULEE);
                resa.setCommentaire("Événement annulé : " +
                        (raisonAnnulation != null ? raisonAnnulation : "Aucune raison fournie"));
                reservationRepository. save(resa);
            }
        }

        // 6. Annuler l'événement
        event.setStatut(EventStatus.ANNULE);
        Event eventAnnule = eventRepository.save(event);

        System. out.println("✅ Événement annulé : " + event.getTitre() +
                " (" + reservations.size() + " réservation(s) annulée(s))");

        return eventAnnule;
    }

    // ==================== MÉTHODE 3 : SUPPRIMER UN ÉVÉNEMENT ====================

    /**
     * Supprimer un événement de manière sécurisée
     * Rejette si l'événement a des réservations
     *
     * @param eventId ID de l'événement
     * @param userId ID de l'utilisateur (doit être l'organisateur)
     */
    @Transactional
    public void deleteEventSafely(Long eventId, Long userId) {
        // 1.  Récupérer l'événement
        Event event = eventRepository. findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        // 2. Vérifier les droits
        if (!event.getOrganisateur().getId().equals(userId)) {
            throw new UnauthorizedException("Seul l'organisateur peut supprimer cet événement");
        }

        // 3. Vérifier qu'il n'y a aucune réservation
        Long nbReservations = reservationRepository. countByEvenementId(eventId);
        if (nbReservations > 0) {
            throw new BusinessException(
                    "Impossible de supprimer cet événement : " + nbReservations +
                            " réservation(s) existe(nt).  Annulez l'événement plutôt."
            );
        }

        // 4. Supprimer
        eventRepository.deleteById(eventId);

        System.out.println("✅ Événement supprimé : " + event.getTitre() + " (ID: " + eventId + ")");
    }

    // ==================== MÉTHODE 4 : CALCULER PLACES DISPONIBLES ====================

    /**
     * Calculer le nombre de places disponibles pour un événement
     *
     * @param eventId ID de l'événement
     * @return Nombre de places restantes
     */
    public int calculateAvailablePlaces(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Événement introuvable avec l'ID : " + eventId));

        Integer placesReservees = reservationRepository.calculateTotalPlacesReserved(eventId);
        if (placesReservees == null) {
            placesReservees = 0;
        }

        int placesDisponibles = event.getCapaciteMax() - placesReservees;

        return Math.max(0, placesDisponibles); // Ne jamais retourner un nombre négatif
    }

    // ==================== MÉTHODE 5 : ÉVÉNEMENTS POPULAIRES ====================

    /**
     * Obtenir les événements les plus populaires (les plus réservés)
     *
     * @param limit Nombre maximum d'événements à retourner
     * @return Liste des événements triés par popularité
     */
    public List<Event> getPopularEvents(int limit) {
        // Récupérer tous les événements publiés
        List<Event> eventsPublies = eventRepository.findByStatut(EventStatus.PUBLIE);

        // Trier par nombre de réservations (décroissant)
        return eventsPublies.stream()
                .sorted((e1, e2) -> {
                    Long count1 = reservationRepository.countByEvenementId(e1.getId());
                    Long count2 = reservationRepository.countByEvenementId(e2. getId());
                    return count2. compareTo(count1); // Ordre décroissant
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODE 6 : STATISTIQUES ORGANISATEUR ====================

    /**
     * Obtenir les statistiques d'un organisateur
     *
     * @param organizerId ID de l'organisateur
     * @return Map contenant les statistiques
     */
    public Map<String, Object> getOrganizerStatistics(Long organizerId) {
        List<Event> events = eventRepository. findByOrganisateurId(organizerId);

        Map<String, Object> stats = new HashMap<>();

        // Nombre total d'événements
        stats.put("totalEvents", events. size());

        // Nombre d'événements par statut
        long publishedCount = events.stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .count();
        stats. put("publishedEvents", publishedCount);

        long draftCount = events.stream()
                .filter(e -> e. getStatut() == EventStatus. BROUILLON)
                . count();
        stats.put("draftEvents", draftCount);

        long cancelledCount = events.stream()
                .filter(e -> e.getStatut() == EventStatus. ANNULE)
                .count();
        stats.put("cancelledEvents", cancelledCount);

        long finishedCount = events.stream()
                .filter(e -> e.getStatut() == EventStatus.TERMINE)
                .count();
        stats.put("finishedEvents", finishedCount);

        // Calculer les revenus totaux (réservations confirmées uniquement)
        double totalRevenue = events.stream()
                .flatMap(e -> reservationRepository.findByEvenementId(e. getId()).stream())
                .filter(r -> r.getStatut() == ReservationStatus. CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
        stats.put("totalRevenue", totalRevenue);

        // Nombre total de réservations
        long totalReservations = events.stream()
                .mapToLong(e -> reservationRepository.countByEvenementId(e.getId()))
                .sum();
        stats.put("totalReservations", totalReservations);

        return stats;
    }

    // ==================== MÉTHODE 7 : JOB AUTOMATIQUE - MARQUER ÉVÉNEMENTS TERMINÉS ====================

    /**
     * Job automatique qui s'exécute tous les jours à 1h du matin
     * Marque les événements comme TERMINÉ si leur date de fin est passée
     */
    @Scheduled(cron = "0 0 1 * * *") // Tous les jours à 1h du matin
    @Transactional
    public void checkAndMarkFinishedEvents() {
        List<Event> publishedEvents = eventRepository.findByStatut(EventStatus.PUBLIE);

        LocalDateTime now = LocalDateTime.now();
        int markedAsFinished = 0;

        for (Event event : publishedEvents) {
            if (event.getDateFin(). isBefore(now)) {
                event.setStatut(EventStatus. TERMINE);
                eventRepository.save(event);
                markedAsFinished++;
            }
        }

        if (markedAsFinished > 0) {
            System.out.println("✅ Job terminé : " + markedAsFinished + " événement(s) marqué(s) comme TERMINÉ");
        }
    }

    // ==================== MÉTHODE 8 : RECHERCHE AVEC FILTRES ====================

    /**
     * Recherche d'événements avec filtres multiples
     * Tous les paramètres sont optionnels (null = pas de filtre)
     *
     * @param categorie Catégorie (optionnel)
     * @param ville Ville (optionnel)
     * @param dateMin Date minimum (optionnel)
     * @param dateMax Date maximum (optionnel)
     * @param prixMin Prix minimum (optionnel)
     * @param prixMax Prix maximum (optionnel)
     * @return Liste des événements filtrés
     */
    public List<Event> searchWithFilters(
            EventCategory categorie,
            String ville,
            LocalDateTime dateMin,
            LocalDateTime dateMax,
            String lieu,
            Double prixMin,
            Double prixMax
    ) {
        List<Event> events = eventRepository. findByStatut(EventStatus.PUBLIE);

        return events.stream()
                .filter(e -> categorie == null || e.getCategorie().equals(categorie))
                .filter(e -> ville == null || e.getVille().equalsIgnoreCase(ville))
                .filter(e -> dateMin == null || e.getDateDebut().isAfter(dateMin))
                .filter(e -> dateMax == null || e. getDateDebut().isBefore(dateMax))
                .filter(e -> lieu == null || e.getLieu(). toLowerCase().contains(lieu.toLowerCase())) // ✅ AJOUTER
                .filter(e -> prixMin == null || e.getPrixUnitaire() >= prixMin)
                .filter(e -> prixMax == null || e.getPrixUnitaire() <= prixMax)
                .collect(Collectors.toList());
    }
}