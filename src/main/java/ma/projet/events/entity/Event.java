package ma.projet.events.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.projet.events.exception.BadRequestException;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor


public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private EventCategory categorie;

    @NotNull
    private LocalDateTime dateDebut;

    @NotNull
    private LocalDateTime dateFin;

    @NotBlank
    private String lieu;

    @NotBlank
    private String ville;

    @Min(1)
    private Integer capaciteMax;

    @Min(0)
    private Double prixUnitaire;

    private String imageUrl; // Chemin vers l'image ou URL

    @Enumerated(EnumType.STRING)
    private EventStatus statut = EventStatus.BROUILLON;

    // Relation : Plusieurs événements peuvent appartenir à UN organisateur
    @ManyToOne
    @JoinColumn(name = "organisateur_id")
    private User organisateur;



    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }


    // AJOUTER ces champs :

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;



    // ❌ AJOUTER validation : dateFin doit être après dateDebut
// Dans la méthode ou avec un validateur personnalisé
    public void setDateFin(LocalDateTime dateFin) {
        if (dateFin != null && dateDebut != null && dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }
        this.dateFin = dateFin;
    }


}