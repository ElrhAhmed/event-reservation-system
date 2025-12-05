package ma.projet.events.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // "user" est un mot réservé SQL, on utilise "users"
@Data // Lombok génère getters, setters, toString...
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
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role; // ADMIN, ORGANIZER ou CLIENT

    private LocalDateTime dateInscription;

    private Boolean actif = true;

    private String telephone;

    // Cette méthode est appelée juste avant que l'utilisateur soit sauvé en base pour la 1ère fois
    @PrePersist
    protected void onCreate() {
        dateInscription = LocalDateTime.now();
    }
}