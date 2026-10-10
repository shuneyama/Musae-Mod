package net.shune.musae.client;

import net.shune.musae.race.RaceType;

public class ClientRaceHandler {

    private static String currentRace = "nenhuma";
    private static int cooldown = 0;
    private static int anjoCharges = 0;
    private static boolean iraAtiva = false;
    private static boolean venenoAtivo = false;
    private static boolean espiritoAtivo = false;
    private static boolean raceElytraEquipped = false;

    public static void receive(String raceId, int cd, int charges,
                               boolean ira, boolean veneno, boolean espirito,
                               boolean hasElytra) {
        currentRace = raceId;
        cooldown = cd;
        anjoCharges = charges;
        iraAtiva = ira;
        venenoAtivo = veneno;
        espiritoAtivo = espirito;
        raceElytraEquipped = hasElytra;
    }

    public static RaceType getRace() { return RaceType.fromId(currentRace); }
    public static int getCooldown() { return cooldown; }
    public static int getAnjoCharges() { return anjoCharges; }
    public static boolean isIraAtiva() { return iraAtiva; }
    public static boolean isVenenoAtivo() { return venenoAtivo; }
    public static boolean isEspiritoAtivo() { return espiritoAtivo; }
    public static boolean hasRaceElytra() { return raceElytraEquipped; }
}