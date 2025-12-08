package ma.projet.events.service;

import ma.projet.events.entity.*;
import ma.projet.events. exception.*;
import ma.projet.events. repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time. LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public UserService(UserRepository userRepository,
                       ReservationRepository reservationRepository,
                       EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    // ==================== MÉTHODES DE BASE (déjà existantes) ====================

    /**
     * Inscription d'un nouvel utilisateur
     * Valide l'email, le mot de passe et les champs obligatoires
     *
     * @param user Utilisateur à inscrire
     * @return L'utilisateur créé
     * @throws ConflictException Si l'email existe déjà
     * @throws BusinessException Si validation échoue
     */
    @Transactional
    public User register(User user) {
        // 1. Vérifier que l'email n'existe pas déjà
        if (userRepository. findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        // 2. Valider le format de l'email
        if (user.getEmail() == null ||
                !user.getEmail().contains("@") ||
                !user.getEmail().contains(".")) {
            throw new BusinessException("Format d'email invalide");
        }

        // 3.  Valider la longueur du mot de passe (minimum 8 caractères)
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            throw new BusinessException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // 4. Valider les champs obligatoires
        if (user.getNom() == null || user.getNom().isBlank()) {
            throw new BusinessException("Le nom est obligatoire");
        }
        if (user.getPrenom() == null || user.getPrenom(). isBlank()) {
            throw new BusinessException("Le prénom est obligatoire");
        }

        // 5. Initialiser les valeurs par défaut
        if (user.getRole() == null) {
            user.setRole(Role.CLIENT);
        }
        if (user.getActif() == null) {
            user.setActif(true);
        }
        if (user.getDateInscription() == null) {
            user. setDateInscription(LocalDateTime.now());
        }

        // TODO : Plus tard, hasher le mot de passe avec BCrypt
        // user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 6.  Sauvegarder
        User savedUser = userRepository.save(user);

        System.out.println("✅ Nouvel utilisateur inscrit : " + savedUser.getEmail());

        return savedUser;
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + id));
    }

    /**
     * Récupérer un utilisateur par email
     */
    public User getUserByEmail(String email) {
        return userRepository. findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + email));
    }

    // ==================== MÉTHODE 1 : AUTHENTIFICATION ====================

    /**
     * Authentifier un utilisateur (login)
     * Vérifie email + mot de passe
     *
     * @param email Email de l'utilisateur
     * @param password Mot de passe en clair
     * @return L'utilisateur si authentification réussie
     * @throws UnauthorizedException Si email ou mot de passe incorrect
     */
    public User authenticate(String email, String password) {
        // 1. Chercher l'utilisateur par email
        User user = userRepository.findByEmail(email)
                . orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        // 2.  Vérifier que le compte est actif
        if (!user.isActif()) {
            throw new UnauthorizedException("Votre compte a été désactivé.  Contactez l'administrateur.");
        }

        // 3. Vérifier le mot de passe
        // TODO : Plus tard, utiliser BCrypt pour comparer les mots de passe hashés
        // if (!passwordEncoder.matches(password, user.getPassword())) {
        //     throw new UnauthorizedException("Email ou mot de passe incorrect");
        // }

        // Pour l'instant : comparaison simple (NON SÉCURISÉ - temporaire)
        if (!user. getPassword().equals(password)) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        System.out.println("✅ Authentification réussie : " + user.getEmail() + " (" + user.getRole(). getLabel() + ")");

        return user;
    }

    // ==================== MÉTHODE 2 : METTRE À JOUR PROFIL ====================

    /**
     * Mettre à jour le profil d'un utilisateur
     * L'utilisateur ne peut modifier que son propre profil
     *
     * @param userId ID de l'utilisateur à modifier
     * @param updatedUser Données mises à jour
     * @param currentUserId ID de l'utilisateur connecté
     * @return L'utilisateur mis à jour
     */
    @Transactional
    public User updateProfile(Long userId, User updatedUser, Long currentUserId) {
        // 1. Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // 2. Vérifier les droits (seulement son propre profil, sauf ADMIN)
        if (!userId.equals(currentUserId)) {
            // TODO : Plus tard, autoriser si currentUser est ADMIN
            throw new UnauthorizedException("Vous ne pouvez modifier que votre propre profil");
        }

        // 3. Mettre à jour les champs autorisés (pas le rôle ni le mot de passe ici)
        if (updatedUser.getNom() != null && !updatedUser.getNom(). isBlank()) {
            user. setNom(updatedUser. getNom());
        }
        if (updatedUser.getPrenom() != null && !updatedUser.getPrenom().isBlank()) {
            user.setPrenom(updatedUser.getPrenom());
        }
        if (updatedUser.getTelephone() != null) {
            user.setTelephone(updatedUser.getTelephone());
        }

        // Vérifier que le nouvel email n'est pas déjà pris
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new ConflictException("Cet email est déjà utilisé par un autre utilisateur");
            }
            user.setEmail(updatedUser.getEmail());
        }

        User updatedUserSaved = userRepository.save(user);

        System.out.println("✅ Profil mis à jour : " + user.getEmail());

        return updatedUserSaved;
    }

    // ==================== MÉTHODE 3 : CHANGER MOT DE PASSE ====================

    /**
     * Changer le mot de passe d'un utilisateur
     * Vérifie l'ancien mot de passe avant de changer
     *
     * @param userId ID de l'utilisateur
     * @param oldPassword Ancien mot de passe
     * @param newPassword Nouveau mot de passe
     * @param currentUserId ID de l'utilisateur connecté
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword, Long currentUserId) {
        // 1. Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // 2.  Vérifier les droits
        if (!userId.equals(currentUserId)) {
            throw new UnauthorizedException("Vous ne pouvez changer que votre propre mot de passe");
        }

        // 3. Vérifier l'ancien mot de passe
        // TODO : Plus tard, utiliser BCrypt
        if (!user.getPassword(). equals(oldPassword)) {
            throw new UnauthorizedException("L'ancien mot de passe est incorrect");
        }

        // 4. Valider le nouveau mot de passe
        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        // 5. Changer le mot de passe
        // TODO : Plus tard, hasher avec BCrypt
        // user.setPassword(passwordEncoder. encode(newPassword));
        user.setPassword(newPassword);

        userRepository.save(user);

        System.out.println("✅ Mot de passe changé : " + user.getEmail());
    }

    // ==================== MÉTHODE 4 : DÉSACTIVER COMPTE ====================

    /**
     * Désactiver le compte d'un utilisateur
     * Le compte devient inactif mais les données sont conservées
     *
     * @param userId ID de l'utilisateur à désactiver
     * @param adminId ID de l'admin qui effectue l'action
     */
    @Transactional
    public void deactivateAccount(Long userId, Long adminId) {
        // 1. Récupérer l'utilisateur à désactiver
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // 2. Récupérer l'admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));

        // 3.  Vérifier que l'admin a les droits
        if (!admin.isAdmin()) {
            throw new UnauthorizedException("Seul un administrateur peut désactiver un compte");
        }

        // 4. Vérifier que le compte n'est pas déjà désactivé
        if (! user.isActif()) {
            throw new BusinessException("Ce compte est déjà désactivé");
        }

        // 5. Désactiver
        user.setActif(false);
        userRepository.save(user);

        System. out.println("✅ Compte désactivé : " + user.getEmail() + " (par admin " + admin.getEmail() + ")");
    }

    // ==================== MÉTHODE 5 : ACTIVER COMPTE ====================

    /**
     * Activer (ou réactiver) le compte d'un utilisateur
     *
     * @param userId ID de l'utilisateur à activer
     * @param adminId ID de l'admin qui effectue l'action
     */
    @Transactional
    public void activateAccount(Long userId, Long adminId) {
        // 1. Récupérer l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        // 2. Récupérer l'admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));

        // 3. Vérifier les droits
        if (!admin. isAdmin()) {
            throw new UnauthorizedException("Seul un administrateur peut activer un compte");
        }

        // 4. Vérifier que le compte est désactivé
        if (user. isActif()) {
            throw new BusinessException("Ce compte est déjà actif");
        }

        // 5. Activer
        user.setActif(true);
        userRepository.save(user);

        System.out.println("✅ Compte activé : " + user.getEmail() + " (par admin " + admin.getEmail() + ")");
    }

    // ==================== MÉTHODE 6 : STATISTIQUES UTILISATEUR ====================

    /**
     * Obtenir les statistiques d'un utilisateur
     * Nombre de réservations, montant dépensé, etc.
     *
     * @param userId ID de l'utilisateur
     * @return Map contenant les statistiques
     */
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'ID : " + userId));

        Map<String, Object> stats = new HashMap<>();

        // Informations de base
        stats.put("userId", user.getId());
        stats.put("nomComplet", user.getNomComplet());
        stats.put("email", user.getEmail());
        stats.put("role", user.getRole().getLabel());
        stats.put("dateInscription", user.getDateInscription());
        stats.put("actif", user.isActif());

        // Statistiques réservations
        List<Reservation> reservations = reservationRepository.findByUtilisateurId(userId);
        stats.put("totalReservations", reservations.size());

        long confirmedReservations = reservations. stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .count();
        stats.put("reservationsConfirmees", confirmedReservations);

        long cancelledReservations = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.ANNULEE)
                .count();
        stats.put("reservationsAnnulees", cancelledReservations);

        // Montant total dépensé (réservations confirmées uniquement)
        Double montantTotal = reservationRepository.calculateTotalAmountByUser(userId);
        stats.put("montantTotalDepense", montantTotal != null ? montantTotal : 0.0);

        // Nombre total de places réservées
        int totalPlaces = reservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus. CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        stats.put("totalPlacesReservees", totalPlaces);

        // Si c'est un organisateur, ajouter stats événements
        if (user.isOrganizer()) {
            List<Event> events = eventRepository.findByOrganisateurId(userId);
            stats.put("totalEvenementsOrganises", events.size());

            long publishedEvents = events.stream()
                    .filter(e -> e. getStatut() == EventStatus. PUBLIE)
                    .count();
            stats.put("evenementsPublies", publishedEvents);
        }

        return stats;
    }

    // ==================== MÉTHODE 7 : RECHERCHER UTILISATEURS ====================

    /**
     * Rechercher des utilisateurs par nom, prénom ou email
     * Accessible uniquement aux admins
     *
     * @param keyword Mot-clé de recherche
     * @param adminId ID de l'admin qui effectue la recherche
     * @return Liste des utilisateurs trouvés
     */
    public List<User> searchUsers(String keyword, Long adminId) {
        // 1. Vérifier que l'utilisateur est admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));

        if (!admin.isAdmin()) {
            throw new UnauthorizedException("Seul un administrateur peut rechercher des utilisateurs");
        }

        // 2.  Rechercher (insensible à la casse)
        String keywordLower = keyword.toLowerCase();

        return userRepository. findAll().stream()
                .filter(u ->
                        u.getNom().toLowerCase().contains(keywordLower) ||
                                u.getPrenom().toLowerCase().contains(keywordLower) ||
                                u.getEmail().toLowerCase().contains(keywordLower)
                )
                .collect(Collectors. toList());
    }

    // ==================== MÉTHODE 8 : FILTRER PAR RÔLE ====================

    /**
     * Obtenir tous les utilisateurs d'un rôle spécifique
     * Accessible uniquement aux admins
     *
     * @param role Rôle recherché
     * @param adminId ID de l'admin qui effectue la recherche
     * @return Liste des utilisateurs avec ce rôle
     */
    public List<User> getUsersByRole(Role role, Long adminId) {
        // 1. Vérifier que l'utilisateur est admin
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));

        if (!admin.isAdmin()) {
            throw new UnauthorizedException("Seul un administrateur peut filtrer les utilisateurs par rôle");
        }

        // 2. Filtrer par rôle
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifier si un utilisateur existe
     */
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * Compter le nombre total d'utilisateurs
     */
    public long countAllUsers() {
        return userRepository.count();
    }

    /**
     * Compter les utilisateurs par rôle
     */
    public long countUsersByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .count();
    }
}