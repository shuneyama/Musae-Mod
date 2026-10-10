package net.shune.musae.monitor;

public class SessaoJogador {

    public long entrada;
    public Long saida;
    public String ip;
    public double x;
    public double y;
    public double z;
    public String dimensao;

    public SessaoJogador() {
    }

    public SessaoJogador(long entrada, String ip, double x, double y, double z, String dimensao) {
        this.entrada = entrada;
        this.saida = null;
        this.ip = ip;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimensao = dimensao;
    }

    public long duracaoMillis() {
        long fim = saida != null ? saida : System.currentTimeMillis();
        return Math.max(0, fim - entrada);
    }
}
