package net.shune.musae.monitor;

public class Anotacao {

    public String uuidJogador;
    public String nomeJogador;
    public String tipo;
    public String responsavel;
    public String comentario;
    public long horario;

    public Anotacao() {
    }

    public Anotacao(String uuidJogador, String nomeJogador, String tipo, String responsavel, String comentario, long horario) {
        this.uuidJogador = uuidJogador;
        this.nomeJogador = nomeJogador;
        this.tipo = tipo;
        this.responsavel = responsavel;
        this.comentario = comentario;
        this.horario = horario;
    }
}
