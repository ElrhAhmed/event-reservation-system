package ma.projet.events.entity;

public enum EventStatus {
    BROUILLON("Brouillon", "#FFA500"),
    PUBLIE("Publié", "#28A745"),
    ANNULE("Annulé", "#DC3545"),
    TERMINE("Terminé", "#6C757D");

    private final String label;
    private final String color;

    EventStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
    @Override
    public String toString() {
        return label;
    }
}