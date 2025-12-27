package ma.projet.events.exception;

/**
 * Exception levée lors d'un accès interdit (403 Forbidden).
 *
 * Utilisée quand un utilisateur est authentifié mais n'a pas
 * les permissions nécessaires pour accéder à une ressource.
 *
 * Exemples d'utilisation :
 * - Un CLIENT essaie d'accéder à /admin/dashboard
 * - Un ORGANIZER essaie de modifier un événement d'un autre organisateur
 * - Un utilisateur tente d'annuler une réservation qui ne lui appartient pas
 *
 * @see UnauthorizedException pour les erreurs d'authentification (401)
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Constructeur avec message et cause
     *
     * @param message Message d'erreur
     * @param cause Exception originale
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}