package net.shune.musae.race;

public enum RaceType {
    NENHUMA("nenhuma", "Nenhuma", ""),
    DEMONIO("demonio", "Demônio", "\uD83D\uDC79"),
    HUMANO("humano", "Humano", "\uD83D\uDC64"),
    QUIMERA("quimera", "Quimera", "\uD83D\uDC31"),
    QUIMERA_ALADA("quimera_alada", "Quimera Alada", "\uD83D\uDC31"),
    QUIMERA_INSETO("quimera_inseto", "Quimera Inseto", "\uD83D\uDC31"),
    AQUARIANO("aquariano", "Aquariano", "\uD83C\uDF0A"),
    ANJO("anjo", "Anjo", "\uD83D\uDC7C"),
    STAFF("staff", "Staff", "\uD83C\uDFBC");

    private final String id;
    private final String displayName;
    private final String emoji;

    RaceType(String id, String displayName, String emoji) {
        this.id = id;
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RaceType fromId(String id) {
        for (RaceType r : values()) {
            if (r.id.equalsIgnoreCase(id)) return r;
        }
        return null;
    }

    public boolean isSelecionavel() {
        return this != NENHUMA && this != STAFF;
    }

    public boolean isQuimera() {
        return this == QUIMERA || this == QUIMERA_ALADA || this == QUIMERA_INSETO;
    }
}