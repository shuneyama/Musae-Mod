package net.shune.musae.monitor;

import java.util.ArrayList;
import java.util.List;

public class HistoricoJogador {

    public String uuid;
    public String nome;
    public List<SessaoJogador> sessoes = new ArrayList<>();

    public HistoricoJogador() {
    }

    public HistoricoJogador(String uuid, String nome) {
        this.uuid = uuid;
        this.nome = nome;
    }

    public SessaoJogador sessaoAberta() {
        for (int i = sessoes.size() - 1; i >= 0; i--) {
            SessaoJogador sessao = sessoes.get(i);
            if (sessao.saida == null) {
                return sessao;
            }
        }
        return null;
    }
}
