package net.shune.musae.comandos;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class Evento {
    private Evento() {
    }

    public static final String KEY = "musae_evento";
    private static final Map<String, Selecao> pend = new HashMap<>();

    public static void limparMemoria() {
        pend.clear();
    }

    private static String nomeEfeito(String efeito) {
        return efeito.equals("nevoa") ? "Névoa" : "Cegueira";
    }

    private static void nevoa(ServerLevel level, CompoundTag a, ServerPlayer p) {
        int r = 12;
        int px = Mth.floor(p.getX());
        int py = Mth.floor(p.getY());
        int pz = Mth.floor(p.getZ());
        int x1 = Math.max(a.getInt("minx"), px - r);
        int x2 = Math.min(a.getInt("maxx") + 1, px + r);
        int y1 = Math.max(a.getInt("miny"), py - 4);
        int y2 = Math.min(a.getInt("maxy") + 1, py + 6);
        int z1 = Math.max(a.getInt("minz"), pz - r);
        int z2 = Math.min(a.getInt("maxz") + 1, pz + r);
        if (x2 <= x1 || y2 <= y1 || z2 <= z1) return;

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < 25; i++) {
            double x = x1 + rnd.nextDouble() * (x2 - x1);
            double y = y1 + rnd.nextDouble() * (y2 - y1);
            double z = z1 + rnd.nextDouble() * (z2 - z1);
            level.sendParticles(ParticleTypes.CLOUD, x, y, z, 4, 1.5, 0.8, 1.5, 0.005);
        }
    }

    public static void tick(MinecraftServer server) {
        CompoundTag root = U.root(server, KEY);
        if (!root.getBoolean("ativo") || !root.contains("area")) return;

        boolean nevoa = root.getBoolean("nevoa");
        boolean cegueira = root.getBoolean("cegueira");
        if (!nevoa && !cegueira) return;

        CompoundTag a = root.getCompound("area");
        String dim = a.getString("dim");

        for (ServerPlayer p : U.players(server)) {
            if (!U.dim(p).equals(dim)) continue;
            if (cegueira && Canon.dentro(a, p)) {
                p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false));
            }
            if (nevoa) nevoa(p.serverLevel(), a, p);
        }
    }

    public static int setPos(CommandContext<CommandSourceStack> ctx, int num) {
        return Selecao.setPos(ctx, pend, num, "Ponto " + num + " do evento", "Seleção",
                "Use /musae evento adicionar <nevoa|cegueira>.");
    }

    public static int adicionar(CommandContext<CommandSourceStack> ctx, String efeito) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);
        ServerPlayer p = U.player(src);
        String uuid = p != null ? p.getStringUUID() : "";
        Selecao sel = p != null ? pend.get(uuid) : null;

        if (sel != null && sel.completa()) {
            CompoundTag a = sel.toArea(src.getTextName(), false);
            root.put("area", a);
            pend.remove(uuid);

            Logs.acao(src.getServer(), src.getTextName(), "Área do evento definida", Selecao.tamanho(a));
            U.reply(src, U.green("Área do evento definida! Tamanho: " + Selecao.tamanho(a) + " blocos."));
        } else if (!root.contains("area")) {
            U.reply(src, U.red("Defina primeiro os dois pontos com /musae evento pos1 e /musae evento pos2."));
            return 0;
        }

        root.putBoolean(efeito, true);
        Logs.acao(src.getServer(), src.getTextName(), "Efeito do evento adicionado", nomeEfeito(efeito));
        U.reply(src, U.green("Efeito \"" + nomeEfeito(efeito) + "\" adicionado ao evento."
                + (root.getBoolean("ativo") ? "" : " Use /musae evento ativar para começar.")));
        return 1;
    }

    public static int removerEfeito(CommandContext<CommandSourceStack> ctx, String efeito) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);

        if (!root.getBoolean(efeito)) {
            U.reply(src, U.red("O efeito \"" + nomeEfeito(efeito) + "\" não está no evento."));
            return 0;
        }
        root.putBoolean(efeito, false);
        Logs.acao(src.getServer(), src.getTextName(), "Efeito do evento removido", nomeEfeito(efeito));
        U.reply(src, U.green("Efeito \"" + nomeEfeito(efeito) + "\" removido do evento."));
        return 1;
    }

    public static int ativar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);

        if (!root.contains("area")) {
            U.reply(src, U.red("Nenhuma área definida. Use /musae evento pos1, pos2 e adicionar."));
            return 0;
        }
        if (!root.getBoolean("nevoa") && !root.getBoolean("cegueira")) {
            U.reply(src, U.red("Adicione ao menos um efeito: /musae evento adicionar <nevoa|cegueira>."));
            return 0;
        }
        if (root.getBoolean("ativo")) {
            U.reply(src, U.red("O evento já está ativo."));
            return 0;
        }

        root.putBoolean("ativo", true);
        Logs.acao(src.getServer(), src.getTextName(), "Evento ativado", "");
        U.reply(src, U.green("Evento ativado."));
        return 1;
    }

    public static int desativar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        CompoundTag root = U.root(server, KEY);

        if (!root.getBoolean("ativo")) {
            U.reply(src, U.red("O evento não está ativo."));
            return 0;
        }
        root.putBoolean("ativo", false);

        if (root.contains("area")) {
            CompoundTag a = root.getCompound("area");
            String dim = a.getString("dim");
            for (ServerPlayer p : U.players(server)) {
                if (U.dim(p).equals(dim) && Canon.dentro(a, p)) {
                    p.removeEffect(MobEffects.BLINDNESS);
                }
            }
        }

        Logs.acao(server, src.getTextName(), "Evento desativado", "");
        U.reply(src, U.green("Evento desativado."));
        return 1;
    }

    public static int ver(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);

        U.reply(src, U.gold("===== Evento ====="));
        U.reply(src, U.gray("Status: ").append(root.getBoolean("ativo") ? U.green("Ativo") : U.red("Desativado")));

        if (!root.contains("area")) {
            U.reply(src, U.gray("Área: ").append(U.white("não definida")));
        } else {
            CompoundTag a = root.getCompound("area");
            U.reply(src, U.gray("Área: ").append(U.white(a.getString("dim") + " - " + Selecao.tamanho(a))));
            U.reply(src, U.gray("  de " + a.getInt("minx") + ", " + a.getInt("miny") + ", " + a.getInt("minz")
                    + " até " + a.getInt("maxx") + ", " + a.getInt("maxy") + ", " + a.getInt("maxz")));
        }

        StringBuilder efeitos = new StringBuilder();
        if (root.getBoolean("nevoa")) efeitos.append("Névoa");
        if (root.getBoolean("cegueira")) efeitos.append(efeitos.length() > 0 ? ", " : "").append("Cegueira");
        U.reply(src, U.gray("Efeitos: ").append(U.white(efeitos.length() > 0 ? efeitos.toString() : "nenhum")));
        return 1;
    }
}
