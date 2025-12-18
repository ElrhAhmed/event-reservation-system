package ma. projet.events.entity;

import jakarta.persistence.*;
import jakarta. validation.constraints. Email;
import jakarta.validation. constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // "user" est un mot réservé SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN, ORGANIZER ou CLIENT

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateInscription;

    @Column(nullable = false)
    private Boolean actif = true;

    private String telephone; // Optionnel

    // ==================== RELATIONS ====================

    // Un utilisateur peut créer plusieurs événements (s'il est ORGANIZER)
    @OneToMany(mappedBy = "organisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> evenementsOrganises = new ArrayList<>();

    // Un utilisateur peut effectuer plusieurs réservations
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    // ==================== HOOKS ====================

    /**
     * Appelée automatiquement avant la première sauvegarde en base
     */
    @PrePersist
    protected void onCreate() {
        if (dateInscription == null) {
            dateInscription = LocalDateTime. now();
        }
        if (actif == null) {
            actif = true;
        }
        if (role == null) {
            role = Role.CLIENT; // Par défaut
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Obtenir le nom complet de l'utilisateur
     */
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /**
     * Vérifier si l'utilisateur est actif
     */
    public boolean isActif() {
        return actif != null && actif;
    }

    /**
     * Vérifier si l'utilisateur est admin
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Vérifier si l'utilisateur est organisateur
     */
    public boolean isOrganizer() {
        return role == Role.ORGANIZER || role == Role.ADMIN;
    }
}
