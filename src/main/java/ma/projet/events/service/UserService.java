package ma.projet.events.service;

import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    // On prépare l'outil pour crypter les mots de passe (sécurité)
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Inscription d'un utilisateur
    public User saveUser(User user) {
        // Règle métier 1 : L'email doit être unique
        if (user.getId() == null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Règle métier 2 : On crypte le mot de passe avant de sauvegarder
        // (Pour ne pas qu'il soit lisible en clair dans la base)
        if (user.getId() == null) { // Seulement à la création
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}