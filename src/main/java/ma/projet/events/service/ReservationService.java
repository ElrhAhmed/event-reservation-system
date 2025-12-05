package ma.projet.events.service;

import ma.projet.events.entity.*;
import ma.projet.events.repository.EventRepository;
import ma.projet.events.repository.ReservationRepository;
import ma.projet.events.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    // C'est LA méthode la plus importante du projet
    @Transactional
    public Reservation reserverTicket(Long eventId, Long userId, int nombrePlacesDemande) {

        // 1. Vérifier que l'événement existe
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Événement introuvable !"));

        // 2. Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable !"));

        // 3. COMPTAGE : Combien de places sont déjà prises ?
        List<Reservation> reservationsExistantes = reservationRepository.findByEvenementId(eventId);

        int placesDejaPrises = 0;
        for (Reservation r : reservationsExistantes) {
            placesDejaPrises += r.getNombrePlaces(); // On additionne toutes les réservations
        }

        // 4. VERDICT : Reste-t-il assez de place ?
        if (placesDejaPrises + nombrePlacesDemande > event.getCapaciteMax()) {
            // Calcul du nombre exact de places restantes
            int placesRestantes = event.getCapaciteMax() - placesDejaPrises;

            throw new RuntimeException("Désolé, impossible de réserver " + nombrePlacesDemande +
                    " places. Il ne reste que " + placesRestantes + " place(s) disponible(s) !");
        }

        // 5. CRÉATION DU BILLET
        Reservation resa = new Reservation();
        resa.setEvenement(event);
        resa.setUtilisateur(user);
        resa.setNombrePlaces(nombrePlacesDemande);
        resa.setDateReservation(LocalDateTime.now());
        resa.setStatut(ReservationStatus.CONFIRMEE);

        // Génération du prix total
        if (event.getPrixUnitaire() != null) {
            resa.setMontantTotal(event.getPrixUnitaire() * nombrePlacesDemande);
        } else {
            resa.setMontantTotal(0.0);
        }

        // Génération d'un code unique (ex: TICKET-XK9L)
        String codeUnique = "TICKET-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        resa.setCodeReservation(codeUnique);

        return reservationRepository.save(resa);
    }

    // Pour afficher "Mes réservations"
    public List<Reservation> findUserReservations(Long userId) {
        return reservationRepository.findByUtilisateurId(userId);
    }

    public void annulerReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        // Vérification de sécurité : Est-ce bien l'utilisateur qui annule sa propre réservation ?
        // (Sauf si on est ADMIN, mais on gérera ça plus tard)
        if (!reservation.getUtilisateur().getId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez pas annuler la réservation de quelqu'un d'autre");
        }

        // Règle des 48h
        LocalDateTime dateEvenement = reservation.getEvenement().getDateDebut();
        LocalDateTime maintenant = LocalDateTime.now();

        if (maintenant.plusHours(48).isAfter(dateEvenement)) {
            throw new RuntimeException("Impossible d'annuler : L'événement a lieu dans moins de 48h !");
        }

        reservation.setStatut(ReservationStatus.ANNULEE);
        reservationRepository.save(reservation);
    }
}