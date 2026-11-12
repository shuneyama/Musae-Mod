package net.shune.musae.comandos;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class Logs {
    private Logs() {
    }

    public static final String KEY = "musae_log";
    public static final int MAX = 500;

    public static void acao(MinecraftServer server, String quem, String acao, String detalhe) {
        try {
            CompoundTag e = new CompoundTag();
            e.putString("data", U.agora());
            e.putString("staff", quem);
            e.putString("acao", acao);
            String d = detalhe == null ? "" : detalhe;
            e.putString("detalhe", d.length() > 300 ? d.substring(0, 300) : d);
            U.listAdd(U.root(server, KEY), "lista", e, MAX);
        } catch (Exception ex) {
            net.shune.musae.MusaeMod.LOGGER.error("[musae] falha ao registrar log", ex);
        }
    }

    public static int ver(CommandContext<CommandSourceStack> ctx, int pagina) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);
        ListTag lista = root.contains("lista", 9) ? root.getList("lista", 10) : null;

        if (lista == null || lista.isEmpty()) {
            U.reply(src, U.red("Nenhuma ação registrada ainda."));
            return 0;
        }

        int total = lista.size();
        int paginas = (int) Math.ceil(total / 10.0);
        pagina = Math.max(1, pagina);
        if (pagina > paginas) {
            U.reply(src, U.red("Página inválida. O log tem " + paginas + " página(s)."));
            return 0;
        }

        int fim = total - (pagina - 1) * 10;
        int ini = Math.max(0, fim - 10);
        U.reply(src, U.gold("===== Log da staff - página " + pagina + "/" + paginas + " (" + total + " registros) ====="));
        for (int i = ini; i < fim; i++) {
            CompoundTag e = lista.getCompound(i);
            U.reply(src, U.gray("[" + e.getString("data") + "] ")
                    .append(U.aqua(e.getString("staff") + ": "))
                    .append(U.yellow(e.getString("acao")))
                    .append(U.white(" - " + e.getString("detalhe"))));
        }

        ServerPlayer p = U.player(src);
        if (p != null && paginas > 1) {
            MutableComponent nav = Component.empty();
            if (pagina > 1) {
                nav.append(U.btnRun("[< MAIS NOVOS]", ChatFormatting.AQUA, "/musae log " + (pagina - 1), "Ir para a página " + (pagina - 1)));
            }
            nav.append(U.gray("  Página " + pagina + "/" + paginas + "  "));
            if (pagina < paginas) {
                nav.append(U.btnRun("[MAIS ANTIGOS >]", ChatFormatting.AQUA, "/musae log " + (pagina + 1), "Ir para a página " + (pagina + 1)));
            }
            U.tell(p, nav);
        }
        return total;
    }

    public static int resetar(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CompoundTag root = U.root(src.getServer(), KEY);
        int qtd = root.contains("lista", 9) ? root.getList("lista", 10).size() : 0;
        root.remove("lista");
        net.shune.musae.MusaeMod.LOGGER.info("[musae] log resetado por " + src.getTextName() + " (" + qtd + " registros apagados)");
        U.reply(src, U.green("Log resetado. " + qtd + " registro(s) apagado(s)."));
        return 1;
    }
}
