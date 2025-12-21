package ma.projet.events.util;

import org.springframework.security. crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "password123";
        String hash = encoder.encode(password);

        System. out.println("=================================");
        System.out.println("Mot de passe :  " + password);
        System.out.println("Hash BCrypt  : " + hash);
        System.out.println("=================================");

        // Vérification
        boolean matches = encoder.matches(password, hash);
        System.out. println("Vérification : " + (matches ? "✅ OK" : "❌ ERREUR"));
    }
}