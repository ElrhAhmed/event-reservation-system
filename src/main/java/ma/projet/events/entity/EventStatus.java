package ma.projet.events.entity;

public enum EventStatus {
    BROUILLON, // En cours de création
    PUBLIE,    // Visible par les clients
    ANNULE,    // Annulé par l'organisateur
    TERMINE    // Date passée
}