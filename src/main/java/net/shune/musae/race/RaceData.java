package net.shune.musae.race;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class RaceData {

    private RaceType race = RaceType.NENHUMA;
    private int cooldownTicks = 0;
    private boolean iraAtiva = false;
    private boolean espiritoAtivo = false;
    private int espiritoTimer = 0;
    private int espiritoCooldown = 0;
    private boolean venenoProximoAtaque = false;
    private int venenoExpireTimer = 0;
    private int anjoCharges = 0;
    private int anjoRegenTimer = 0;

    private ItemStack raceElytra = ItemStack.EMPTY;

    public RaceType getRace() {
        return race;
    }

    public void setRace(RaceType race) {
        this.race = race;
        resetFlags();
    }

    public void resetFlags() {
        cooldownTicks = 0;
        iraAtiva = false;
        espiritoAtivo = false;
        espiritoTimer = 0;
        espiritoCooldown = 0;
        venenoProximoAtaque = false;
        venenoExpireTimer = 0;
        anjoCharges = race == RaceType.ANJO ? RaceConfig.ANJO_MAX_CHARGES.get() : 0;
        anjoRegenTimer = 0;
    }

    public ItemStack getRaceElytra() {
        return raceElytra;
    }

    public void setRaceElytra(ItemStack stack) {
        this.raceElytra = stack == null ? ItemStack.EMPTY : stack;
    }

    public boolean hasRaceElytra() {
        return !raceElytra.isEmpty();
    }

    public int getCooldownTicks() { return cooldownTicks; }
    public void setCooldownTicks(int ticks) { this.cooldownTicks = ticks; }
    public void decrementCooldown() { if (cooldownTicks > 0) cooldownTicks--; }

    public boolean isIraAtiva() { return iraAtiva; }
    public void setIraAtiva(boolean v) { this.iraAtiva = v; }

    public boolean isEspiritoAtivo() { return espiritoAtivo; }
    public void setEspiritoAtivo(boolean v) { this.espiritoAtivo = v; }
    public int getEspiritoTimer() { return espiritoTimer; }
    public void setEspiritoTimer(int t) { this.espiritoTimer = t; }
    public void decrementEspiritoTimer() { if (espiritoTimer > 0) espiritoTimer--; }

    public int getEspiritoCooldown() { return espiritoCooldown; }
    public void setEspiritoCooldown(int t) { this.espiritoCooldown = t; }
    public void decrementEspiritoCooldown() { if (espiritoCooldown > 0) espiritoCooldown--; }

    public boolean isVenenoProximoAtaque() { return venenoProximoAtaque; }
    public void setVenenoProximoAtaque(boolean v) { this.venenoProximoAtaque = v; }
    public int getVenenoExpireTimer() { return venenoExpireTimer; }
    public void setVenenoExpireTimer(int t) { this.venenoExpireTimer = t; }
    public void decrementVenenoExpireTimer() { if (venenoExpireTimer > 0) venenoExpireTimer--; }

    public int getAnjoCharges() { return anjoCharges; }
    public void setAnjoCharges(int c) { this.anjoCharges = Math.max(0, Math.min(c, RaceConfig.ANJO_MAX_CHARGES.get())); }
    public int getAnjoRegenTimer() { return anjoRegenTimer; }
    public void setAnjoRegenTimer(int t) { this.anjoRegenTimer = t; }
    public void decrementAnjoRegenTimer() { if (anjoRegenTimer > 0) anjoRegenTimer--; }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Race", race.getId());
        tag.putInt("Cooldown", cooldownTicks);
        tag.putBoolean("IraAtiva", iraAtiva);
        tag.putBoolean("EspiritoAtivo", espiritoAtivo);
        tag.putInt("EspiritoTimer", espiritoTimer);
        tag.putInt("EspiritoCooldown", espiritoCooldown);
        tag.putBoolean("VenenoProximoAtaque", venenoProximoAtaque);
        tag.putInt("VenenoExpireTimer", venenoExpireTimer);
        tag.putInt("AnjoCharges", anjoCharges);
        tag.putInt("AnjoRegenTimer", anjoRegenTimer);

        if (!raceElytra.isEmpty()) {
            tag.put("RaceElytra", raceElytra.save(new CompoundTag()));
        }

        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        race = RaceType.fromId(tag.getString("Race"));
        if (race == null) race = RaceType.NENHUMA;
        cooldownTicks = tag.getInt("Cooldown");
        iraAtiva = tag.getBoolean("IraAtiva");
        espiritoAtivo = tag.getBoolean("EspiritoAtivo");
        espiritoTimer = tag.getInt("EspiritoTimer");
        espiritoCooldown = tag.getInt("EspiritoCooldown");
        venenoProximoAtaque = tag.getBoolean("VenenoProximoAtaque");
        venenoExpireTimer = tag.getInt("VenenoExpireTimer");
        anjoCharges = tag.getInt("AnjoCharges");
        anjoRegenTimer = tag.getInt("AnjoRegenTimer");

        if (tag.contains("RaceElytra")) {
            raceElytra = ItemStack.of(tag.getCompound("RaceElytra"));
        } else {
            raceElytra = ItemStack.EMPTY;
        }
    }

    public void copyFrom(RaceData other) {
        deserializeNBT(other.serializeNBT());
    }
}
