package ma.projet.events.repository;

import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trouver un utilisateur par son email (pour le login)
    Optional<User> findByEmail(String email);

    // Vérifier si un email existe déjà (pour l'inscription)
    boolean existsByEmail(String email);

    // Trouver tous les utilisateurs actifs d'un certain rôle
    List<User> findByRoleAndActifTrue(Role role);

    // Recherche par nom ou prénom (pour l'admin)
    List<User> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    // Compter combien on a d'utilisateurs d'un certain rôle
    Long countByRole(Role role);
}