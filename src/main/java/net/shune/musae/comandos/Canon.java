package net.shune.musae.comandos;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Canon {
    private Canon() {
    }

    public static final int MAX = 3;
    public static final String PLAYERS = "musae_vc_players";
    public static final String AREAS = "musae_vc_areas";

    private static final Map<String, Selecao> pend = new HashMap<>();
    private static final Map<String, String> ultimaArea = new HashMap<>();

    private static final class Mostrar {
        final String area;
        final long ate;

        Mostrar(String area, long ate) {
            this.area = area;
            this.ate = ate;
        }
    }

    private static final Map<String, Mostrar> mostrar = new HashMap<>();

    public static void limparMemoria() {
        pend.clear();
        ultimaArea.clear();
        mostrar.clear();
    }

    private static final Map<String, String> MOTIVOS = new HashMap<>();

    private static void m(String k, String v) {
        MOTIVOS.put(k, v);
    }

    static {
        m("inFire", "Fogo");
        m("onFire", "Queimado");
        m("lava", "Lava");
        m("hotFloor", "Bloco de magma");
        m("inWall", "Sufocado em bloco");
        m("cramming", "Esmagado (entity cramming)");
        m("drown", "Afogamento");
        m("starve", "Fome");
        m("cactus", "Cacto");
        m("fall", "Queda");
        m("flyIntoWall", "Colisão com elytra");
        m("outOfWorld", "Caiu no vazio");
        m("generic", "Dano genérico");
        m("genericKill", "Kill");
        m("magic", "Magia");
        m("indirectMagic", "Magia");
        m("wither", "Efeito Wither");
        m("anvil", "Bigorna");
        m("fallingBlock", "Bloco caindo");
        m("fallingStalactite", "Estalactite caindo");
        m("stalagmite", "Estalagmite");
        m("dragonBreath", "Sopro do dragão");
        m("dryout", "Ressecamento");
        m("sweetBerryBush", "Arbusto de frutinhas");
        m("freeze", "Congelamento");
        m("sting", "Ferroada");
        m("mob", "Morto por mob");
        m("player", "Morto por jogador");
        m("arrow", "Flecha");
        m("trident", "Tridente");
        m("mobProjectile", "Projétil de mob");
        m("fireworks", "Fogos de artifício");
        m("fireball", "Bola de fogo");
        m("witherSkull", "Crânio de wither");
        m("thrown", "Projétil arremessado");
        m("thorns", "Espinhos");
        m("explosion", "Explosão");
        m("explosion.player", "Explosão");
        m("sonic_boom", "Sonic boom");
        m("badRespawnPoint", "Respawn explosivo");
        m("outsideBorder", "Fora da borda do mundo");
        m("lightningBolt", "Raio");
    }

    public static CompoundTag getRec(MinecraftServer server, ServerPlayer p) {
        CompoundTag root = U.root(server, PLAYERS);
        String uuid = p.getStringUUID();
        return root.contains(uuid, 10) ? root.getCompound(uuid) : null;
    }

    public static CompoundTag getOrCreate(MinecraftServer server, ServerPlayer p) {
        CompoundTag root = U.root(server, PLAYERS);
        String uuid = p.getStringUUID();
        if (!root.contains(uuid, 10)) {
            CompoundTag novo = new CompoundTag();
            novo.putBoolean("ativo", false);
            novo.putInt("vidas", MAX);
            novo.put("mortes", new ListTag());
            root.put(uuid, novo);
        }
        CompoundTag rec = root.getCompound(uuid);
        rec.putString("nome", U.nome(p));
        return rec;
    }

    public static int vidas(CompoundTag rec) {
        return rec == null ? MAX : rec.getInt("vidas");
    }

    public static String achaArea(CompoundTag root, String nome) {
        if (root.contains(nome)) return nome;
        for (String k : U.keys(root)) {
            if (k.equalsIgnoreCase(nome)) return k;
        }
        return null;
    }

    public static boolean dentro(CompoundTag a, Entity p) {
        int x = Mth.floor(p.getX());
        int y = Mth.floor(p.getY());
        int z = Mth.floor(p.getZ());
        return x >= a.getInt("minx") && x <= a.getInt("maxx")
                && y >= a.getInt("miny") && y <= a.getInt("maxy")
                && z >= a.getInt("minz") && z <= a.getInt("maxz");
    }

    public static String areaDe(MinecraftServer server, ServerPlayer p) {
        CompoundTag root = U.root(server, AREAS);
        if (root.size() == 0) return null;
        String dim = U.dim(p);
        for (String nome : U.keys(root)) {
            CompoundTag a = root.getCompound(nome);
            if (!a.getString("dim").equals(dim)) continue;
            if (dentro(a, p)) return nome;
        }
        return null;
    }

    public static boolean dentroDeArea(MinecraftServer server, ServerPlayer p) {
        return areaDe(server, p) != null;
    }

    private static String motivo(DamageSource src, ServerPlayer player) {
        try {
            String id = src.getMsgId();
            String motivo = MOTIVOS.getOrDefault(id, id);
            Entity killer = src.getEntity();
            if (killer != null && !killer.getUUID().equals(player.getUUID())) {
                motivo += " (por " + killer.getName().getString() + ")";
            }
            return motivo;
        } catch (Exception e) {
            return "Desconhecido";
        }
    }

    public static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> sugerirAreas(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        try {
            CompoundTag root = U.root(ctx.getSource().getServer(), AREAS);
            for (String k : U.keys(root)) builder.suggest(U.arg(k));
        } catch (Exception ignored) {
        }
        return builder.buildFuture();
    }


    public static void tick(MinecraftServer server, long agora) {
        Help.limpar(server);
        Help.reenviar(server, agora);

        for (ServerPlayer p : U.players(server)) {
            String uuid = p.getStringUUID();
            String area = areaDe(server, p);

            String antes = ultimaArea.get(uuid);
            if (!Objects.equals(area, antes)) {
                if (antes != null) U.tell(p, U.gray("Você saiu da área \"" + antes + "\"."));
                if (area != null) U.tell(p, U.red("Você entrou na área \"" + area + "\". Vidas Canon ativo!"));
                ultimaArea.put(uuid, area);
            }

            if (area != null) {
                p.displayClientMessage(U.red("Área Vidas Canon: " + area + " - tome cuidado"), true);
                continue;
            }

            CompoundTag rec = getRec(server, p);
            if ((rec != null && rec.getBoolean("ativo")) || Teams.in(server, "npc", uuid)) {
                p.displayClientMessage(U.red("Vidas canon ativa"), true);
            }
        }

        try {
            Evento.tick(server);
        } catch (Exception e) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] erro no evento", e);
        }

        CompoundTag areas = U.root(server, AREAS);
        for (Map.Entry<String, Mostrar> en : new ArrayList<>(mostrar.entrySet())) {
            Mostrar mo = en.getValue();
            ServerPlayer viewer = U.player(server, en.getKey());
            if (viewer == null || agora > mo.ate || !areas.contains(mo.area)) {
                mostrar.remove(en.getKey());
                continue;
            }
            desenharArea(server, viewer, areas.getCompound(mo.area));
        }
    }

    public static void aoEntrar(ServerPlayer p) {
        ultimaArea.remove(p.getStringUUID());
    }

    public static void aoMorrer(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        CompoundTag rec = getRec(server, player);

        Entity killer = event.getSource().getEntity();
        boolean mortoPorNpc = killer instanceof ServerPlayer kp
                && !kp.getUUID().equals(player.getUUID())
                && Teams.in(server, "npc", kp.getStringUUID());

        boolean ativo = (rec != null && rec.getBoolean("ativo")) || dentroDeArea(server, player)
                || Teams.in(server, "npc", player.getStringUUID()) || mortoPorNpc;
        if (!ativo) return;

        rec = getOrCreate(server, player);

        CompoundTag nota = new CompoundTag();
        nota.putString("data", U.agora());
        String motivo = motivo(event.getSource(), player);
        if (mortoPorNpc) motivo += " [NPC]";
        nota.putString("motivo", motivo);

        ListTag mortes = rec.getList("mortes", 10);
        mortes.add(nota);
        rec.put("mortes", mortes);

        int restantes = Math.max(0, rec.getInt("vidas") - 1);
        rec.putInt("vidas", restantes);

        U.tell(player, U.red("Você perdeu uma vida canon! Vidas restantes: " + restantes + "/" + MAX));
        if (restantes == 0) {
            U.tell(player, U.darkRed("Você não possui mais vidas canon."));
        }

        MutableComponent aviso = U.red("[VIDAS CANON] ")
                .append(U.yellow(U.nome(player)))
                .append(U.white(" morreu: " + motivo + ". Restam " + restantes + "/" + MAX + "."));
        for (ServerPlayer d : Teams.staffOnlineGeral(server, player.getStringUUID())) {
            U.tell(d, aviso);
        }
    }

    private static List<double[]> pontosArea(CompoundTag a) {
        int x1 = a.getInt("minx");
        int x2 = a.getInt("maxx") + 1;
        int y1 = a.getInt("miny");
        int y2 = a.getInt("maxy") + 1;
        int z1 = a.getInt("minz");
        int z2 = a.getInt("maxz") + 1;

        int[][] arestas = {
                {x1, y1, z1, x2, y1, z1}, {x1, y2, z1, x2, y2, z1}, {x1, y1, z2, x2, y1, z2}, {x1, y2, z2, x2, y2, z2},
                {x1, y1, z1, x1, y2, z1}, {x2, y1, z1, x2, y2, z1}, {x1, y1, z2, x1, y2, z2}, {x2, y1, z2, x2, y2, z2},
                {x1, y1, z1, x1, y1, z2}, {x2, y1, z1, x2, y1, z2}, {x1, y2, z1, x1, y2, z2}, {x2, y2, z1, x2, y2, z2}
        };

        int total = 4 * ((x2 - x1) + (y2 - y1) + (z2 - z1));
        int passo = Math.max(1, (int) Math.ceil(total / 400.0));

        List<double[]> pts = new ArrayList<>();
        for (int[] e : arestas) {
            int dx = e[3] - e[0];
            int dy = e[4] - e[1];
            int dz = e[5] - e[2];
            int len = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
            for (int t = 0; t <= len; t += passo) {
                double f = len == 0 ? 0 : (double) t / len;
                pts.add(new double[]{e[0] + dx * f, e[1] + dy * f, e[2] + dz * f});
            }
        }
        return pts;
    }

    private static void desenharArea(MinecraftServer server, ServerPlayer viewer, CompoundTag a) {
        ServerLevel level = U.level(server, a.getString("dim"));
        if (level == null) return;
        for (double[] pt : pontosArea(a)) {
            level.sendParticles(viewer, ParticleTypes.END_ROD, true, pt[0], pt[1], pt[2], 1, 0, 0, 0, 0);
        }
    }

    private static Collection<ServerPlayer> alvos(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayers(ctx, "alvos");
    }

    public static int ativar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        List<String> nomes = new ArrayList<>();
        for (ServerPlayer p : alvos(ctx)) {
            CompoundTag rec = getOrCreate(src.getServer(), p);
            rec.putBoolean("ativo", true);
            U.tell(p, U.red("Vidas Canon ativado! Suas vidas: " + rec.getInt("vidas") + "/" + MAX));
            nomes.add(U.nome(p));
        }
        Logs.acao(src.getServer(), src.getTextName(), "Vidas Canon ativado", String.join(", ", nomes));
        U.reply(src, U.green("Vidas Canon ativado para " + nomes.size() + " jogador(es)."));
        return nomes.size();
    }

    public static int desativar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        List<String> nomes = new ArrayList<>();
        for (ServerPlayer p : alvos(ctx)) {
            CompoundTag rec = getOrCreate(src.getServer(), p);
            rec.putBoolean("ativo", false);
            U.tell(p, U.green("Seu Vidas Canon foi desativado."));
            nomes.add(U.nome(p));
        }
        Logs.acao(src.getServer(), src.getTextName(), "Vidas Canon desativado", String.join(", ", nomes));
        U.reply(src, U.green("Vidas Canon desativado para " + nomes.size() + " jogador(es)."));
        return nomes.size();
    }

    public static int ver(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        MinecraftServer server = src.getServer();
        int n = 0;
        for (ServerPlayer p : alvos(ctx)) {
            CompoundTag rec = getRec(server, p);
            int vidas = vidas(rec);
            boolean manual = rec != null && rec.getBoolean("ativo");
            boolean naArea = dentroDeArea(server, p);
            boolean ehNpc = Teams.in(server, "npc", p.getStringUUID());

            String status = "Desativado";
            if (ehNpc) status = "Ativo (equipe NPC)";
            else if (manual && naArea) status = "Ativo (manual + dentro da área)";
            else if (manual) status = "Ativo (manual)";
            else if (naArea) status = "Ativo (dentro da área)";

            String txt = vidas + "/" + MAX;
            MutableComponent vidasTxt = vidas >= MAX ? U.green(txt) : (vidas == 2 ? U.yellow(txt) : U.red(txt));

            U.reply(src, U.gold("===== Vidas Canon de " + U.nome(p) + " ====="));
            U.reply(src, U.gray("Status: ").append(U.white(status)));
            U.reply(src, U.gray("Vidas: ").append(vidasTxt));

            ListTag mortes = rec != null ? rec.getList("mortes", 10) : null;
            if (mortes == null || mortes.isEmpty()) {
                U.reply(src, U.gray("Nenhum registro."));
            } else {
                U.reply(src, U.gray("Histórico:"));
                for (int i = 0; i < mortes.size(); i++) {
                    CompoundTag mo = mortes.getCompound(i);
                    String tipo = mo.getString("tipo");
                    MutableComponent linha = U.yellow("#" + (i + 1) + " ")
                            .append(U.gray("[" + mo.getString("data") + "] "));
                    if (tipo.equals("remocao")) {
                        linha.append(U.red("Vida removida pelo staff " + mo.getString("staff")));
                    } else if (tipo.equals("adicao")) {
                        linha.append(U.green("Vida adicionada pelo staff " + mo.getString("staff")));
                    } else {
                        linha.append(U.white("Morte: " + mo.getString("motivo")));
                    }
                    U.reply(src, linha);
                }
            }
            n++;
        }
        return n;
    }

    public static int mudarVida(CommandContext<CommandSourceStack> ctx, int delta) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        int ok = 0;
        int ignorados = 0;
        for (ServerPlayer p : alvos(ctx)) {
            CompoundTag rec = getOrCreate(src.getServer(), p);
            int novo = rec.getInt("vidas") + delta;
            if (novo > MAX || novo < 0) {
                ignorados++;
                continue;
            }
            rec.putInt("vidas", novo);
            Logs.acao(src.getServer(), src.getTextName(), delta > 0 ? "Vida adicionada" : "Vida removida",
                    U.nome(p) + " (agora " + novo + "/" + MAX + ")");

            CompoundTag reg = new CompoundTag();
            reg.putString("data", U.agora());
            reg.putString("tipo", delta > 0 ? "adicao" : "remocao");
            reg.putString("staff", src.getTextName());
            ListTag hist = rec.getList("mortes", 10);
            hist.add(reg);
            rec.put("mortes", hist);

            if (delta > 0) U.tell(p, U.green("Você ganhou uma vida canon! Vidas: " + novo + "/" + MAX));
            else U.tell(p, U.red("Você perdeu uma vida canon! Vidas: " + novo + "/" + MAX));
            ok++;
        }

        String acao = delta > 0 ? "adicionada para " : "removida de ";
        if (ok > 0) U.reply(src, U.green("Vida " + acao + ok + " jogador(es)."));
        if (ignorados > 0) {
            String motivo = delta > 0 ? "já estavam com " + MAX + "/" + MAX : "já estavam com 0/" + MAX;
            U.reply(src, U.red(ignorados + " jogador(es) ignorado(s): " + motivo + "."));
        }
        return ok;
    }

    public static int resetar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();
        int n = 0;
        for (ServerPlayer p : alvos(ctx)) {
            CompoundTag rec = getOrCreate(src.getServer(), p);
            rec.putInt("vidas", MAX);
            rec.put("mortes", new ListTag());
            Logs.acao(src.getServer(), src.getTextName(), "Vidas Canon resetado", U.nome(p) + " (histórico apagado)");
            U.tell(p, U.green("Suas vidas canon foram resetadas: " + MAX + "/" + MAX));
            n++;
        }
        U.reply(src, U.green("Vidas Canon resetado para " + n + " jogador(es)."));
        return n;
    }

    public static int setPos(CommandContext<CommandSourceStack> ctx, int num) {
        return Selecao.setPos(ctx, pend, num, "Ponto " + num, "Tamanho da seleção",
                "Use /musae vidas_canon area adicionar \"nome\" para criar a área.");
    }

    public static int areaAdicionar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        Selecao sel = pend.get(p.getStringUUID());
        if (sel == null || !sel.completa()) {
            U.reply(src, U.red("Defina primeiro os dois pontos com /musae vidas_canon area pos1 e area pos2."));
            return 0;
        }

        String nome = StringArgumentType.getString(ctx, "nome").trim();
        if (nome.isEmpty()) {
            U.reply(src, U.red("Nome inválido."));
            return 0;
        }

        CompoundTag root = U.root(src.getServer(), AREAS);
        if (achaArea(root, nome) != null) {
            U.reply(src, U.red("Já existe uma área chamada \"" + nome + "\"."));
            return 0;
        }

        CompoundTag a = sel.toArea(src.getTextName(), true);
        root.put(nome, a);
        Logs.acao(src.getServer(), src.getTextName(), "Área criada", nome);
        pend.remove(p.getStringUUID());

        U.reply(src, U.green("Área \"" + nome + "\" criada! Tamanho: " + Selecao.tamanho(a) + " blocos (X, Y, Z)."));
        return 1;
    }

    public static int areaVer(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), AREAS);

        if (root.size() == 0) {
            U.reply(src, U.red("Nenhuma área de Vidas Canon criada."));
            return 0;
        }

        U.reply(src, U.gold("===== Áreas de Vidas Canon (" + root.size() + ") ====="));
        for (String nome : U.keys(root)) {
            CompoundTag a = root.getCompound(nome);
            U.reply(src, U.yellow(nome).append(U.gray(" - " + a.getString("dim") + " - " + Selecao.tamanho(a))));
            U.reply(src, U.gray("  de " + a.getInt("minx") + ", " + a.getInt("miny") + ", " + a.getInt("minz")
                    + " até " + a.getInt("maxx") + ", " + a.getInt("maxy") + ", " + a.getInt("maxz")
                    + " (criada por " + a.getString("criador") + " em " + a.getString("data") + ")"));
        }
        return root.size();
    }

    public static int areaRemover(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), AREAS);
        String nome = achaArea(root, StringArgumentType.getString(ctx, "nome"));

        if (nome == null) {
            U.reply(src, U.red("Área não encontrada. Use /musae vidas_canon area ver."));
            return 0;
        }
        root.remove(nome);
        Logs.acao(src.getServer(), src.getTextName(), "Área removida", nome);
        U.reply(src, U.green("Área \"" + nome + "\" removida."));
        return 1;
    }

    public static int teleportar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        CompoundTag root = U.root(src.getServer(), AREAS);
        String nome = achaArea(root, StringArgumentType.getString(ctx, "nome"));
        if (nome == null) {
            U.reply(src, U.red("Área não encontrada. Use /musae vidas_canon area ver."));
            return 0;
        }

        CompoundTag a = root.getCompound(nome);
        ServerLevel level = U.level(src.getServer(), a.getString("dim"));
        if (level == null) {
            U.reply(src, U.red("A dimensão dessa área não existe mais."));
            return 0;
        }
        p.teleportTo(level, a.getDouble("tpx"), a.getDouble("tpy"), a.getDouble("tpz"), p.getYRot(), p.getXRot());
        U.reply(src, U.green("Teleportado para a área \"" + nome + "\"."));
        return 1;
    }

    public static int areaMostrar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando precisa ser usado por um jogador."));
            return 0;
        }

        CompoundTag root = U.root(src.getServer(), AREAS);
        String nome = achaArea(root, StringArgumentType.getString(ctx, "nome"));
        if (nome == null) {
            U.reply(src, U.red("Área não encontrada. Use /musae vidas_canon area ver."));
            return 0;
        }

        String uuid = p.getStringUUID();
        Mostrar atual = mostrar.get(uuid);
        if (atual != null && atual.area.equals(nome)) {
            mostrar.remove(uuid);
            U.reply(src, U.gray("Limites da área \"" + nome + "\" ocultados."));
            return 1;
        }

        int seg = 30;
        mostrar.put(uuid, new Mostrar(nome, System.currentTimeMillis() + seg * 1000L));
        U.reply(src, U.green("Mostrando os limites da área \"" + nome + "\" por " + seg + "s. Use o comando de novo para ocultar."));
        return 1;
    }
}
