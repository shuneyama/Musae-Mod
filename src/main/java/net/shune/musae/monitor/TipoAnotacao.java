package net.shune.musae.monitor;

import net.minecraft.ChatFormatting;

public enum TipoAnotacao {
    ELOGIO("elogio", "Elogio", ChatFormatting.GREEN),
    AVISO("aviso", "Aviso", ChatFormatting.GOLD),
    OBSERVACAO("observacao", "Observação", ChatFormatting.YELLOW);

    private final String id;
    private final String nomeExibicao;
    private final ChatFormatting cor;

    TipoAnotacao(String id, String nomeExibicao, ChatFormatting cor) {
        this.id = id;
        this.nomeExibicao = nomeExibicao;
        this.cor = cor;
    }

    public String getId() {
        return id;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public ChatFormatting getCor() {
        return cor;
    }

    public static TipoAnotacao fromId(String id) {
        for (TipoAnotacao tipo : values()) {
            if (tipo.id.equalsIgnoreCase(id)) {
                return tipo;
            }
        }
        return null;
    }
}
