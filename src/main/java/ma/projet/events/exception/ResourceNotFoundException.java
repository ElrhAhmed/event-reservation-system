package ma.projet.events.exception;

// Hérite de RuntimeException pour arrêter le programme proprement sans le faire planter
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}