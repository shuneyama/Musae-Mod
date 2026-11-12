package net.shune.musae.comandos;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Help {
    private Help() {
    }

    public static final long COOLDOWN_MS = 30000;
    public static final String DISCORD = "https://discord.gg/aCtc7j74V";
    public static final long EXPIRA_MS = 10 * 60 * 1000L;
    public static final long REENVIO_MS = 3 * 60 * 1000L;
    public static final String HIST_KEY = "musae_ajuda_hist";
    public static final int HIST_MAX = 200;

    static final class Req {
        int id;
        String uuid;
        String nome;
        String hora;
        String motivo;
        long criado;
        List<String> destinos;
        String status;
        boolean escalado;
        boolean encaminhado;
        boolean logado;
        String aceitoPor;
        String aceitoNome;
        long aceitoEm;
        final Set<String> recusaram = new HashSet<>();
    }

    private static final Map<Integer, Req> reqs = new LinkedHashMap<>();
    private static final Map<String, Long> ultimaAjuda = new HashMap<>();
    private static int seq = 0;

    public static void limparMemoria() {
        reqs.clear();
        ultimaAjuda.clear();
    }

    private static void avisoDiscord(MinecraftServer server, String uuid, String texto) {
        ServerPlayer p = U.player(server, uuid);
        if (p != null) U.tell(p, U.red(texto).append(U.link(DISCORD)));
    }

    private static void enviarAviso(MinecraftServer server, Req req, List<String> uuids) {
        for (String u : uuids) {
            ServerPlayer d = U.player(server, u);
            if (d == null) continue;
            MutableComponent c = Component.empty()
                    .append(U.negrito("[AJUDA] ", ChatFormatting.RED))
                    .append(U.yellow(req.nome))
                    .append(U.white(" solicitou ajuda. Horário: "))
                    .append(U.aqua(req.hora + "\n"));
            if (req.motivo != null && !req.motivo.isEmpty()) {
                c.append(U.gray("Motivo: " + req.motivo + "\n"));
            }
            c.append(U.btnRun("[ACEITAR]", ChatFormatting.GREEN, "/musae ajuda aceitar " + req.id, "Aceitar a solicitação de " + req.nome));
            c.append(U.white("  "));
            c.append(U.btnRun("[RECUSAR]", ChatFormatting.RED, "/musae ajuda recusar " + req.id, "Recusar a solicitação de " + req.nome));
            U.tell(d, c);
        }
    }

    private static void log(MinecraftServer server, Req req, String resultado) {
        if (req.logado) return;
        req.logado = true;
        try {
            CompoundTag e = new CompoundTag();
            e.putString("data", req.hora);
            e.putString("solicitante", req.nome);
            e.putString("motivo", req.motivo == null ? "" : req.motivo);
            e.putString("resultado", resultado);
            e.putString("atendente", req.aceitoNome == null ? "" : req.aceitoNome);
            e.putInt("segundos", req.aceitoEm > 0 ? (int) Math.round((req.aceitoEm - req.criado) / 1000.0) : -1);
            e.putBoolean("encaminhado", req.encaminhado);
            U.listAdd(U.root(server, HIST_KEY), "lista", e, HIST_MAX);
        } catch (Exception err) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] falha ao registrar historico de ajuda", err);
        }
    }

    public static void limpar(MinecraftServer server) {
        long agora = System.currentTimeMillis();
        for (Req r : new ArrayList<>(reqs.values())) {
            if (agora - r.criado > EXPIRA_MS) {
                if (r.status.equals("pendente")) {
                    r.status = "expirada";
                    log(server, r, "Sem atendimento");
                }
                reqs.remove(r.id);
            }
        }
    }

    public static void reenviar(MinecraftServer server, long agora) {
        if (REENVIO_MS <= 0) return;
        for (Req r : new ArrayList<>(reqs.values())) {
            if (r.status.equals("pendente") && !r.escalado && agora - r.criado > REENVIO_MS) {
                if (!escalar(server, r)) {
                    avisoDiscord(server, r.uuid, "Ainda não houve atendimento. Se preferir, abra um ticket de suporte no nosso Discord: ");
                }
            }
        }
    }

    private static boolean escalar(MinecraftServer server, Req req) {
        if (req.escalado) return false;
        req.escalado = true;

        List<String> uuids = new ArrayList<>();
        for (ServerPlayer d : Teams.online(server, new String[]{"npc"}, req.uuid)) {
            if (!req.destinos.contains(d.getStringUUID())) uuids.add(d.getStringUUID());
        }
        if (uuids.isEmpty()) return false;

        req.destinos.addAll(uuids);
        req.encaminhado = true;
        enviarAviso(server, req, uuids);

        ServerPlayer sol = U.player(server, req.uuid);
        if (sol != null) U.tell(sol, U.yellow("Sua solicitação foi encaminhada para outra equipe."));
        return true;
    }

    public static int ajuda(CommandContext<CommandSourceStack> ctx) {
        return criar(ctx, "");
    }

    public static int ajudaComMotivo(CommandContext<CommandSourceStack> ctx) {
        return criar(ctx, StringArgumentType.getString(ctx, "motivo").trim());
    }

    private static int criar(CommandContext<CommandSourceStack> ctx, String motivo) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando só pode ser usado por jogadores."));
            return 0;
        }
        MinecraftServer server = src.getServer();

        limpar(server);
        if (motivo.length() > 120) motivo = motivo.substring(0, 120);

        String uuid = p.getStringUUID();
        long agoraMs = System.currentTimeMillis();
        Long ultimo = ultimaAjuda.get(uuid);
        if (ultimo != null && agoraMs - ultimo < COOLDOWN_MS) {
            long falta = (long) Math.ceil((COOLDOWN_MS - (agoraMs - ultimo)) / 1000.0);
            U.tell(p, U.red("Aguarde " + falta + "s para solicitar ajuda novamente."));
            return 0;
        }

        boolean usouNpc = false;
        List<ServerPlayer> destinos = Teams.online(server, new String[]{"staff", "mod"}, uuid);
        if (destinos.isEmpty()) {
            destinos = Teams.online(server, new String[]{"npc"}, uuid);
            usouNpc = !destinos.isEmpty();
        }

        if (destinos.isEmpty()) {
            avisoDiscord(server, uuid, "Não há staffs online nesse momento. Mande sua solicitação de ajuda no nosso ticket de suporte no nosso Discord: ");
            return 0;
        }

        ultimaAjuda.put(uuid, agoraMs);

        Req req = new Req();
        req.id = ++seq;
        req.uuid = uuid;
        req.nome = U.nome(p);
        req.hora = U.agora();
        req.motivo = motivo;
        req.criado = agoraMs;
        req.destinos = new ArrayList<>();
        for (ServerPlayer d : destinos) req.destinos.add(d.getStringUUID());
        req.status = "pendente";
        req.escalado = usouNpc;
        reqs.put(req.id, req);

        enviarAviso(server, req, req.destinos);
        U.tell(p, U.green("Sua solicitação de ajuda foi enviada para a equipe. Aguarde um momento."));
        return 1;
    }

    private static Req carregar(CommandContext<CommandSourceStack> ctx, ServerPlayer p) {
        CommandSourceStack src = ctx.getSource();
        if (p == null) {
            U.reply(src, U.red("Esse comando só pode ser usado por jogadores."));
            return null;
        }
        limpar(src.getServer());
        Req req = reqs.get(IntegerArgumentType.getInteger(ctx, "id"));
        if (req == null) {
            U.reply(src, U.red("Solicitação não encontrada ou expirada."));
            return null;
        }
        if (!req.destinos.contains(p.getStringUUID())) {
            U.reply(src, U.red("Você não é um dos destinatários dessa solicitação."));
            return null;
        }
        return req;
    }

    public static int aceitar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        ServerPlayer p = U.player(src);
        Req req = carregar(ctx, p);
        if (req == null) return 0;
        String uuid = p.getStringUUID();

        if (req.status.equals("aceita")) {
            U.reply(src, U.red("Essa solicitação já foi aceita por " + req.aceitoNome + "."));
            return 0;
        }
        if (!req.status.equals("pendente")) {
            U.reply(src, U.red("Essa solicitação já foi encerrada."));
            return 0;
        }

        ServerPlayer sol = U.player(server, req.uuid);
        if (sol == null) {
            req.status = "encerrada";
            log(server, req, "Jogador saiu");
            U.reply(src, U.red(req.nome + " não está mais online."));
            return 0;
        }

        req.status = "aceita";
        req.aceitoPor = uuid;
        req.aceitoNome = U.nome(p);
        req.aceitoEm = System.currentTimeMillis();
        log(server, req, "Atendida");
        String hora = U.agora();

        for (String u : req.destinos) {
            if (u.equals(uuid)) continue;
            ServerPlayer d = U.player(server, u);
            if (d != null) {
                U.tell(d, U.red("[AJUDA] ")
                        .append(U.yellow(req.aceitoNome))
                        .append(U.white(" aceitou a solicitação de "))
                        .append(U.yellow(req.nome))
                        .append(U.white(" às "))
                        .append(U.aqua(hora))
                        .append(U.white(".")));
            }
        }

        U.tell(p, Component.empty()
                .append(U.negrito("[AJUDA] ", ChatFormatting.RED))
                .append(U.white("Você aceitou a solicitação de "))
                .append(U.yellow(req.nome))
                .append(U.white(" às " + hora + ".\n"))
                .append(U.btnRun("[TELEPORTAR ATÉ " + req.nome.toUpperCase() + "]", ChatFormatting.AQUA,
                        "/musae ajuda teleportar " + req.id, "Teleportar até " + req.nome)));

        U.tell(sol, U.green("Sua solicitação de ajuda foi aceita! Um membro da equipe está a caminho."));
        return 1;
    }

    public static int recusar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        ServerPlayer p = U.player(src);
        Req req = carregar(ctx, p);
        if (req == null) return 0;
        String uuid = p.getStringUUID();

        if (req.status.equals("aceita")) {
            U.reply(src, U.red("Essa solicitação já foi aceita por " + req.aceitoNome + "."));
            return 0;
        }
        if (!req.status.equals("pendente")) {
            U.reply(src, U.red("Essa solicitação já foi encerrada."));
            return 0;
        }
        if (req.recusaram.contains(uuid)) {
            U.reply(src, U.red("Você já recusou essa solicitação."));
            return 0;
        }

        req.recusaram.add(uuid);
        U.reply(src, U.yellow("Você recusou a solicitação de " + req.nome + "."));

        boolean todos = req.recusaram.containsAll(req.destinos);
        if (todos && !escalar(server, req)) {
            req.status = "recusada";
            log(server, req, "Recusada por todos");
            avisoDiscord(server, req.uuid, "Nenhum membro da equipe pôde atender sua solicitação agora. Abra um ticket de suporte no nosso Discord: ");
        }
        return 1;
    }

    public static int ir(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        Req req = carregar(ctx, p);
        if (req == null) return 0;

        if (!req.status.equals("aceita") || !p.getStringUUID().equals(req.aceitoPor)) {
            U.reply(src, U.red("Você não é o responsável por essa solicitação."));
            return 0;
        }

        ServerPlayer sol = U.player(src.getServer(), req.uuid);
        if (sol == null) {
            U.reply(src, U.red(req.nome + " não está mais online."));
            return 0;
        }

        p.teleportTo(sol.serverLevel(), sol.getX(), sol.getY(), sol.getZ(), sol.getYRot(), sol.getXRot());
        U.reply(src, U.green("Teleportado até " + req.nome + "."));
        return 1;
    }

    public static int historico(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), HIST_KEY);
        ListTag lista = root.contains("lista", 9) ? root.getList("lista", 10) : null;

        if (lista == null || lista.isEmpty()) {
            U.reply(src, U.red("Nenhum atendimento registrado ainda."));
            return 0;
        }

        int ini = Math.max(0, lista.size() - 10);
        U.reply(src, U.gold("===== Histórico de ajuda (últimos " + (lista.size() - ini) + " de " + lista.size() + ") ====="));

        for (int i = ini; i < lista.size(); i++) {
            CompoundTag e = lista.getCompound(i);
            String resultado = e.getString("resultado");
            int segs = e.getInt("segundos");
            String detalhe = resultado;
            if (resultado.equals("Atendida")) {
                detalhe += " por " + e.getString("atendente") + " em " + segs + "s";
            }
            if (e.getBoolean("encaminhado")) detalhe += " (encaminhada)";

            MutableComponent linha = U.gray("[" + e.getString("data") + "] ")
                    .append(U.yellow(e.getString("solicitante")));
            if (!e.getString("motivo").isEmpty()) {
                linha.append(U.white(" - \"" + e.getString("motivo") + "\""));
            }
            linha.append(resultado.equals("Atendida") ? U.green(" -> " + detalhe) : U.red(" -> " + detalhe));
            U.reply(src, linha);
        }
        return lista.size();
    }
}
