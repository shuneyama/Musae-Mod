package net.shune.musae.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.chat.BalaoConfig;
import net.shune.musae.chat.BalaoStyle;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BalaoRenderer {

    private static final int BALLOON_PADDING = 8;
    private static final int MIN_BALLOON_WIDTH = 13;
    private static final int DISTANCE_BETWEEN_BALLOONS = 18;
    private static final Minecraft client = Minecraft.getInstance();

    public static void renderBaloes(PoseStack poseStack, EntityRenderDispatcher dispatcher, Font font,
                                     List<BalaoClientManager.BalaoMensagem> mensagens, float playerHeight) {
        Quaternionf rotation = Axis.YP.rotationDegrees(toEulerXyzDegrees(dispatcher.cameraOrientation()).y + 180);

        long currentTime = System.currentTimeMillis();
        int balloonDistance = 0;
        int previousBalloonHeight = 0;

        for (int i = 0; i < mensagens.size(); i++) {
            BalaoClientManager.BalaoMensagem msg = mensagens.get(i);

            String visibleText = msg.getTextoVisivel(currentTime);

            if (visibleText.isEmpty()) {
                continue;
            }

            poseStack.pushPose();

            poseStack.translate(0.0, playerHeight + msg.altura(), 0.0);
            poseStack.mulPose(rotation);
            poseStack.scale(-0.025F, -0.025F, 0.025F);

            List<FormattedCharSequence> lines = font.split(FormattedText.of(visibleText), BalaoConfig.MAX_BALLOON_WIDTH);

            int maxTextWidth = 0;
            for (FormattedCharSequence line : lines) {
                int w = font.width(line);
                if (w > maxTextWidth) maxTextWidth = w;
            }

            if (maxTextWidth < 8) maxTextWidth = 8;

            int balloonWidth = Mth.clamp(maxTextWidth + BALLOON_PADDING * 2,
                    MIN_BALLOON_WIDTH + BALLOON_PADDING * 2, BalaoConfig.MAX_BALLOON_WIDTH + BALLOON_PADDING * 2);
            int balloonHeight = lines.size();

            if (balloonWidth % 2 == 0) balloonWidth--;

            if (previousBalloonHeight != 0) {
                balloonDistance += 9 * previousBalloonHeight + DISTANCE_BETWEEN_BALLOONS;
            }
            previousBalloonHeight = balloonHeight;

            int baseX = balloonWidth / 2;
            int baseY = (-balloonHeight - (balloonHeight - 1) * 7) - (balloonHeight - 1);

            Matrix4f matrix = poseStack.last().pose();

            float bgR = ((msg.corFundo() >> 16) & 0xFF) / 255.0F;
            float bgG = ((msg.corFundo() >> 8) & 0xFF) / 255.0F;
            float bgB = (msg.corFundo() & 0xFF) / 255.0F;

            float bordaR = ((msg.corBorda() >> 16) & 0xFF) / 255.0F;
            float bordaG = ((msg.corBorda() >> 8) & 0xFF) / 255.0F;
            float bordaB = (msg.corBorda() & 0xFF) / 255.0F;

            int contentHeight = balloonHeight * 9;
            int totalHeight = contentHeight + BALLOON_PADDING * 2;

            RenderSystem.disableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            if (msg.estilo() == BalaoStyle.ROUNDED) {
                drawRoundedBalloon(matrix, -baseX - 2, baseY - balloonDistance, balloonWidth + 4, totalHeight,
                        bgR, bgG, bgB, bordaR, bordaG, bordaB);
            } else {
                drawSquareBalloon(matrix, -baseX - 2, baseY - balloonDistance, balloonWidth + 4, totalHeight,
                        bgR, bgG, bgB, bordaR, bordaG, bordaB);
            }

            if (i == 0) {
                drawTail(matrix, -3, baseY - balloonDistance + totalHeight, bgR, bgG, bgB, bordaR, bordaG, bordaB);
            }

            poseStack.pushPose();
            poseStack.translate(0, 0, 1.0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.scale(1.0F, -1.0F, 1.0F);
            Matrix4f textMatrix = poseStack.last().pose();

            MultiBufferSource.BufferSource bufferSource = client.renderBuffers().bufferSource();

            if (lines.size() > 1) {
                int textY = baseY - balloonDistance + BALLOON_PADDING;
                for (FormattedCharSequence line : lines) {
                    font.drawInBatch(line, -font.width(line) / 2.0f, textY, (msg.corTexto() | 0xFF000000), false,
                            textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                    textY += 9;
                }
            } else {
                font.drawInBatch(visibleText, -maxTextWidth / 2.0f, baseY - balloonDistance + BALLOON_PADDING, (msg.corTexto() | 0xFF000000), false,
                        textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            }

            bufferSource.endBatch();

            poseStack.popPose();
            poseStack.popPose();
        }

        RenderSystem.enableCull();
    }

    private static void drawRoundedBalloon(Matrix4f matrix, int x, int y, int width, int height,
                                            float bgR, float bgG, float bgB, float bordaR, float bordaG, float bordaB) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int r = 5;

        addQuad(buffer, matrix, x + r, y, width - r * 2, height, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x, y + r, r, height - r * 2, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - r, y + r, r, height - r * 2, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x + 1, y + 2, 1, 3, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 1, 1, 4, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + 1, 2, 4, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x + width - 2, y + 2, 1, 3, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + 1, 1, 4, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y + 1, 2, 4, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x + 1, y + height - 5, 1, 3, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + height - 5, 1, 4, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + height - 5, 2, 4, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x + width - 2, y + height - 5, 1, 3, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + height - 5, 1, 4, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y + height - 5, 2, 4, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x + r, y, width - r * 2, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + r, y + height - 1, width - r * 2, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + r, 1, height - r * 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + r, 1, height - r * 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        addQuad(buffer, matrix, x + 3, y, 2, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 1, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 2, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + 3, 1, 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        addQuad(buffer, matrix, x + width - 5, y, 2, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + 1, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 2, y + 2, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + 3, 1, 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        addQuad(buffer, matrix, x, y + height - 5, 1, 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + height - 3, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + height - 2, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + height - 1, 2, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        addQuad(buffer, matrix, x + width - 1, y + height - 5, 1, 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 2, y + height - 3, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 3, y + height - 2, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 5, y + height - 1, 2, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawSquareBalloon(Matrix4f matrix, int x, int y, int width, int height,
                                           float bgR, float bgG, float bgB, float bordaR, float bordaG, float bordaB) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        addQuad(buffer, matrix, x + 1, y + 1, width - 2, height - 2, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x, y, width, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + height - 1, width, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + 1, 1, height - 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + width - 1, y + 1, 1, height - 2, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void drawTail(Matrix4f matrix, int x, int y, float bgR, float bgG, float bgB,
                                  float bordaR, float bordaG, float bordaB) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        addQuad(buffer, matrix, x, y, 5, 1, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 1, 3, 1, 0, bgR, bgG, bgB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 2, 1, 1, 0, bgR, bgG, bgB, 1.0f);

        addQuad(buffer, matrix, x - 1, y, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 5, y, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x, y + 1, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 4, y + 1, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 1, y + 2, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 3, y + 2, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);
        addQuad(buffer, matrix, x + 2, y + 3, 1, 1, 0.5f, bordaR, bordaG, bordaB, 1.0f);

        BufferUploader.drawWithShader(buffer.end());
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, int x, int y, int width, int height, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y + height, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width, y + height, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + width, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }

    private static Vector3f toEulerXyz(Quaternionf q) {
        float f = q.w() * q.w();
        float g = q.x() * q.x();
        float h = q.y() * q.y();
        float i = q.z() * q.z();
        float j = f + g + h + i;
        float k = 2.0f * q.w() * q.x() - 2.0f * q.y() * q.z();
        float l = (float) Math.asin(k / j);

        if (Math.abs(k) > 0.999f * j)
            return new Vector3f(l, 2.0f * (float) Math.atan2(q.y(), q.w()), 0.0f);

        return new Vector3f(l,
                (float) Math.atan2(2.0f * q.x() * q.z() + 2.0f * q.y() * q.w(), f - g - h + i),
                (float) Math.atan2(2.0f * q.x() * q.y() + 2.0f * q.w() * q.z(), f - g + h - i));
    }

    private static Vector3f toEulerXyzDegrees(Quaternionf q) {
        Vector3f vec = toEulerXyz(q);
        return new Vector3f((float) Math.toDegrees(vec.x()), (float) Math.toDegrees(vec.y()), (float) Math.toDegrees(vec.z()));
    }
}
