package ma.projet.events.security;

import ma.projet.events.entity.User;
import ma.projet.events.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service personnalisé pour connecter Spring Security à notre base de données.
 * Il implémente UserDetailsService pour indiquer à Spring comment charger un utilisateur.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Méthode appelée automatiquement par Spring lors de la connexion.
     * Elle cherche l'utilisateur par son email (utilisé comme identifiant).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Recherche de l'utilisateur dans la base H2
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        // 2. Conversion du Rôle (Enum) en Autorité Spring Security (ex: ADMIN -> ROLE_ADMIN)
        // Spring Security attend généralement le préfixe "ROLE_"
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 3. Retourne un objet UserDetails standard que Spring Security comprend
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),          // L'identifiant
                user.getPassword(),       // Le mot de passe hashé
                user.isActif(),           // Vérifie si le compte est activé
                true, // Compte non expiré
                true, // Identifiants non expirés
                true, // Compte non verrouillé
                authorities               // La liste des droits (Rôles)
        );
    }
}