package ma.projet.events.security;

import ma. projet.events.entity.User;
import ma.projet.events. repository.UserRepository;
import org.springframework.security.core. GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security. core.userdetails.UserDetailsService;
import org.springframework. security.core.userdetails. UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Service pour charger les utilisateurs depuis la base de données
 * Implémente UserDetailsService de Spring Security
 *
 * Spring Security utilise ce service pour :
 * - Charger un utilisateur par son email (username)
 * - Vérifier le mot de passe
 * - Gérer les rôles et permissions
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Charge un utilisateur par son email (utilisé comme username)
     * Appelée automatiquement par Spring Security lors de l'authentification
     *
     * @param email Email de l'utilisateur (username)
     * @return UserDetails avec les informations de l'utilisateur
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Chercher l'utilisateur dans la base de données
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable avec l'email :  " + email
                ));

        // 2. Vérifier que le compte est actif
        if (!user.isActif()) {
            throw new UsernameNotFoundException(
                    "Le compte est désactivé. Contactez l'administrateur."
            );
        }

        // 3. Convertir notre User en UserDetails de Spring Security
        return org.springframework.security.core.userdetails.User. builder()
                .username(user.getEmail())
                .password(user.getPassword()) // Mot de passe déjà hashé avec BCrypt
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(! user.isActif()) // Verrouillé si inactif
                .credentialsExpired(false)
                .disabled(! user.isActif())
                .build();
    }

    /**
     * Convertit le rôle de l'utilisateur en autorités Spring Security
     * Format attendu par Spring Security : "ROLE_XXX"
     *
     * @param user L'utilisateur
     * @return Collection d'autorités
     */
    private Collection<?  extends GrantedAuthority> getAuthorities(User user) {
        // Spring Security exige le préfixe "ROLE_" pour les rôles
        String authority = "ROLE_" + user.getRole().name(); // ROLE_CLIENT, ROLE_ADMIN, etc.
        return Collections. singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * Méthode utilitaire pour récupérer l'utilisateur complet depuis l'email
     * Utile pour récupérer toutes les infos (pas juste UserDetails)
     *
     * @param email Email de l'utilisateur
     * @return L'entité User complète
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur introuvable avec l'email : " + email
                ));
    }
}