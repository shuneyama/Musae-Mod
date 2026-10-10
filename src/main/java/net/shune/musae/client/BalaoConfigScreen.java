package net.shune.musae.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.chat.BalaoStyle;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.SaveBalaoConfigPacket;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BalaoConfigScreen extends Screen {

    private int corTexto;
    private int corFundo;
    private int corBorda;
    private BalaoStyle estilo;
    private final float altura;

    private EditBox corTextoInput;
    private EditBox corFundoInput;
    private EditBox corBordaInput;

    private Button estiloButton;

    private static final int GUI_WIDTH = 200;
    private static final int BLOCO_ALTURA = 200;

    public BalaoConfigScreen(int corTexto, int corFundo, int corBorda, float altura, BalaoStyle estilo) {
        super(Component.literal("Customizar Balão"));
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.corBorda = corBorda;
        this.altura = altura;
        this.estilo = estilo;
    }

    public static void abrir(int corTexto, int corFundo, int corBorda, float altura, BalaoStyle estilo) {
        Minecraft.getInstance().setScreen(new BalaoConfigScreen(corTexto, corFundo, corBorda, altura, estilo));
    }

    private int blocoTopo() {
        return Math.max(28, (this.height - BLOCO_ALTURA) / 2);
    }

    private int colunaEsquerdaX() {
        return Math.max(120, this.width / 4);
    }

    @Override
    protected void init() {
        int leftColX = colunaEsquerdaX();
        int startY = blocoTopo() + 30;

        this.addRenderableWidget(Button.builder(Component.literal("Texto"), b -> {})
                .bounds(leftColX - 100, startY, 50, 16).build());
        corTextoInput = new EditBox(this.font, leftColX - 45, startY, 110, 16, Component.literal(""));
        corTextoInput.setMaxLength(6);
        corTextoInput.setValue(formatarCor(corTexto));
        corTextoInput.setResponder(v -> corTexto = parseCorOu(v, corTexto));
        this.addRenderableWidget(corTextoInput);

        this.addRenderableWidget(Button.builder(Component.literal("Fundo"), b -> {})
                .bounds(leftColX - 100, startY + 22, 50, 16).build());
        corFundoInput = new EditBox(this.font, leftColX - 45, startY + 22, 110, 16, Component.literal(""));
        corFundoInput.setMaxLength(6);
        corFundoInput.setValue(formatarCor(corFundo));
        corFundoInput.setResponder(v -> corFundo = parseCorOu(v, corFundo));
        this.addRenderableWidget(corFundoInput);

        this.addRenderableWidget(Button.builder(Component.literal("Borda"), b -> {})
                .bounds(leftColX - 100, startY + 44, 50, 16).build());
        corBordaInput = new EditBox(this.font, leftColX - 45, startY + 44, 110, 16, Component.literal(""));
        corBordaInput.setMaxLength(6);
        corBordaInput.setValue(formatarCor(corBorda));
        corBordaInput.setResponder(v -> corBorda = parseCorOu(v, corBorda));
        this.addRenderableWidget(corBordaInput);

        estiloButton = Button.builder(
                Component.literal("Estilo: " + (estilo == BalaoStyle.ROUNDED ? "Arredondado" : "Quadrado")),
                button -> {
                    estilo = (estilo == BalaoStyle.ROUNDED) ? BalaoStyle.SQUARE : BalaoStyle.ROUNDED;
                    button.setMessage(Component.literal("Estilo: " + (estilo == BalaoStyle.ROUNDED ? "Arredondado" : "Quadrado")));
                }
        ).bounds(leftColX - 100, startY + 70, GUI_WIDTH, 20).build();
        this.addRenderableWidget(estiloButton);

        this.addRenderableWidget(Button.builder(Component.literal("Resetar cores"), button -> {
            corTexto = 0x141414;
            corFundo = 0xFFFFFF;
            corBorda = 0x000000;
            estilo = BalaoStyle.ROUNDED;
            corTextoInput.setValue(formatarCor(corTexto));
            corFundoInput.setValue(formatarCor(corFundo));
            corBordaInput.setValue(formatarCor(corBorda));
            estiloButton.setMessage(Component.literal("Estilo: Arredondado"));
        }).bounds(leftColX - 100, startY + 96, GUI_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal(BalaoRenderHandler.baloesVisiveis ? "Balões: Visíveis" : "Balões: Ocultos"),
                button -> {
                    BalaoRenderHandler.baloesVisiveis = !BalaoRenderHandler.baloesVisiveis;
                    button.setMessage(Component.literal(BalaoRenderHandler.baloesVisiveis ? "Balões: Visíveis" : "Balões: Ocultos"));
                }
        ).bounds(leftColX - 100, startY + 122, GUI_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Salvar"), button -> {
            salvar();
            this.onClose();
        }).bounds(leftColX - 100, startY + 152, 95, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancelar"), button -> this.onClose())
                .bounds(leftColX + 5, startY + 152, 95, 20).build());
    }

    private String formatarCor(int cor) {
        return String.format("%06X", cor & 0xFFFFFF);
    }

    private int parseCorOu(String input, int atual) {
        try {
            return Integer.parseInt(input.replace("#", ""), 16);
        } catch (NumberFormatException ignored) {
            return atual;
        }
    }

    private void salvar() {
        NetworkHandler.CHANNEL.sendToServer(new SaveBalaoConfigPacket(corTexto, corFundo, corBorda, altura, estilo));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, blocoTopo(), 0xFFFFFF);

        int previewX = Math.min(this.width - 90, this.width * 3 / 4);
        renderPreview(guiGraphics, previewX, blocoTopo() + 90);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPreview(GuiGraphics guiGraphics, int x, int y) {
        String previewText = "Personalize seu balão!";
        int textWidth = this.font.width(previewText);
        int padding = 6;
        int balloonWidth = textWidth + padding * 2;
        int balloonHeight = 9 + padding * 2;

        int balloonX = x - balloonWidth / 2;
        int balloonY = y - balloonHeight / 2;

        float bgR = ((corFundo >> 16) & 0xFF) / 255.0F;
        float bgG = ((corFundo >> 8) & 0xFF) / 255.0F;
        float bgB = (corFundo & 0xFF) / 255.0F;

        float bordaR = ((corBorda >> 16) & 0xFF) / 255.0F;
        float bordaG = ((corBorda >> 8) & 0xFF) / 255.0F;
        float bordaB = (corBorda & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = guiGraphics.pose().last().pose();

        if (estilo == BalaoStyle.ROUNDED) {
            desenharBalaoArredondado(matrix, balloonX, balloonY, balloonWidth, balloonHeight, bgR, bgG, bgB, bordaR, bordaG, bordaB);
        } else {
            desenharBalaoQuadrado(matrix, balloonX, balloonY, balloonWidth, balloonHeight, bgR, bgG, bgB, bordaR, bordaG, bordaB);
        }

        guiGraphics.drawString(this.font, previewText, x - textWidth / 2, y - 4, corTexto | 0xFF000000, false);
    }

    private void desenharBalaoQuadrado(Matrix4f matrix, int x, int y, int width, int height,
                                        float bgR, float bgG, float bgB, float bordaR, float bordaG, float bordaB) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        addQuad(buffer, matrix, x + 1, y + 1, width - 2, height - 2, bgR, bgG, bgB);
        addQuad(buffer, matrix, x, y, width, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x, y + height - 1, width, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x, y + 1, 1, height - 2, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 1, y + 1, 1, height - 2, bordaR, bordaG, bordaB);
        BufferUploader.drawWithShader(buffer.end());
    }

    private void desenharBalaoArredondado(Matrix4f matrix, int x, int y, int width, int height,
                                           float bgR, float bgG, float bgB, float bordaR, float bordaG, float bordaB) {
        int r = 5;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        addQuad(buffer, matrix, x + r, y, width - r * 2, height, bgR, bgG, bgB);
        addQuad(buffer, matrix, x, y + r, r, height - r * 2, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + width - r, y + r, r, height - r * 2, bgR, bgG, bgB);

        addQuad(buffer, matrix, x + 1, y + 2, 1, 3, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + 2, y + 1, 1, 4, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + 3, y + 1, 2, 4, bgR, bgG, bgB);

        addQuad(buffer, matrix, x + width - 2, y + 2, 1, 3, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + width - 3, y + 1, 1, 4, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + width - 5, y + 1, 2, 4, bgR, bgG, bgB);

        addQuad(buffer, matrix, x + 1, y + height - 5, 1, 3, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + 2, y + height - 5, 1, 4, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + 3, y + height - 5, 2, 4, bgR, bgG, bgB);

        addQuad(buffer, matrix, x + width - 2, y + height - 5, 1, 3, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + width - 3, y + height - 5, 1, 4, bgR, bgG, bgB);
        addQuad(buffer, matrix, x + width - 5, y + height - 5, 2, 4, bgR, bgG, bgB);

        BufferUploader.drawWithShader(buffer.end());

        buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        addQuad(buffer, matrix, x + r, y, width - r * 2, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + r, y + height - 1, width - r * 2, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x, y + r, 1, height - r * 2, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 1, y + r, 1, height - r * 2, bordaR, bordaG, bordaB);

        addQuad(buffer, matrix, x + 3, y, 2, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + 2, y + 1, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + 1, y + 2, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x, y + 3, 1, 2, bordaR, bordaG, bordaB);

        addQuad(buffer, matrix, x + width - 5, y, 2, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 3, y + 1, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 2, y + 2, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 1, y + 3, 1, 2, bordaR, bordaG, bordaB);

        addQuad(buffer, matrix, x, y + height - 5, 1, 2, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + 1, y + height - 3, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + 2, y + height - 2, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + 3, y + height - 1, 2, 1, bordaR, bordaG, bordaB);

        addQuad(buffer, matrix, x + width - 1, y + height - 5, 1, 2, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 2, y + height - 3, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 3, y + height - 2, 1, 1, bordaR, bordaG, bordaB);
        addQuad(buffer, matrix, x + width - 5, y + height - 1, 2, 1, bordaR, bordaG, bordaB);

        BufferUploader.drawWithShader(buffer.end());
    }

    private void addQuad(BufferBuilder buffer, Matrix4f matrix, int x, int y, int width, int height, float r, float g, float b) {
        buffer.vertex(matrix, x, y + height, 0).color(r, g, b, 1.0f).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).color(r, g, b, 1.0f).endVertex();
        buffer.vertex(matrix, x + width, y, 0).color(r, g, b, 1.0f).endVertex();
        buffer.vertex(matrix, x, y, 0).color(r, g, b, 1.0f).endVertex();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
