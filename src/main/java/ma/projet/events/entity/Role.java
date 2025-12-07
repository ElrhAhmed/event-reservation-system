package ma.projet.events.entity;

public enum Role {
    CLIENT("Client", "👤"),
    ORGANIZER("Organisateur", "🎯"),
    ADMIN("Administrateur", "⚙️");

    private final String label;
    private final String icon;

    Role(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }
    @Override
    public String toString() {
        return label;
    }
}