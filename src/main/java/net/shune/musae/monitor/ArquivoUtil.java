package net.shune.musae.monitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArquivoUtil {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path pastaDoMundo(MinecraftServer servidor, String... subpastas) {
        Path raiz = servidor.getWorldPath(LevelResource.ROOT).resolve("musae");
        for (String parte : subpastas) {
            raiz = raiz.resolve(parte);
        }
        try {
            Files.createDirectories(raiz);
        } catch (IOException ignorado) {
        }
        return raiz;
    }

    public static String sanitizarNomeArquivo(String nome) {
        return nome.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static <T> T lerJson(Path arquivo, Class<T> tipo, T valorPadrao) {
        if (!Files.exists(arquivo)) {
            return valorPadrao;
        }
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            T resultado = GSON.fromJson(leitor, tipo);
            return resultado != null ? resultado : valorPadrao;
        } catch (IOException e) {
            return valorPadrao;
        }
    }

    public static void escreverJson(Path arquivo, Object dados) {
        try {
            Files.createDirectories(arquivo.getParent());
            try (Writer escritor = Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8)) {
                GSON.toJson(dados, escritor);
            }
        } catch (IOException ignorado) {
        }
    }

    public static void anexarTexto(Path arquivo, String linha) {
        try {
            Files.createDirectories(arquivo.getParent());
            Files.writeString(
                    arquivo,
                    linha + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException ignorado) {
        }
    }
}
