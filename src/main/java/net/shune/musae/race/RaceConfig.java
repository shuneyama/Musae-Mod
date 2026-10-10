package net.shune.musae.race;

import net.minecraftforge.common.ForgeConfigSpec;

public class RaceConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue DEMONIO_IRA_DURATION;
    public static final ForgeConfigSpec.IntValue DEMONIO_IRA_COOLDOWN;
    public static final ForgeConfigSpec.IntValue DEMONIO_LAVA_REGEN_INTERVAL;
    public static final ForgeConfigSpec.IntValue DEMONIO_WEAKNESS_CHECK_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue DEMONIO_EXTRA_HEALTH;

    public static final ForgeConfigSpec.IntValue HUMANO_SPIRIT_THRESHOLD_HP;
    public static final ForgeConfigSpec.IntValue HUMANO_SPIRIT_DURATION;
    public static final ForgeConfigSpec.IntValue HUMANO_SPIRIT_COOLDOWN;

    public static final ForgeConfigSpec.IntValue QUIMERA_FAREJAR_COOLDOWN;
    public static final ForgeConfigSpec.IntValue QUIMERA_FAREJAR_RADIUS;
    public static final ForgeConfigSpec.IntValue QUIMERA_FAREJAR_DURATION;
    public static final ForgeConfigSpec.DoubleValue QUIMERA_EXHAUSTION_MULTIPLIER;

    public static final ForgeConfigSpec.DoubleValue QUIMERA_ALADA_HEALTH;
    public static final ForgeConfigSpec.IntValue QUIMERA_ALADA_DASH_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue QUIMERA_ALADA_DASH_FORCE;

    public static final ForgeConfigSpec.DoubleValue QUIMERA_INSETO_HEALTH;
    public static final ForgeConfigSpec.DoubleValue QUIMERA_INSETO_ARMOR;
    public static final ForgeConfigSpec.DoubleValue QUIMERA_INSETO_SCALE;
    public static final ForgeConfigSpec.IntValue QUIMERA_INSETO_VENOM_COOLDOWN;
    public static final ForgeConfigSpec.IntValue QUIMERA_INSETO_VENOM_EXPIRE;
    public static final ForgeConfigSpec.IntValue QUIMERA_INSETO_POISON_DURATION;

    public static final ForgeConfigSpec.IntValue AQUARIANO_DASH_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue AQUARIANO_DASH_FORCE;
    public static final ForgeConfigSpec.IntValue AQUARIANO_WEAKNESS_CHECK_INTERVAL;

    public static final ForgeConfigSpec.IntValue ANJO_MAX_CHARGES;
    public static final ForgeConfigSpec.IntValue ANJO_CHARGE_REGEN_TICKS;
    public static final ForgeConfigSpec.IntValue ANJO_AREA_RADIUS;
    public static final ForgeConfigSpec.IntValue ANJO_AREA_DURATION;
    public static final ForgeConfigSpec.IntValue ANJO_SINGLE_DURATION;
    public static final ForgeConfigSpec.DoubleValue ANJO_SINGLE_RANGE;

    static {
        BUILDER.push("demonio");
        DEMONIO_EXTRA_HEALTH = BUILDER.defineInRange("extra_health", 4.0, 0.0, 40.0);
        DEMONIO_IRA_DURATION = BUILDER.defineInRange("ira_duration_ticks", 400, 100, 2400);
        DEMONIO_IRA_COOLDOWN = BUILDER.defineInRange("ira_cooldown_ticks", 2400, 200, 12000);
        DEMONIO_LAVA_REGEN_INTERVAL = BUILDER.defineInRange("lava_regen_interval", 20, 5, 100);
        DEMONIO_WEAKNESS_CHECK_INTERVAL = BUILDER.defineInRange("weakness_check_interval", 10, 1, 40);
        BUILDER.pop();

        BUILDER.push("humano");
        HUMANO_SPIRIT_THRESHOLD_HP = BUILDER.defineInRange("spirit_threshold_hp", 6, 1, 20);
        HUMANO_SPIRIT_DURATION = BUILDER.defineInRange("spirit_duration_ticks", 600, 100, 2400);
        HUMANO_SPIRIT_COOLDOWN = BUILDER.defineInRange("spirit_cooldown_ticks", 600, 200, 12000);
        BUILDER.pop();

        BUILDER.push("quimera");
        QUIMERA_FAREJAR_COOLDOWN = BUILDER.defineInRange("farejar_cooldown_ticks", 1200, 200, 6000);
        QUIMERA_FAREJAR_RADIUS = BUILDER.defineInRange("farejar_radius", 30, 10, 100);
        QUIMERA_FAREJAR_DURATION = BUILDER.defineInRange("farejar_duration_ticks", 200, 40, 600);
        QUIMERA_EXHAUSTION_MULTIPLIER = BUILDER.defineInRange("exhaustion_multiplier", 2.0, 1.0, 10.0);
        BUILDER.pop();

        BUILDER.push("quimera_alada");
        QUIMERA_ALADA_HEALTH = BUILDER.defineInRange("max_health", 16.0, 4.0, 40.0);
        QUIMERA_ALADA_DASH_COOLDOWN = BUILDER.defineInRange("dash_cooldown_ticks", 200, 40, 2400);
        QUIMERA_ALADA_DASH_FORCE = BUILDER.defineInRange("dash_force", 2.0, 1.0, 10.0);
        BUILDER.pop();

        BUILDER.push("quimera_inseto");
        QUIMERA_INSETO_HEALTH = BUILDER.defineInRange("max_health", 16.0, 4.0, 40.0);
        QUIMERA_INSETO_ARMOR = BUILDER.defineInRange("armor_bonus", 4.0, 0.0, 20.0);
        QUIMERA_INSETO_SCALE = BUILDER.defineInRange("scale", 0.5, 0.1, 1.0);
        QUIMERA_INSETO_VENOM_COOLDOWN = BUILDER.defineInRange("venom_cooldown_ticks", 600, 100, 3600);
        QUIMERA_INSETO_VENOM_EXPIRE = BUILDER.defineInRange("venom_expire_ticks", 600, 100, 1200);
        QUIMERA_INSETO_POISON_DURATION = BUILDER.defineInRange("poison_duration_ticks", 100, 20, 600);
        BUILDER.pop();

        BUILDER.push("aquariano");
        AQUARIANO_DASH_COOLDOWN = BUILDER.defineInRange("dash_cooldown_ticks", 160, 40, 2400);
        AQUARIANO_DASH_FORCE = BUILDER.defineInRange("dash_force", 1.5, 0.1, 10.0);
        AQUARIANO_WEAKNESS_CHECK_INTERVAL = BUILDER.defineInRange("weakness_check_interval", 10, 1, 40);
        BUILDER.pop();

        BUILDER.push("anjo");
        ANJO_MAX_CHARGES = BUILDER.defineInRange("max_charges", 3, 1, 10);
        ANJO_CHARGE_REGEN_TICKS = BUILDER.defineInRange("charge_regen_ticks", 1200, 200, 6000);
        ANJO_AREA_RADIUS = BUILDER.defineInRange("area_radius", 5, 3, 50);
        ANJO_AREA_DURATION = BUILDER.defineInRange("area_duration_ticks", 200, 40, 600);
        ANJO_SINGLE_DURATION = BUILDER.defineInRange("single_duration_ticks", 400, 40, 1200);
        ANJO_SINGLE_RANGE = BUILDER.defineInRange("single_range", 20.0, 5.0, 50.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
