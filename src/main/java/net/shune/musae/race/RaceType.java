package net.shune.musae.race;

public enum RaceType {
    NENHUMA("nenhuma", "Nenhuma"),
    DEMONIO("demonio", "Demônio"),
    HUMANO("humano", "Humano"),
    QUIMERA("quimera", "Quimera"),
    QUIMERA_ALADA("quimera_alada", "Quimera Alada"),
    QUIMERA_INSETO("quimera_inseto", "Quimera Inseto"),
    AQUARIANO("aquariano", "Aquariano"),
    ANJO("anjo", "Anjo");

    private final String id;
    private final String displayName;

    RaceType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
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

    public boolean isQuimera() {
        return this == QUIMERA || this == QUIMERA_ALADA || this == QUIMERA_INSETO;
    }
}