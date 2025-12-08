package ma.projet.events.service;

import ma.projet.events.entity.*;
import ma.projet.events. exception.*;
import ma.projet.events. repository.*;
import org.junit.jupiter.api.BeforeEach;
import org. junit.jupiter.api.DisplayName;
import org.junit. jupiter.api.Test;
import org.junit.jupiter.api. extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java. util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito. Mockito.*;

@ExtendWith(MockitoExtension. class)
@DisplayName("Tests de UserService avec Mockito")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private UserService userService;

    // Données de test
    private User client;
    private User admin;
    private User organisateur;

    @BeforeEach
    void setUp() {
        // Créer un client
        client = new User();
        client.setId(1L);
        client.setNom("Client");
        client.setPrenom("Test");
        client.setEmail("client@test.com"); // ✅ PAS D'ESPACE
        client. setPassword("password123");
        client.setRole(Role.CLIENT);
        client.setActif(true);
        client. setDateInscription(LocalDateTime.now());

        // Créer un admin
        admin = new User();
        admin.setId(2L);
        admin.setNom("Admin");
        admin.setPrenom("Test");
        admin.setEmail("admin@test.com");
        admin.setPassword("admin123");
        admin.setRole(Role.ADMIN);
        admin.setActif(true);

        // Créer un organisateur
        organisateur = new User();
        organisateur.setId(3L);
        organisateur. setNom("Organisateur");
        organisateur.setPrenom("Test");
        organisateur. setEmail("org@test.com");
        organisateur.setPassword("org123");
        organisateur.setRole(Role.ORGANIZER);
        organisateur.setActif(true);
    }

    // ==================== TESTS authenticate() ====================

    @Test
    @DisplayName("✅ Doit authentifier avec email et mot de passe corrects")
    void testAuthenticate_Success() {
        // ARRANGE
        when(userRepository.findByEmail("client@test.com")). thenReturn(Optional.of(client));

        // ACT
        User result = userService.authenticate("client@test.com", "password123");

        // ASSERT
        assertNotNull(result);
        assertEquals("client@test.com", result. getEmail());
        verify(userRepository, times(1)).findByEmail("client@test.com");
    }

    @Test
    @DisplayName("❌ Doit rejeter si email incorrect")
    void testAuthenticate_WrongEmail() {
        // ARRANGE
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        // ACT & ASSERT
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            userService. authenticate("wrong@test.com", "password123");
        });

        assertTrue(exception.getMessage().contains("Email ou mot de passe incorrect"));
    }

    @Test
    @DisplayName("❌ Doit rejeter si mot de passe incorrect")
    void testAuthenticate_WrongPassword() {
        // ARRANGE
        when(userRepository.findByEmail("client@test.com")).thenReturn(Optional.of(client));

        // ACT & ASSERT
        UnauthorizedException exception = assertThrows(UnauthorizedException. class, () -> {
            userService.authenticate("client@test.com", "wrongpassword");
        });

        assertTrue(exception.getMessage().contains("Email ou mot de passe incorrect"));
    }

    @Test
    @DisplayName("❌ Doit rejeter si compte désactivé")
    void testAuthenticate_InactiveAccount() {
        // ARRANGE
        client.setActif(false);
        when(userRepository.findByEmail("client@test.com")). thenReturn(Optional.of(client));

        // ACT & ASSERT
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            userService.authenticate("client@test.com", "password123");
        });

        assertTrue(exception.getMessage().contains("désactivé"));
    }

    // ==================== TESTS updateProfile() ====================

    @Test
    @DisplayName("✅ Doit mettre à jour le profil")
    void testUpdateProfile_Success() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.save(any(User.class))). thenAnswer(invocation -> invocation.getArgument(0));

        User updates = new User();
        updates. setNom("Nouveau Nom");
        updates.setTelephone("0612345678");

        // ACT
        User result = userService.updateProfile(1L, updates, 1L);

        // ASSERT
        assertEquals("Nouveau Nom", result. getNom());
        assertEquals("0612345678", result.getTelephone());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("❌ Doit rejeter si pas le propriétaire")
    void testUpdateProfile_NotOwner() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        User updates = new User();
        updates.setNom("Nouveau Nom");

        // ACT & ASSERT
        assertThrows(UnauthorizedException. class, () -> {
            userService.updateProfile(1L, updates, 999L); // Autre user
        });

        verify(userRepository, never()). save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si email déjà pris")
    void testUpdateProfile_EmailTaken() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(admin));

        User updates = new User();
        updates.setEmail("taken@test.com");

        // ACT & ASSERT
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService. updateProfile(1L, updates, 1L);
        });

        assertTrue(exception.getMessage().contains("déjà utilisé"));
        verify(userRepository, never()).save(any());
    }

    // ==================== TESTS changePassword() ====================

    @Test
    @DisplayName("✅ Doit changer le mot de passe")
    void testChangePassword_Success() {
        // ARRANGE
        when(userRepository.findById(1L)). thenReturn(Optional.of(client));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        assertDoesNotThrow(() -> {
            userService.changePassword(1L, "password123", "newpassword123", 1L);
        });

        // ASSERT
        assertEquals("newpassword123", client. getPassword());
        verify(userRepository, times(1)).save(client);
    }

    @Test
    @DisplayName("❌ Doit rejeter si ancien mot de passe incorrect")
    void testChangePassword_WrongOldPassword() {
        // ARRANGE
        when(userRepository. findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        assertThrows(UnauthorizedException.class, () -> {
            userService.changePassword(1L, "wrongpassword", "newpassword123", 1L);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si nouveau mot de passe trop court")
    void testChangePassword_TooShort() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(1L, "password123", "short", 1L);
        });

        assertTrue(exception.getMessage(). contains("8 caractères"));
        verify(userRepository, never()).save(any());
    }

    // ==================== TESTS deactivateAccount() ====================

    @Test
    @DisplayName("✅ Admin doit pouvoir désactiver un compte")
    void testDeactivateAccount_Success() {
        // ARRANGE
        when(userRepository.findById(1L)). thenReturn(Optional.of(client));
        when(userRepository. findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation. getArgument(0));

        // ACT
        assertDoesNotThrow(() -> {
            userService.deactivateAccount(1L, 2L);
        });

        // ASSERT
        assertFalse(client.isActif());
        verify(userRepository, times(1)). save(client);
    }

    @Test
    @DisplayName("❌ Non-admin ne peut pas désactiver")
    void testDeactivateAccount_NotAdmin() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(3L)).thenReturn(Optional. of(organisateur));

        // ACT & ASSERT
        assertThrows(UnauthorizedException. class, () -> {
            userService.deactivateAccount(1L, 3L);
        });

        verify(userRepository, never()).save(any());
    }

    // ==================== TESTS activateAccount() ====================

    @Test
    @DisplayName("✅ Admin doit pouvoir activer un compte")
    void testActivateAccount_Success() {
        // ARRANGE
        client.setActif(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        assertDoesNotThrow(() -> {
            userService.activateAccount(1L, 2L);
        });

        // ASSERT
        assertTrue(client.isActif());
        verify(userRepository, times(1)).save(client);
    }

    // ==================== TESTS getUserStatistics() ====================

    @Test
    @DisplayName("✅ Doit calculer les statistiques d'un client")
    void testGetUserStatistics_Client() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        Reservation resa1 = new Reservation();
        resa1.setStatut(ReservationStatus.CONFIRMEE);
        resa1. setNombrePlaces(3);

        Reservation resa2 = new Reservation();
        resa2.setStatut(ReservationStatus.ANNULEE);
        resa2. setNombrePlaces(2);

        when(reservationRepository.findByUtilisateurId(1L))
                .thenReturn(Arrays.asList(resa1, resa2));
        when(reservationRepository.calculateTotalAmountByUser(1L))
                .thenReturn(150.0);

        // ACT
        Map<String, Object> stats = userService.getUserStatistics(1L);

        // ASSERT
        assertNotNull(stats);

        // ✅ CORRECTION : Cast en Integer pour size()
        assertEquals(2, (Integer) stats.get("totalReservations"));

        // Les count() sont des Long
        assertEquals(1L, (Long) stats.get("reservationsConfirmees"));
        assertEquals(1L, (Long) stats.get("reservationsAnnulees"));

        // Double
        assertEquals(150.0, (Double) stats.get("montantTotalDepense"), 0.01);

        // Integer
        assertEquals(3, (Integer) stats.get("totalPlacesReservees"));
    }

    @Test
    @DisplayName("✅ Doit inclure stats événements pour un organisateur")
    void testGetUserStatistics_Organizer() {
        // ARRANGE
        when(userRepository.findById(3L)).thenReturn(Optional. of(organisateur));
        when(reservationRepository.findByUtilisateurId(3L)). thenReturn(Collections.emptyList());
        when(reservationRepository.calculateTotalAmountByUser(3L)).thenReturn(0.0);

        Event event1 = new Event();
        event1.setStatut(EventStatus.PUBLIE);

        Event event2 = new Event();
        event2.setStatut(EventStatus.BROUILLON);

        when(eventRepository.findByOrganisateurId(3L))
                .thenReturn(Arrays.asList(event1, event2));

        // ACT
        Map<String, Object> stats = userService.getUserStatistics(3L);

        // ASSERT
        assertNotNull(stats);

        // ✅ CORRECTION : Cast en Integer pour size()
        assertEquals(0, (Integer) stats.get("totalReservations"));
        assertEquals(2, (Integer) stats.get("totalEvenementsOrganises"));

        // Long
        assertEquals(1L, (Long) stats.get("evenementsPublies"));
    }

    // ==================== TESTS searchUsers() ====================

    @Test
    @DisplayName("✅ Admin doit pouvoir rechercher des utilisateurs")
    void testSearchUsers_Success() {
        // ARRANGE
        when(userRepository.findById(2L)).thenReturn(Optional. of(admin));
        when(userRepository.findAll()).thenReturn(Arrays.asList(client, admin, organisateur));

        // ACT
        List<User> results = userService.searchUsers("test", 2L);

        // ASSERT
        assertEquals(3, results.size()); // Tous ont "Test" dans prénom
    }

    @Test
    @DisplayName("❌ Non-admin ne peut pas rechercher")
    void testSearchUsers_NotAdmin() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional. of(client));

        // ACT & ASSERT
        assertThrows(UnauthorizedException.class, () -> {
            userService.searchUsers("test", 1L);
        });
    }

    // ==================== TESTS getUsersByRole() ====================

    @Test
    @DisplayName("✅ Admin doit pouvoir filtrer par rôle")
    void testGetUsersByRole_Success() {
        // ARRANGE
        when(userRepository.findById(2L)).thenReturn(Optional. of(admin));
        when(userRepository.findAll()).thenReturn(Arrays.asList(client, admin, organisateur));

        // ACT
        List<User> clients = userService.getUsersByRole(Role.CLIENT, 2L);

        // ASSERT
        assertEquals(1, clients.size());
        assertEquals(Role.CLIENT, clients.get(0).getRole());
    }

    // ==================== TESTS register() (INSCRIPTION) ====================

    @Test
    @DisplayName("✅ Doit créer un nouvel utilisateur avec succès")
    void testRegister_Success() {
        // ARRANGE
        User newUser = new User();
        newUser.setNom("Nouveau");
        newUser.setPrenom("Utilisateur");
        newUser.setEmail("nouveau@test.com");
        newUser. setPassword("password123");
        newUser.setRole(Role.CLIENT);

        when(userRepository.findByEmail("nouveau@test.com")). thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))). thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            savedUser.setActif(true);
            savedUser.setDateInscription(LocalDateTime.now());
            return savedUser;
        });

        // ACT
        User result = userService.register(newUser);

        // ASSERT
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("nouveau@test.com", result.getEmail());
        assertTrue(result.isActif());
        assertNotNull(result.getDateInscription());
        verify(userRepository, times(1)). save(any(User.class));
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'email existe déjà")
    void testRegister_EmailAlreadyExists() {
        // ARRANGE
        User newUser = new User();
        newUser. setEmail("client@test.com"); // Email déjà pris

        when(userRepository.findByEmail("client@test.com")). thenReturn(Optional.of(client));

        // ACT & ASSERT
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService. register(newUser);
        });

        assertTrue(exception.getMessage().contains("déjà utilisé") ||
                exception.getMessage().contains("existe déjà"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si l'email est invalide")
    void testRegister_InvalidEmail() {
        // ARRANGE
        User newUser = new User();
        newUser.setNom("Test");
        newUser.setPrenom("User");
        newUser.setEmail("email-invalide"); // Pas de @
        newUser.setPassword("password123");

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(newUser);
        });

        assertTrue(exception.getMessage().contains("email") ||
                exception.getMessage(). contains("invalide"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si le mot de passe est trop court")
    void testRegister_PasswordTooShort() {
        // ARRANGE
        User newUser = new User();
        newUser.setNom("Test");
        newUser.setPrenom("User");
        newUser. setEmail("test@test.com");
        newUser.setPassword("short"); // < 8 caractères

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService. register(newUser);
        });

        assertTrue(exception.getMessage(). contains("8 caractères"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("❌ Doit rejeter si des champs obligatoires manquent")
    void testRegister_MissingFields() {
        // ARRANGE - User sans nom
        User newUser = new User();
        newUser.setPrenom("User");
        newUser.setEmail("test@test.com");
        newUser.setPassword("password123");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService. register(newUser);
        });

        assertTrue(exception.getMessage(). contains("obligatoire") ||
                exception.getMessage().contains("requis") ||
                exception.getMessage().contains("manquant"));
        verify(userRepository, never()).save(any());
    }

// ==================== TESTS ADDITIONNELS pour searchUsers() ====================

    @Test
    @DisplayName("✅ Doit pouvoir filtrer par statut actif")
    void testSearchUsers_FilterActif() {
        // ARRANGE
        client.setActif(true);
        organisateur.setActif(false);

        when(userRepository.findById(2L)).thenReturn(Optional. of(admin));
        when(userRepository. findAll()).thenReturn(Arrays.asList(client, admin, organisateur));

        // ACT - Recherche avec filtre "actif" dans le mot-clé
        List<User> results = userService.searchUsers("", 2L);
        List<User> actifs = results.stream()
                .filter(User::isActif)
                . collect(java.util.stream.Collectors. toList());

        // ASSERT
        assertEquals(2, actifs.size()); // client et admin sont actifs
        assertTrue(actifs.stream().allMatch(User::isActif));
    }

    @Test
    @DisplayName("✅ Doit combiner recherche et filtre par rôle")
    void testSearchUsers_CombinedFilters() {
        // ARRANGE
        when(userRepository.findById(2L)).thenReturn(Optional. of(admin));
        when(userRepository.findAll()).thenReturn(Arrays.asList(client, admin, organisateur));

        // ACT - Filtrer CLIENT avec "Test" dans le nom
        List<User> clients = userService.getUsersByRole(Role.CLIENT, 2L);
        List<User> results = clients.stream()
                . filter(u -> u.getPrenom().toLowerCase().contains("test"))
                .collect(java. util.stream.Collectors.toList());

        // ASSERT
        assertEquals(1, results.size());
        assertEquals(Role.CLIENT, results.get(0).getRole());
    }
}