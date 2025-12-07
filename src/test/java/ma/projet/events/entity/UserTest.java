package ma.projet.events.entity;

import org.junit.jupiter.api.BeforeEach;
import org. junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter. api.Assertions.*;

@DisplayName("Tests de l'entité User")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user. setId(1L);
        user.setNom("Dupont");
        user.setPrenom("Jean");
        user. setEmail("jean. dupont@example.com");
        user.setPassword("password123456");
        user.setRole(Role.CLIENT);
        user. setActif(true);
        user. setTelephone("0612345678");
        user.setDateInscription(LocalDateTime.now());
    }

    // ==================== TEST 1 : Méthode getNomComplet ====================

    @Test
    @DisplayName("Doit retourner le nom complet (prénom + nom)")
    void testGetNomComplet() {
        // ACT
        String nomComplet = user.getNomComplet();

        // ASSERT
        assertEquals("Jean Dupont", nomComplet);
    }

    @Test
    @DisplayName("Le nom complet doit contenir prénom et nom")
    void testGetNomComplet_Contenu() {
        // ACT
        String nomComplet = user. getNomComplet();

        // ASSERT
        assertTrue(nomComplet.contains("Jean"));
        assertTrue(nomComplet.contains("Dupont"));
    }

    // ==================== TEST 2 : Méthode isActif ====================

    @Test
    @DisplayName("Doit identifier un utilisateur actif")
    void testIsActif_Actif() {
        // ARRANGE
        user.setActif(true);

        // ACT & ASSERT
        assertTrue(user. isActif());
    }

    @Test
    @DisplayName("Doit identifier un utilisateur inactif")
    void testIsActif_Inactif() {
        // ARRANGE
        user.setActif(false);

        // ACT & ASSERT
        assertFalse(user.isActif());
    }

    @Test
    @DisplayName("Doit gérer le cas où actif est null")
    void testIsActif_Null() {
        // ARRANGE
        user.setActif(null);

        // ACT & ASSERT
        assertFalse(user.isActif());
    }

    // ==================== TEST 3 : Méthode isAdmin ====================

    @Test
    @DisplayName("Doit identifier un administrateur")
    void testIsAdmin_Admin() {
        // ARRANGE
        user.setRole(Role.ADMIN);

        // ACT & ASSERT
        assertTrue(user.isAdmin());
    }

    @Test
    @DisplayName("Un CLIENT ne doit pas être admin")
    void testIsAdmin_Client() {
        // ARRANGE
        user.setRole(Role. CLIENT);

        // ACT & ASSERT
        assertFalse(user.isAdmin());
    }

    @Test
    @DisplayName("Un ORGANIZER ne doit pas être admin")
    void testIsAdmin_Organizer() {
        // ARRANGE
        user.setRole(Role. ORGANIZER);

        // ACT & ASSERT
        assertFalse(user.isAdmin());
    }

    // ==================== TEST 4 : Méthode isOrganizer ====================

    @Test
    @DisplayName("Un ORGANIZER doit être identifié comme organisateur")
    void testIsOrganizer_Organizer() {
        // ARRANGE
        user.setRole(Role.ORGANIZER);

        // ACT & ASSERT
        assertTrue(user.isOrganizer());
    }

    @Test
    @DisplayName("Un ADMIN doit aussi être considéré comme organisateur")
    void testIsOrganizer_Admin() {
        // ARRANGE
        user.setRole(Role. ADMIN);

        // ACT & ASSERT
        assertTrue(user.isOrganizer(), "Un ADMIN a les droits d'organisateur");
    }

    @Test
    @DisplayName("Un CLIENT ne doit PAS être organisateur")
    void testIsOrganizer_Client() {
        // ARRANGE
        user.setRole(Role.CLIENT);

        // ACT & ASSERT
        assertFalse(user.isOrganizer());
    }

    // ==================== TEST 5 : Validation Email ====================

    @Test
    @DisplayName("L'email doit être valide")
    void testEmailValide() {
        // ACT
        String email = user.getEmail();

        // ASSERT
        assertNotNull(email);
        assertTrue(email. contains("@"), "L'email doit contenir @");
        assertTrue(email. contains(". "), "L'email doit contenir un point");
    }

    // ==================== TEST 6 : Validation Mot de Passe ====================

    @Test
    @DisplayName("Le mot de passe doit avoir au moins 8 caractères")
    void testPasswordLongueur() {
        // ACT
        String password = user. getPassword();

        // ASSERT
        assertNotNull(password);
        assertTrue(password.length() >= 8, "Le mot de passe doit avoir au moins 8 caractères");
    }

    // ==================== TEST 7 : Champs Obligatoires ====================

    @Test
    @DisplayName("Les champs obligatoires doivent être présents")
    void testChampsObligatoires() {
        // ASSERT
        assertNotNull(user.getNom());
        assertNotNull(user.getPrenom());
        assertNotNull(user.getEmail());
        assertNotNull(user. getPassword());
        assertNotNull(user.getRole());
    }

    // ==================== TEST 8 : Champ Optionnel ====================

    @Test
    @DisplayName("Le téléphone est optionnel")
    void testTelephoneOptionnel() {
        // ARRANGE
        User userSansTel = new User();
        userSansTel.setNom("Test");
        userSansTel.setPrenom("User");
        userSansTel.setEmail("test@example.com");
        userSansTel. setPassword("password123");
        userSansTel.setRole(Role.CLIENT);
        userSansTel.setTelephone(null); // Optionnel

        // ACT & ASSERT
        assertNull(userSansTel.getTelephone());
    }

    // ==================== TEST 9 : Enum Role ====================

    @Test
    @DisplayName("Le rôle doit avoir un label")
    void testRoleLabel() {
        // ASSERT
        assertNotNull(user.getRole().getLabel());
        assertNotNull(user.getRole().getIcon());
    }

    @Test
    @DisplayName("Tous les rôles doivent être testés")
    void testTousLesRoles() {
        // CLIENT
        user.setRole(Role.CLIENT);
        assertEquals("Client", user.getRole().getLabel());

        // ORGANIZER
        user.setRole(Role.ORGANIZER);
        assertEquals("Organisateur", user.getRole().getLabel());

        // ADMIN
        user.setRole(Role. ADMIN);
        assertEquals("Administrateur", user.getRole(). getLabel());
    }

    // ==================== TEST 10 : Date Inscription ====================

    @Test
    @DisplayName("La date d'inscription doit être définie")
    void testDateInscription() {
        // ACT
        LocalDateTime dateInscription = user.getDateInscription();

        // ASSERT
        assertNotNull(dateInscription);
    }
}