package net.shune.musae.comandos;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import java.util.Map;

public final class Selecao {
    public static final class Pt {
        public final int bx, by, bz;
        public final double fx, fy, fz;
        public final String dim;

        Pt(ServerPlayer p) {
            bx = Mth.floor(p.getX());
            by = Mth.floor(p.getY());
            bz = Mth.floor(p.getZ());
            fx = p.getX();
            fy = p.getY();
            fz = p.getZ();
            dim = U.dim(p);
        }
    }

    public Pt p1;
    public Pt p2;

    public boolean completa() {
        return p1 != null && p2 != null;
    }

    public CompoundTag toArea(String criador, boolean comTp) {
        CompoundTag a = new CompoundTag();
        a.putString("dim", p1.dim);
        a.putInt("minx", Math.min(p1.bx, p2.bx));
        a.putInt("maxx", Math.max(p1.bx, p2.bx));
        a.putInt("miny", Math.min(p1.by, p2.by));
        a.putInt("maxy", Math.max(p1.by, p2.by));
        a.putInt("minz", Math.min(p1.bz, p2.bz));
        a.putInt("maxz", Math.max(p1.bz, p2.bz));
        if (comTp) {
            a.putDouble("tpx", p1.fx);
            a.putDouble("tpy", p1.fy);
            a.putDouble("tpz", p1.fz);
        }
        a.putString("criador", criador);
        a.putString("data", U.agora());
        return a;
    }

    public static String tamanho(CompoundTag a) {
        int tx = a.getInt("maxx") - a.getInt("minx") + 1;
        int ty = a.getInt("maxy") - a.getInt("miny") + 1;
        int tz = a.getInt("maxz") - a.getInt("minz") + 1;
        return tx + "x" + ty + "x" + tz;
    }

    public static int setPos(CommandContext<CommandSourceStack> ctx, Map<String, Selecao> mapa, int num,
                             String rotulo, String tamLabel, String dica) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer p = U.player(src);
        if (p == null) {
            U.reply(src, U.red("Esse comando precisa ser usado por um jogador (usa a sua posição)."));
            return 0;
        }

        Selecao sel = mapa.computeIfAbsent(p.getStringUUID(), k -> new Selecao());
        Pt novo = new Pt(p);
        if (num == 1) sel.p1 = novo;
        else sel.p2 = novo;

        int outro = num == 1 ? 2 : 1;
        Pt outroPt = outro == 1 ? sel.p1 : sel.p2;
        String pos = novo.bx + ", " + novo.by + ", " + novo.bz;

        if (outroPt == null) {
            U.reply(src, U.green(rotulo + " definido em " + pos + ". Agora defina o pos" + outro + "."));
            return 1;
        }

        if (!outroPt.dim.equals(novo.dim)) {
            if (num == 1) sel.p1 = null;
            else sel.p2 = null;
            U.reply(src, U.red("Os dois pontos precisam estar na mesma dimensão. Refaça o pos" + num + "."));
            return 0;
        }

        int tx = Math.abs(sel.p1.bx - sel.p2.bx) + 1;
        int ty = Math.abs(sel.p1.by - sel.p2.by) + 1;
        int tz = Math.abs(sel.p1.bz - sel.p2.bz) + 1;
        U.reply(src, U.green(rotulo + " definido em " + pos + ". " + tamLabel + ": " + tx + "x" + ty + "x" + tz
                + " blocos. " + dica));
        return 1;
    }
}
