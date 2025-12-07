package ma. projet.events.entity;

public enum EventCategory {
    CONCERT("Concert", "🎵"),
    THEATRE("Théâtre", "🎭"),
    CONFERENCE("Conférence", "🎤"),
    SPORT("Sport", "⚽"),
    AUTRE("Autre", "🎪");

    private final String label;
    private final String icon;

    EventCategory(String label, String icon) {
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