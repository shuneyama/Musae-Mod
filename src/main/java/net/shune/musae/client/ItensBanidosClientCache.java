package net.shune.musae.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ItensBanidosClientCache {

    private static final Set<String> BANIDOS = ConcurrentHashMap.newKeySet();
    private static final Set<String> PERMITIDOS = ConcurrentHashMap.newKeySet();

    public static void setBanidos(Iterable<String> itens) {
        BANIDOS.clear();
        itens.forEach(BANIDOS::add);
    }

    public static void setPermitidos(Iterable<String> itens) {
        PERMITIDOS.clear();
        itens.forEach(PERMITIDOS::add);
    }

    public static boolean isBanido(String itemId) {
        return BANIDOS.contains(itemId);
    }

    public static boolean isPermitido(String itemId) {
        return PERMITIDOS.contains(itemId);
    }
}
