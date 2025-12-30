package ma.projet.events.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_statut", columnList = "statut"),
        @Index(name = "idx_categorie", columnList = "categorie"),
        @Index(name = "idx_ville", columnList = "ville"),
        @Index(name = "idx_date_debut", columnList = "dateDebut")
})
@Getter  // ← Génère tous les getters
@Setter  // ← Génère tous les setters (SAUF ceux qu'on écrit manuellement)
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    @Column(nullable = false)
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "La catégorie est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory categorie;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false)
    private String lieu;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false)
    private String ville;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    @Column(nullable = false)
    private Integer capaciteMax;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le prix ne peut pas être négatif")
    @Column(nullable = false)
    private Double prixUnitaire;

    private String imageUrl;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus statut;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateModification;

    @ManyToOne
    @JoinColumn(name = "organisateur_id", nullable = false)
    private User organisateur;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // <--- ET CECI
    private List<Reservation> reservations = new ArrayList<>();

    // ==================== SETTER PERSONNALISÉ POUR dateFin ====================

    /*
     * Setter personnalisé pour dateFin avec validation
     * Remplace le setter généré par Lombok
     */
    /**
    public void setDateFin(LocalDateTime dateFin) {
        // Validation : dateFin doit être après dateDebut
        if (dateFin != null && dateDebut != null) {
            if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
                throw new IllegalArgumentException(
                        "La date de fin doit être après la date de début"
                );
            }
        }
        this.dateFin = dateFin;
    }
     */

    // ==================== HOOKS ====================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (dateCreation == null) {
            dateCreation = now;
        }
        if (dateModification == null) {
            dateModification = now;
        }
        if (statut == null) {
            statut = EventStatus.BROUILLON;
        }

        validateDates();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
        validateDates();
    }

    // ==================== VALIDATIONS PERSONNALISÉES ====================

    private void validateDates() {
        if (dateDebut != null && dateFin != null) {
            if (dateFin.isBefore(dateDebut) || dateFin.isEqual(dateDebut)) {
                throw new IllegalArgumentException(
                        "La date de fin doit être après la date de début"
                );
            }
        }
    }

    @AssertTrue(message = "Toutes les informations obligatoires doivent être renseignées pour publier")
    public boolean isReadyToPublish() {
        if (statut == EventStatus.BROUILLON) {
            return true;
        }

        return titre != null && !titre.isBlank() &&
                description != null && !description.isBlank() &&
                dateDebut != null &&
                dateFin != null &&
                lieu != null && !lieu.isBlank() &&
                ville != null && !ville.isBlank() &&
                capaciteMax != null && capaciteMax > 0 &&
                prixUnitaire != null && prixUnitaire >= 0 &&
                organisateur != null;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    public boolean isModifiable() {
        return statut != EventStatus.TERMINE;
    }

    public boolean isReservable() {
        return statut == EventStatus.PUBLIE &&
                dateDebut. isAfter(LocalDateTime.now());
    }

    public boolean isTermine() {
        return statut == EventStatus.TERMINE ||
                (dateFin != null && dateFin.isBefore(LocalDateTime. now()));
    }

    public long getJoursRestants() {
        if (dateDebut == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(),
                dateDebut
        );
    }

    public long getDureeEnHeures() {
        if (dateDebut == null || dateFin == null) return 0;
        return java.time.temporal.ChronoUnit.HOURS.between(dateDebut, dateFin);
    }
}