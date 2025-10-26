package g1t1.models.sessions;

public enum SessionStatus {
    /**
     * Active session. End session to save.
     */
    Active("ongoing"),
    /**
     * Ended session. Ready to be saved.
     */
    Ended("completed");

    public final String label;

    private SessionStatus(String label) {
        this.label = label;
    }

    public static SessionStatus valueOfLabel(String label) {
        return switch (label) {
            case "ongoing" -> SessionStatus.Active;
            case "completed" -> SessionStatus.Ended;
            default -> null;
        };
    }
}
