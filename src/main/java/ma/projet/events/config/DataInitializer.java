package ma.projet.events.config;

import ma.projet.events.entity.Role;
import ma.projet.events.entity.User;
import ma.projet.events.repository.UserRepository;
import org.springframework.boot. CommandLineRunner;
import org. springframework.context.annotation.Bean;
import org.springframework.context. annotation.Configuration;
import org. springframework.security.crypto.password. PasswordEncoder;

import java. time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Nettoyer les utilisateurs existants
            userRepository.deleteAll();

            System.out.println("\n================================");
            System.out.println("🔧 CRÉATION DES UTILISATEURS DE TEST");
            System.out.println("================================\n");

            // 1. ADMIN
            createUser(userRepository, passwordEncoder,
                    "Admin", "System", "admin@festivent.com", "admin123", Role.ADMIN, "+212 6 00 00 00 00");

            // 2. ORGANISATEUR 1
            createUser(userRepository, passwordEncoder,
                    "Martin", "Sarah", "sarah.martin@festivent.com", "sarah123", Role.ORGANIZER, "+212 6 11 11 11 11");

            // 3. ORGANISATEUR 2
            createUser(userRepository, passwordEncoder,
                    "Alami", "Karim", "karim.alami@festivent.com", "karim123", Role.ORGANIZER, "+212 6 22 22 22 22");

            // 4. CLIENT 1
            createUser(userRepository, passwordEncoder,
                    "Benali", "Ahmed", "ahmed.benali@festivent.com", "ahmed123", Role. CLIENT, "+212 6 33 33 33 33");

            // 5. CLIENT 2
            createUser(userRepository, passwordEncoder,
                    "Mansouri", "Fatima", "fatima.mansouri@festivent.com", "fatima123", Role.CLIENT, "+212 6 44 44 44 44");

            System.out.println("\n================================");
            System.out.println("✅ " + userRepository.count() + " utilisateurs créés avec succès !");
            System. out.println("================================\n");
        };
    }

    private void createUser(UserRepository repo, PasswordEncoder encoder,
                            String nom, String prenom, String email, String password,
                            Role role, String telephone) {
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));  // ✅ HASH AUTOMATIQUE
        user. setRole(role);
        user.setActif(true);
        user.setDateInscription(LocalDateTime.now());
        user.setTelephone(telephone);

        repo.save(user);

        System.out.println("✅ " + role.getLabel() + " : " + email + " / " + password);
        System.out.println("   Hash généré : " + user.getPassword().substring(0, 20) + "...");
    }
}