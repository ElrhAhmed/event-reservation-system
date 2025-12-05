package ma.projet.events.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codeReservation; // Format EVT-XXXXX

    @Min(1)
    private Integer nombrePlaces;

    private Double montantTotal;

    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    private ReservationStatus statut;

    private String commentaire;

    // Qui a réservé ?
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User utilisateur;

    // Quel événement ?
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event evenement;

    @PrePersist
    protected void onCreate() {
        dateReservation = LocalDateTime.now();
        if(statut == null) statut = ReservationStatus.EN_ATTENTE;
    }
}