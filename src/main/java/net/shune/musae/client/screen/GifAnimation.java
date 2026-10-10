package net.shune.musae.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.MusaeMod;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@OnlyIn(Dist.CLIENT)
public class GifAnimation {

    private static final int MAX_WIDTH = 960;
    private static final int MAX_HEIGHT = 540;
    private static final Map<ResourceLocation, GifAnimation> CACHE = new HashMap<>();

    private final List<ResourceLocation> frameTextures;
    private final int[] frameDelays;
    private final int totalDuration;
    private final int width;
    private final int height;

    private GifAnimation(List<ResourceLocation> frames, int[] delays, int total, int w, int h) {
        this.frameTextures = frames;
        this.frameDelays = delays;
        this.totalDuration = total;
        this.width = w;
        this.height = h;
    }

    public ResourceLocation currentFrame() {
        if (totalDuration <= 0 || frameTextures.isEmpty()) return frameTextures.get(0);
        int t = (int) (System.currentTimeMillis() % totalDuration);
        int acc = 0;
        for (int i = 0; i < frameDelays.length; i++) {
            acc += frameDelays[i];
            if (acc > t) return frameTextures.get(i);
        }
        return frameTextures.get(frameTextures.size() - 1);
    }

    public int width() { return width; }
    public int height() { return height; }

    public static GifAnimation getOrLoad(ResourceLocation source) {
        GifAnimation cached = CACHE.get(source);
        if (cached != null) return cached;

        try {
            Minecraft mc = Minecraft.getInstance();
            Resource res = mc.getResourceManager().getResource(source)
                    .orElseThrow(() -> new RuntimeException("GIF resource not found: " + source));

            try (InputStream is = res.open();
                 ImageInputStream iis = ImageIO.createImageInputStream(is)) {

                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if (!readers.hasNext()) throw new RuntimeException("No GIF reader available");

                ImageReader reader = readers.next();
                reader.setInput(iis, false);

                int count = reader.getNumImages(true);
                if (count <= 0) throw new RuntimeException("GIF has no frames");

                int origW = reader.getWidth(0);
                int origH = reader.getHeight(0);

                float scale = Math.min(1.0f, Math.min((float) MAX_WIDTH / origW, (float) MAX_HEIGHT / origH));
                int targetW = Math.max(1, Math.round(origW * scale));
                int targetH = Math.max(1, Math.round(origH * scale));

                BufferedImage fullCanvas = new BufferedImage(origW, origH, BufferedImage.TYPE_INT_ARGB);

                List<ResourceLocation> frames = new ArrayList<>(count);
                int[] delays = new int[count];
                int total = 0;

                for (int i = 0; i < count; i++) {
                    BufferedImage rawFrame = reader.read(i);
                    FrameMeta meta = readFrameMeta(reader, i);

                    delays[i] = meta.delayMs > 0 ? meta.delayMs : 100;
                    total += delays[i];

                    BufferedImage savedPrior = null;
                    if (meta.disposal == 3) {
                        savedPrior = new BufferedImage(origW, origH, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D s = savedPrior.createGraphics();
                        s.drawImage(fullCanvas, 0, 0, null);
                        s.dispose();
                    }

                    Graphics2D gc = fullCanvas.createGraphics();
                    gc.drawImage(rawFrame, meta.left, meta.top, null);
                    gc.dispose();

                    BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = scaled.createGraphics();
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                            RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                    g.drawImage(fullCanvas, 0, 0, targetW, targetH, null);
                    g.dispose();

                    NativeImage native_ = bufferedToNative(scaled);
                    DynamicTexture tex = new DynamicTexture(native_);
                    ResourceLocation texId = ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "seletor_gif_" + System.identityHashCode(source) + "_" + i);
                    mc.getTextureManager().register(texId, tex);
                    applyLinearFilter(tex);
                    frames.add(texId);

                    if (meta.disposal == 2) {
                        Graphics2D clr = fullCanvas.createGraphics();
                        clr.setComposite(java.awt.AlphaComposite.Clear);
                        clr.fillRect(meta.left, meta.top, rawFrame.getWidth(), rawFrame.getHeight());
                        clr.dispose();
                    } else if (meta.disposal == 3 && savedPrior != null) {
                        Graphics2D rst = fullCanvas.createGraphics();
                        rst.setComposite(java.awt.AlphaComposite.Src);
                        rst.drawImage(savedPrior, 0, 0, null);
                        rst.dispose();
                    }
                }

                reader.dispose();

                GifAnimation result = new GifAnimation(frames, delays, total, targetW, targetH);
                CACHE.put(source, result);
                return result;
            }
        } catch (Exception e) {
            MusaeMod.LOGGER.error("[MusaeMod] Failed to load GIF {}: {}", source, e.getMessage());
            NativeImage fallback = new NativeImage(1, 1, false);
            fallback.setPixelRGBA(0, 0, 0xFF200030);
            DynamicTexture tex = new DynamicTexture(fallback);
            ResourceLocation texId = ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "seletor_gif_fallback");
            Minecraft.getInstance().getTextureManager().register(texId, tex);
            GifAnimation result = new GifAnimation(List.of(texId), new int[]{100}, 100, 1, 1);
            CACHE.put(source, result);
            return result;
        }
    }

    private static void applyLinearFilter(DynamicTexture tex) {
        RenderSystem.recordRenderCall(() -> {
            int id = tex.getId();
            RenderSystem.bindTexture(id);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        });
    }

    private static NativeImage bufferedToNative(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        NativeImage out = new NativeImage(NativeImage.Format.RGBA, w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                out.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        return out;
    }

    private static final class FrameMeta {
        final int delayMs;
        final int left;
        final int top;
        final int disposal;
        FrameMeta(int delayMs, int left, int top, int disposal) {
            this.delayMs = delayMs;
            this.left = left;
            this.top = top;
            this.disposal = disposal;
        }
    }

    private static FrameMeta readFrameMeta(ImageReader reader, int index) {
        int delay = 100;
        int left = 0;
        int top = 0;
        int disposal = 1;
        try {
            IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(index)
                    .getAsTree("javax_imageio_gif_image_1.0");

            IIOMetadataNode descriptor = (IIOMetadataNode) root.getElementsByTagName("ImageDescriptor").item(0);
            if (descriptor != null) {
                String l = descriptor.getAttribute("imageLeftPosition");
                String t = descriptor.getAttribute("imageTopPosition");
                if (l != null && !l.isEmpty()) left = Integer.parseInt(l);
                if (t != null && !t.isEmpty()) top = Integer.parseInt(t);
            }

            IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
            if (gce != null) {
                String dt = gce.getAttribute("delayTime");
                if (dt != null && !dt.isEmpty()) delay = Integer.parseInt(dt) * 10;
                String dm = gce.getAttribute("disposalMethod");
                if (dm != null) {
                    switch (dm) {
                        case "doNotDispose" -> disposal = 1;
                        case "restoreToBackgroundColor" -> disposal = 2;
                        case "restoreToPrevious" -> disposal = 3;
                        case "none" -> disposal = 0;
                        default -> disposal = 1;
                    }
                }
            }
        } catch (Exception ignored) {}
        return new FrameMeta(delay, left, top, disposal);
    }
}