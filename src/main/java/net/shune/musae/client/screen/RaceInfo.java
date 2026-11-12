package net.shune.musae.client.screen;

import net.shune.musae.race.RaceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RaceInfo {

    public final RaceType type;
    public final String displayName;
    public final String tier;
    public final List<String> buffs;
    public final List<String> debuffs;
    public final int color;

    private RaceInfo(RaceType type, String displayName, String tier,
                     List<String> buffs, List<String> debuffs, int color) {
        this.type = type;
        this.displayName = displayName;
        this.tier = tier;
        this.buffs = buffs;
        this.debuffs = debuffs;
        this.color = color;
    }

    private static final Map<RaceType, RaceInfo> REGISTRY = new EnumMap<>(RaceType.class);
    private static final List<RaceType> SELECTABLE = List.of(
            RaceType.HUMANO, RaceType.DEMONIO, RaceType.ANJO,
            RaceType.AQUARIANO, RaceType.QUIMERA, RaceType.QUIMERA_ALADA, RaceType.QUIMERA_INSETO
    );

    static {
        REGISTRY.put(RaceType.HUMANO, new RaceInfo(
                RaceType.HUMANO, "Humano", "",
                List.of("Espírito Indomável ao chegar em HP baixo",
                        "Adaptabilidade a qualquer ambiente",
                        "Sem fraquezas elementais"),
                List.of("Sem habilidades passivas marcantes",
                        "Vida padrão (20 HP)"),
                0xFFE8DCB0));

        REGISTRY.put(RaceType.DEMONIO, new RaceInfo(
                RaceType.DEMONIO, "Demônio", "",
                List.of("Vida extra (24 HP)",
                        "Regeneração em lava",
                        "Ira: força e velocidade temporária",
                        "Imune a fogo"),
                List.of("Fraqueza em contato com água",
                        "Fraqueza em ambientes gelados",
                        "Não pode ativar Ira em ambiente gelado"),
                0xFFD03030));

        REGISTRY.put(RaceType.ANJO, new RaceInfo(
                RaceType.ANJO, "Anjo", "",
                List.of("3 cargas de cura recarregáveis",
                        "Cura em área para aliados próximos",
                        "Cura à distância de alvo único",
                        "Resistência divina a quedas"),
                List.of("Vida padrão (20 HP)",
                        "Vulnerável a efeitos de trevas",
                        "Cooldown longo entre cargas"),
                0xFFFFE890));

        REGISTRY.put(RaceType.AQUARIANO, new RaceInfo(
                RaceType.AQUARIANO, "Aquariano", "",
                List.of("Respiração aquática infinita",
                        "Mineração e ataque normais na água",
                        "Dash impulsionado para movimento rápido"),
                List.of("Fraqueza fora da água por muito tempo",
                        "Mais lento em ambientes secos",
                        "Dano extra a fogo"),
                0xFF50A0D0));

        REGISTRY.put(RaceType.QUIMERA, new RaceInfo(
                RaceType.QUIMERA, "Quimera", "",
                List.of("Instinto de Sobrevivência: come tudo",
                        "Farejar: detecta entidades ao redor",
                        "Digere couro, ossos, folhas e cogumelos"),
                List.of("Fome consumida 2x mais rápido",
                        "Sem buffs de combate diretos"),
                0xFF8B5A2B));

        REGISTRY.put(RaceType.QUIMERA_ALADA, new RaceInfo(
                RaceType.QUIMERA_ALADA, "Quimera Alada", "",
                List.of("Asas naturais (elytra permanente)",
                        "Dash aéreo impulsionado",
                        "Mesmos benefícios alimentares de Quimera"),
                List.of("Vida menor (16 HP)",
                        "Fome consumida 2x mais rápido",
                        "Asas não removíveis"),
                0xFFC080E0));

        REGISTRY.put(RaceType.QUIMERA_INSETO, new RaceInfo(
                RaceType.QUIMERA_INSETO, "Quimera Inseto", "",
                List.of("Carapaça natural (+4 armadura)",
                        "Veneno no próximo ataque",
                        "Mesmos benefícios alimentares de Quimera",
                        "Tamanho menor (esquiva natural)"),
                List.of("Vida menor (16 HP)",
                        "Fome consumida 2x mais rápido",
                        "Altura fixa de 1 bloco"),
                0xFF80C040));
    }

    public static RaceInfo get(RaceType type) {
        return REGISTRY.get(type);
    }

    public static List<RaceType> selectable() {
        return SELECTABLE;
    }
}