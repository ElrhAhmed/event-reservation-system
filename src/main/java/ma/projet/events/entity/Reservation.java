package ma.projet.events.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.security.SecureRandom;
import java.time.LocalDateTime;



@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_code_reservation", columnList = "codeReservation", unique = true),
        @Index(name = "idx_res_statut", columnList = "statut"),
        @Index(name = "idx_date_reservation", columnList = "dateReservation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false, length = 20)
    private String codeReservation;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Il faut réserver au moins 1 place")
    @Max(value = 10, message = "Impossible de réserver plus de 10 places")
    @Column(nullable = false)
    private Integer nombrePlaces;

    @NotNull(message = "Le montant total est obligatoire")
    @Min(value = 0, message = "Le montant total ne peut pas être négatif")
    @Column(nullable = false)
    private Double montantTotal;

    @NotNull(message = "La date de réservation est obligatoire")
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateReservation;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus statut;

    @Column(length = 500)
    private String commentaire;

    // ==================== RELATIONS ====================

    // Une réservation appartient à un utilisateur
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User utilisateur;

    // Une réservation concerne un événement
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event evenement;

    // ==================== HOOKS ====================

    /**
     * Appelée automatiquement avant la première sauvegarde en base
     */

    @PrePersist
    protected void onCreate() {
        if (dateReservation == null) {
            dateReservation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = ReservationStatus.EN_ATTENTE;
        }
        // Générer un code de réservation si absent (format: EVT-XXXXX)
        if (codeReservation == null || codeReservation.isBlank()) {
            codeReservation = generateCodeReservation();
        }
        // Calcul automatique du montant total
        recalcMontantTotalOrThrow();
    }

    @PreUpdate
    protected void onUpdate() {
        // Recalcul si le nombre de places ou le prix de l'événement change
        recalcMontantTotalOrThrow();
    }

    private void recalcMontantTotalOrThrow() {
        Double prix = (evenement != null) ? evenement.getPrixUnitaire() : null;
        if (nombrePlaces == null || nombrePlaces < 1) {
            throw new IllegalStateException("Le nombre de places doit être strictement positif pour calculer le montant total");
        }
        if (prix == null) {
            throw new IllegalStateException("Le prix unitaire de l'événement est requis pour calculer le montant total");
        }
        this.montantTotal = prix * nombrePlaces;
    }

    private String generateCodeReservation() {
        // Génère 5 caractères alphanumériques en majuscule
        final String prefix = "EVT-";
        final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return prefix + sb;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifier si la réservation peut être annulée
     * Règle : Annulation possible jusqu'à 48h avant l'événement
     */
    public boolean isAnnulable() {
        if (statut == ReservationStatus. ANNULEE) {
            return false; // Déjà annulée
        }

        if (evenement == null || evenement.getDateDebut() == null) {
            return false;
        }

        // Calculer la limite d'annulation (48h avant)
        LocalDateTime limiteAnnulation = evenement. getDateDebut().minusHours(48);

        // On peut annuler si on est avant la limite
        return LocalDateTime.now().isBefore(limiteAnnulation);
    }

    /**
     * Obtenir le nombre d'heures restantes avant la limite d'annulation
     */
    public long getHeuresAvantLimiteAnnulation() {
        if (evenement == null || evenement.getDateDebut() == null) {
            return 0;
        }

        LocalDateTime limiteAnnulation = evenement.getDateDebut().minusHours(48);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(limiteAnnulation)) {
            return 0; // Dépassé
        }

        return java.time.temporal.ChronoUnit.HOURS.between(now, limiteAnnulation);
    }

    /**
     * Vérifier si la réservation est confirmée
     */
    public boolean isConfirmee() {
        return statut == ReservationStatus.CONFIRMEE;
    }

    /**
     * Vérifier si la réservation est annulée
     */
    public boolean isAnnulee() {
        return statut == ReservationStatus.ANNULEE;
    }

    /**
     * Vérifier si la réservation est en attente
     */
    public boolean isEnAttente() {
        return statut == ReservationStatus.EN_ATTENTE;
    }

    /**
     * Calculer le prix unitaire (montant / nombre de places)
     */
    public Double getPrixUnitaire() {
        if (nombrePlaces == null || nombrePlaces == 0 || montantTotal == null) {
            return 0.0;
        }
        return montantTotal / nombrePlaces;
    }

    /**
     * Obtenir un résumé textuel de la réservation
     */
    public String getResume() {
        return String.format(
                "Réservation %s - %d place(s) pour '%s' le %s - %s",
                codeReservation,
                nombrePlaces,
                evenement != null ? evenement.getTitre() : "N/A",
                evenement != null && evenement.getDateDebut() != null
                        ? evenement.getDateDebut().toLocalDate()
                        : "N/A",
                statut != null ? statut.getLabel() : "N/A"
        );
    }
}