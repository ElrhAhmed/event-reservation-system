package ma.projet.events.entity;

public enum ReservationStatus {
    EN_ATTENTE("En attente", "#FFC107"),
    CONFIRMEE("Confirmée", "#28A745"),
    ANNULEE("Annulée", "#DC3545");

    private final String label;
    private final String color;

    ReservationStatus(String label, String color) {
        this. label = label;
        this. color = color;
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