package net.shune.musae.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.MusaeMod;
import net.shune.musae.client.ClientScaleHandler;
import net.shune.musae.network.ApplyCharacterPacket;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.race.RaceType;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class SeletorScreen extends Screen {

    private static final ResourceLocation PANEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "textures/gui/seletor_panel.png");
    private static final ResourceLocation GIF_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(MusaeMod.MODID, "textures/gui/seletor_background.gif");
    private static final ResourceLocation DEEPSLATE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/polished_deepslate.png");

    private static final int PANEL_TEX_W = 176;
    private static final int PANEL_TEX_H = 128;
    private static final int PANEL_DRAW_W = 320;
    private static final int PANEL_DRAW_H = 240;

    public static final float HEIGHT_MIN = 1.50f;
    public static final float HEIGHT_MAX = 2.25f;
    public static final float HEIGHT_DEFAULT = 1.80f;
    public static final float HEIGHT_INSETO = 1.00f;
    public static final float BASE_HEIGHT = 1.80f;

    private static final int MAX_NICK_LENGTH = 16;

    private static final float ENTER_DARK_DURATION = 0.5f;
    private static final float ENTER_GIF_START = 0.5f;
    private static final float ENTER_GIF_DURATION = 0.6f;
    private static final float ENTER_PANEL_START = 0.9f;
    private static final float ENTER_PANEL_DURATION = 0.6f;

    private static final float P3_REVEAL_START = 0.8f;
    private static final float P3_REVEAL_END = 2.4f;
    private static final float P3_SUBTITLE_START = 2.4f;
    private static final float P3_HEADER_FADE_DURATION = 0.5f;
    private static final float P3_HOLD_END = 4.0f;
    private static final float P3_PHASE_A_END = 5.0f;
    private static final float P3_BLACKOUT_FULL = 5.3f;
    private static final float P3_FADEOUT_START = 8.3f;
    private static final float P3_FADEOUT_END = 9.3f;

    private int page = 0;
    private int raceIndex = 0;
    private String nickname = "";
    private float heightBlocks = HEIGHT_DEFAULT;

    private long screenStart = 0L;
    private long page3Start = 0L;
    private boolean confirmSent = false;
    private boolean musicFading = false;
    private int lastTypewriterCount = 0;

    private final Random rng = new Random();

    private EditBox nicknameField;
    private HeightSlider heightSlider;
    private Button arrowLeft;
    private Button arrowRight;
    private Button selectButton;
    private Button confirmButton;
    private Button backButton;

    private GifAnimation gif;
    private int panelX, panelY;
    private boolean initialized = false;
    private LoopingMusic music;

    public SeletorScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    protected void init() {
        this.gif = GifAnimation.getOrLoad(GIF_BACKGROUND);
        this.panelX = (this.width - PANEL_DRAW_W) / 2;
        this.panelY = (this.height - PANEL_DRAW_H) / 2 + 2;
        if (!initialized) {
            screenStart = System.currentTimeMillis();
            startMusic();
            playSound(SoundEvents.BEACON_ACTIVATE, 0.2f, 1.3f);
            initialized = true;
        }
        rebuildForPage();
    }

    private void startMusic() {
        if (music == null) {
            Minecraft.getInstance().getMusicManager().stopPlaying();
            music = new LoopingMusic(SoundEvents.BEACON_AMBIENT, 0.4f);
            Minecraft.getInstance().getSoundManager().play(music);
        }
    }

    private void playSound(SoundEvent ev, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ev, pitch, volume));
    }

    @Override
    public void removed() {
        super.removed();
        LocalPlayer lp = Minecraft.getInstance().player;
        if (lp != null) {
            ClientScaleHandler.clearPreviewOverride(lp.getUUID());
        }
        if (music != null) {
            Minecraft.getInstance().getSoundManager().stop(music);
            music = null;
        }
    }

    private void rebuildForPage() {
        this.clearWidgets();
        if (page == 0) buildPage1();
        else if (page == 1) buildPage2();
        else buildPage3();
        applyWidgetState();
    }

    private RaceType currentRace() {
        return RaceInfo.selectable().get(raceIndex);
    }

    private void buildPage1() {
        int cy = panelY + PANEL_DRAW_H - 36;
        int cx = panelX + PANEL_DRAW_W / 2;

        arrowLeft = Button.builder(Component.literal("◀"), b -> {
            raceIndex = (raceIndex - 1 + RaceInfo.selectable().size()) % RaceInfo.selectable().size();
            playSound(SoundEvents.BOOK_PAGE_TURN, 0.6f, 0.9f);
        }).bounds(panelX + 10, panelY + PANEL_DRAW_H / 2 - 10, 22, 20).build();

        arrowRight = Button.builder(Component.literal("▶"), b -> {
            raceIndex = (raceIndex + 1) % RaceInfo.selectable().size();
            playSound(SoundEvents.BOOK_PAGE_TURN, 0.6f, 1.1f);
        }).bounds(panelX + PANEL_DRAW_W - 32, panelY + PANEL_DRAW_H / 2 - 10, 22, 20).build();

        selectButton = Button.builder(Component.literal("Escolher minha raça"), b -> {
            if (currentRace() == RaceType.QUIMERA_INSETO) heightBlocks = HEIGHT_INSETO;
            else heightBlocks = HEIGHT_DEFAULT;
            page = 1;
            playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.4f);
            playSound(SoundEvents.BOOK_PAGE_TURN, 0.6f, 1.0f);
            rebuildForPage();
        }).bounds(cx - 100, cy, 200, 20).build();

        addRenderableWidget(arrowLeft);
        addRenderableWidget(arrowRight);
        addRenderableWidget(selectButton);
    }

    private void buildPage2() {
        boolean insect = currentRace() == RaceType.QUIMERA_INSETO;

        int leftColX = panelX + 22;
        int leftColW = 150;

        nicknameField = new EditBox(this.font, leftColX, panelY + 64, leftColW, 18, Component.literal("nickname"));
        nicknameField.setMaxLength(MAX_NICK_LENGTH);
        nicknameField.setValue(nickname);
        nicknameField.setFilter(s -> s.codePoints().allMatch(c ->
                Character.isLetterOrDigit(c) || c == '_' || c == ' '));
        nicknameField.setResponder(s -> nickname = s);
        addRenderableWidget(nicknameField);

        heightSlider = new HeightSlider(leftColX, panelY + 128, leftColW, 18, heightBlocks, insect);
        heightSlider.active = !insect;
        addRenderableWidget(heightSlider);

        int btnY = panelY + PANEL_DRAW_H - 36;
        int cx = panelX + PANEL_DRAW_W / 2;

        backButton = Button.builder(Component.literal("Voltar"), b -> {
            page = 0;
            playSound(SoundEvents.BOOK_PAGE_TURN, 0.5f, 0.85f);
            rebuildForPage();
        }).bounds(cx - 150, btnY, 80, 20).build();

        confirmButton = Button.builder(Component.literal("Confirmar"), b -> {
            String cleaned = nickname.trim();
            if (cleaned.isEmpty()) {
                playSound(SoundEvents.VILLAGER_NO, 0.5f, 1.0f);
                return;
            }
            if (confirmSent) return;
            confirmSent = true;
            float scale = (currentRace() == RaceType.QUIMERA_INSETO ? HEIGHT_INSETO : heightBlocks) / BASE_HEIGHT;
            NetworkHandler.CHANNEL.sendToServer(
                    new ApplyCharacterPacket(currentRace().getId(), cleaned, scale));
            page = 2;
            page3Start = System.currentTimeMillis();
            lastTypewriterCount = 0;
            playSound(SoundEvents.END_PORTAL_SPAWN, 0.4f, 1.4f);
            rebuildForPage();
        }).bounds(cx + 70, btnY, 80, 20).build();

        addRenderableWidget(backButton);
        addRenderableWidget(confirmButton);
        setInitialFocus(nicknameField);
    }

    private void buildPage3() {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        applyWidgetState();

        // 1. Fundo preto SEMPRE primeiro (atrás de tudo)
        int darkAlphaByte = darkBackdropAlphaByte();
        if (darkAlphaByte > 1) {
            g.fill(0, 0, this.width, this.height, darkAlphaByte << 24);
        }

        // 2. GIF por cima do preto
        float gifA = gifAlpha();
        if (gifA > 0.01f) renderGifBackground(g, gifA);

        // 3. Painel + conteúdo
        if (page < 2) {
            float pA = panelAlpha();
            if (pA > 0.01f) {
                drawPanel(g, pA);
                drawHeader(g, pA, 1f);
                if (page == 0) renderPage1Content(g, mouseX, mouseY, partial, pA);
                else renderPage2Content(g, mouseX, mouseY, partial, pA);
            }
            if (pA > 0.99f) super.render(g, mouseX, mouseY, partial);
        } else {
            float p3 = page3Elapsed();
            float panelPhaseFade = panelFadeForPage3(p3);
            if (panelPhaseFade > 0.01f) {
                drawPanel(g, panelPhaseFade);
            }
            float headerFade = page3HeaderAlpha(p3);
            if (headerFade > 0.01f) {
                drawHeader(g, 1f, headerFade);
            }
            renderPage3Content(g, p3);

            int fgAlpha = foregroundBlackoutAlphaByte(p3);
            if (fgAlpha > 0) {
                g.fill(0, 0, this.width, this.height, fgAlpha << 24);
            }

            handlePage3Sounds(p3);
            checkPage3Close(p3);
        }
    }

    private float page3Elapsed() {
        return (System.currentTimeMillis() - page3Start) / 1000f;
    }

    private float panelFadeForPage3(float p3) {
        // Painel some em 0.8s, e também respeita o fade do header (0.5s)
        float baseFade = Math.max(0f, 1f - p3 / 0.8f);
        return baseFade;
    }

    private float page3HeaderAlpha(float p3) {
        // Header some em 0.5s assim que entra na página 3
        if (p3 >= P3_HEADER_FADE_DURATION) return 0f;
        return 1f - p3 / P3_HEADER_FADE_DURATION;
    }

    private void handlePage3Sounds(float p3) {
        if (p3 >= P3_HOLD_END && !musicFading && music != null) {
            music.fadeOut();
            musicFading = true;
            playSound(SoundEvents.BEACON_DEACTIVATE, 0.3f, 0.5f);
        }
    }

    private void checkPage3Close(float p3) {
        if (p3 >= P3_FADEOUT_END) {
            this.minecraft.setScreen(null);
        }
    }

    private int darkBackdropAlphaByte() {
        long now = System.currentTimeMillis();
        float enterT = (now - screenStart) / 1000f;
        // Fade-in rápido: 0 → 100% em 0.5s
        float enter = Math.min(1f, enterT / ENTER_DARK_DURATION);

        if (page < 2) {
            return (int) (enter * 255f) & 0xFF;
        }

        // Página 3: fundo fica 100% preto até começar o fade out final
        float p3 = page3Elapsed();
        if (p3 < P3_FADEOUT_START) {
            return 0xFF;
        }
        if (p3 < P3_FADEOUT_END) {
            float t = (p3 - P3_FADEOUT_START) / (P3_FADEOUT_END - P3_FADEOUT_START);
            return (int) ((1f - t) * 255f) & 0xFF;
        }
        return 0;
    }

    private int foregroundBlackoutAlphaByte(float p3) {
        if (p3 < P3_PHASE_A_END) return 0;
        if (p3 < P3_BLACKOUT_FULL) {
            float t = (p3 - P3_PHASE_A_END) / (P3_BLACKOUT_FULL - P3_PHASE_A_END);
            return (int) (t * 255f) & 0xFF;
        }
        if (p3 < P3_FADEOUT_START) return 0xFF;
        if (p3 < P3_FADEOUT_END) {
            float t = (p3 - P3_FADEOUT_START) / (P3_FADEOUT_END - P3_FADEOUT_START);
            return (int) ((1f - t) * 255f) & 0xFF;
        }
        return 0;
    }

    private float gifAlpha() {
        long now = System.currentTimeMillis();
        float enterT = (now - screenStart) / 1000f;
        // GIF começa a aparecer APÓS o preto estar totalmente em posição (T=0.5)
        float fadeIn = Math.max(0f, Math.min(1f, (enterT - ENTER_GIF_START) / ENTER_GIF_DURATION));

        if (page < 2) return fadeIn;

        float p3 = page3Elapsed();
        if (p3 < P3_HOLD_END) return fadeIn;
        if (p3 < P3_PHASE_A_END) {
            // GIF some, revelando o preto que tá atrás
            float t = (p3 - P3_HOLD_END) / (P3_PHASE_A_END - P3_HOLD_END);
            return fadeIn * (1f - t);
        }
        return 0f;
    }

    private float panelAlpha() {
        long now = System.currentTimeMillis();
        float enterT = (now - screenStart) / 1000f;
        return Math.max(0f, Math.min(1f, (enterT - ENTER_PANEL_START) / ENTER_PANEL_DURATION));
    }

    private boolean widgetsShouldBeActive() {
        if (page == 2) return false;
        return panelAlpha() >= 1f;
    }

    private void applyWidgetState() {
        boolean active = widgetsShouldBeActive();
        for (var child : this.children()) {
            if (child instanceof AbstractWidget aw) {
                aw.visible = active;
                aw.active = active;
            }
        }
    }

    private void renderGifBackground(GuiGraphics g, float alpha) {
        if (gif == null) return;
        ResourceLocation frame = gif.currentFrame();
        float aspectGif = (float) gif.width() / gif.height();
        float aspectScreen = (float) this.width / this.height;
        int dw, dh;
        if (aspectGif > aspectScreen) {
            dh = this.height;
            dw = (int) (this.height * aspectGif);
        } else {
            dw = this.width;
            dh = (int) (this.width / aspectGif);
        }
        int dx = (this.width - dw) / 2;
        int dy = (this.height - dh) / 2;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        g.blit(frame, dx, dy, dw, dh, 0f, 0f, gif.width(), gif.height(), gif.width(), gif.height());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawPanel(GuiGraphics g, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        g.blit(PANEL_TEXTURE, panelX, panelY, PANEL_DRAW_W, PANEL_DRAW_H,
                0f, 0f, PANEL_TEX_W, PANEL_TEX_H, PANEL_TEX_W, PANEL_TEX_H);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void drawHeader(GuiGraphics g, float panelAlpha, float headerAlpha) {
        float effective = panelAlpha * headerAlpha;
        if (effective < 0.01f) return;
        int a = (int) (effective * 255) & 0xFF;
        int titleColor = (a << 24) | 0xFFFFFF;
        int subColor = (a << 24) | 0xCCCCCC;

        int tx = this.width / 2;
        int ty = panelY - 30;

        Component title = Component.literal("Crie seu personagem em Musae SMP")
                .withStyle(ChatFormatting.BOLD);
        g.drawCenteredString(this.font, title, tx, ty, titleColor);

        Component sub = Component.literal("Passo " + (Math.min(page, 1) + 1) + " de 2");
        g.drawCenteredString(this.font, sub, tx, ty + 12, subColor);
    }

    private void renderPage1Content(GuiGraphics g, int mouseX, int mouseY, float partial, float alpha) {
        RaceInfo info = RaceInfo.get(currentRace());
        int cx = panelX + PANEL_DRAW_W / 2;
        int top = panelY + 24;

        int a = (int) (alpha * 255) & 0xFF;
        int nameColor = ((a << 24) | (info.color & 0x00FFFFFF));
        int tierColor = (a << 24) | 0xAAAAAA;
        int labelGreen = (a << 24) | 0x80FF80;
        int labelRed = (a << 24) | 0xFF8080;
        int buffColor = (a << 24) | 0xDDFFDD;
        int debuffColor = (a << 24) | 0xFFDDDD;
        int counterColor = (a << 24) | 0x888888;

        g.drawCenteredString(this.font, Component.literal(info.displayName)
                .withStyle(ChatFormatting.BOLD), cx, top, nameColor);

        int contentStart;
        if (!info.tier.isEmpty()) {
            g.drawCenteredString(this.font, Component.literal(info.tier)
                    .withStyle(ChatFormatting.ITALIC), cx, top + 12, tierColor);
            contentStart = top + 32;
        } else {
            contentStart = top + 24;
        }

        int contentX = panelX + 44;
        int colW = PANEL_DRAW_W - 88;

        g.drawString(this.font, Component.literal("Buffs:").withStyle(ChatFormatting.GREEN),
                contentX, contentStart, labelGreen, false);
        int cursor = contentStart + 11;
        for (String b : info.buffs) {
            for (String line : wrap("• " + b, colW)) {
                g.drawString(this.font, line, contentX + 4, cursor, buffColor, false);
                cursor += 10;
            }
        }

        cursor += 14;
        g.drawString(this.font, Component.literal("Debuffs:").withStyle(ChatFormatting.RED),
                contentX, cursor, labelRed, false);
        cursor += 11;
        for (String d : info.debuffs) {
            for (String line : wrap("• " + d, colW)) {
                g.drawString(this.font, line, contentX + 4, cursor, debuffColor, false);
                cursor += 10;
            }
        }

        Component counter = Component.literal((raceIndex + 1) + " / " + RaceInfo.selectable().size());
        g.drawCenteredString(this.font, counter, cx, panelY + PANEL_DRAW_H - 56, counterColor);
    }

    private java.util.List<String> wrap(String text, int maxWidth) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String tentative = current.length() == 0 ? word : current + " " + word;
            if (this.font.width(tentative) > maxWidth && current.length() > 0) {
                result.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(tentative);
            }
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private void renderPage2Content(GuiGraphics g, int mouseX, int mouseY, float partial, float alpha) {
        boolean insect = currentRace() == RaceType.QUIMERA_INSETO;
        int a = (int) (alpha * 255) & 0xFF;
        int whiteA = (a << 24) | 0xFFFFFF;
        int grayA = (a << 24) | 0xAAAAAA;

        int leftColX = panelX + 22;
        g.drawString(this.font, Component.literal("Nickname").withStyle(ChatFormatting.BOLD),
                leftColX, panelY + 50, whiteA, false);

        g.drawString(this.font, Component.literal("Altura").withStyle(ChatFormatting.BOLD),
                leftColX, panelY + 108, whiteA, false);

        if (insect) {
            g.drawString(this.font, Component.literal("Quimera Inseto tem altura fixa")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                    leftColX, panelY + 154, grayA, false);
        }

        int previewCenterX = panelX + PANEL_DRAW_W - 40;
        int previewFootY = panelY + PANEL_DRAW_H - 56;
        int baseScale = 60;

        renderDeepslateRefs(g, previewCenterX - 100, previewFootY, baseScale, alpha);
        renderPlayerPreview(g, previewCenterX, previewFootY, baseScale, mouseX, mouseY, partial, alpha);
    }

    private void renderDeepslateRefs(GuiGraphics g, int x, int footY, int blockPx, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        int b1y = footY - blockPx;
        int b2y = footY - blockPx * 2;
        g.blit(DEEPSLATE_TEXTURE, x, b1y, blockPx, blockPx, 0f, 0f, 16, 16, 16, 16);
        g.blit(DEEPSLATE_TEXTURE, x, b2y, blockPx, blockPx, 0f, 0f, 16, 16, 16, 16);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderPlayerPreview(GuiGraphics g, int x, int y, int baseScale,
                                     int mouseX, int mouseY, float partial, float alpha) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        float currentHeight = currentRace() == RaceType.QUIMERA_INSETO ? HEIGHT_INSETO : heightBlocks;
        float hScale = currentHeight / BASE_HEIGHT;
        int finalScale = Math.max(16, Math.round(baseScale * hScale));

        ClientScaleHandler.setPreviewOverride(player.getUUID(), 1.0f);
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, x, y, finalScale,
                    (float) x - mouseX, (float) (y - finalScale) - mouseY, player);
        } finally {
            ClientScaleHandler.clearPreviewOverride(player.getUUID());
        }

        int a = (int) (alpha * 255) & 0xFF;
        int playerHeadY = y - (int) (finalScale * BASE_HEIGHT);

        RaceInfo info = RaceInfo.get(currentRace());
        Component raceName = Component.literal(info.displayName).withStyle(ChatFormatting.BOLD);
        int raceColor = (a << 24) | (info.color & 0x00FFFFFF);
        int raceWidth = this.font.width(raceName);
        int raceY = playerHeadY - 38;
        g.fill(x - raceWidth / 2 - 3, raceY - 2, x + raceWidth / 2 + 3, raceY + 10, (a / 2) << 24);
        g.drawString(this.font, raceName, x - raceWidth / 2, raceY, raceColor, false);

        String displayNick = nickname.isBlank() ? player.getGameProfile().getName() : nickname;
        Component nick = Component.literal(displayNick).withStyle(ChatFormatting.WHITE);
        int nameWidth = this.font.width(nick);
        int nameY = playerHeadY - 22;
        int nickColor = (a << 24) | 0xFFFFFF;
        g.fill(x - nameWidth / 2 - 2, nameY - 1, x + nameWidth / 2 + 2, nameY + 9, (a / 2) << 24);
        g.drawString(this.font, nick, x - nameWidth / 2, nameY, nickColor, false);
    }

    private void renderPage3Content(GuiGraphics g, float elapsed) {
        if (elapsed >= P3_BLACKOUT_FULL) {
            return;
        }

        int cx = this.width / 2;
        int cy = this.height / 2;

        String target = "Personalização Realizada!";
        String subTarget = "Que Chrona te guie!";

        float revealProgress = Math.min(1f, Math.max(0f, (elapsed - P3_REVEAL_START) / (P3_REVEAL_END - P3_REVEAL_START)));
        if (revealProgress < 1f) rng.setSeed(System.currentTimeMillis() / 50L);
        String shown = ZalgoText.reveal(target, revealProgress, rng);

        int realCount = (int) (revealProgress * target.length());
        if (realCount > lastTypewriterCount && realCount <= target.length()) {
            float pitch = 0.9f + (float) Math.random() * 0.5f;
            playSound(SoundEvents.NOTE_BLOCK_HAT.value(), 0.35f, pitch);
            lastTypewriterCount = realCount;
        }

        float titleAlpha = Math.min(1f, elapsed / 0.3f);

        if (titleAlpha > 0.01f) {
            float settleScale = 1f + (1f - Math.min(1f, elapsed / 0.4f)) * 1.5f;

            PoseStack pose = g.pose();
            pose.pushPose();
            pose.translate(cx, cy - 20, 0);
            pose.scale(2.0f * settleScale, 2.0f * settleScale, 1f);

            int color = applyAlpha(0xFFFFAA00, titleAlpha);
            int shadow = applyAlpha(0xFF000000, titleAlpha);
            int textWidth = this.font.width(shown);
            g.drawString(this.font, shown, -textWidth / 2 + 1, 1, shadow, false);
            g.drawString(this.font, shown, -textWidth / 2, 0, color, false);

            pose.popPose();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }

        if (elapsed > P3_SUBTITLE_START) {
            float subFade = Math.min(1f, (elapsed - P3_SUBTITLE_START) / 0.6f);
            float subZalgo = Math.max(0f, 1f - (elapsed - P3_SUBTITLE_START) / 1.5f);

            if (subFade > 0.01f) {
                rng.setSeed(System.currentTimeMillis() / 80L);
                String shownSub = ZalgoText.corrupt(subTarget, subZalgo, rng);

                PoseStack pose = g.pose();
                pose.pushPose();
                pose.translate(cx, cy + 20, 0);
                pose.scale(1.4f, 1.4f, 1f);

                int subColor = applyAlpha(0xFFCCCCCC, subFade);
                int subWidth = this.font.width(shownSub);
                g.drawString(this.font, shownSub, -subWidth / 2 + 1, 1, applyAlpha(0xFF000000, subFade), false);
                g.drawString(this.font, shownSub, -subWidth / 2, 0, subColor, false);

                pose.popPose();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }
    }

    private int applyAlpha(int argb, float alpha) {
        int a = (int) (((argb >>> 24) & 0xFF) * alpha) & 0xFF;
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private static class HeightSlider extends AbstractSliderButton {
        private final boolean locked;

        public HeightSlider(int x, int y, int w, int h, float currentHeight, boolean locked) {
            super(x, y, w, h, Component.empty(), locked ? 0.5 : clamp01(normalize(currentHeight)));
            this.locked = locked;
            updateMessage();
        }

        private static double clamp01(double v) {
            return Math.max(0.0, Math.min(1.0, v));
        }

        private static double normalize(float h) {
            return (h - HEIGHT_MIN) / (HEIGHT_MAX - HEIGHT_MIN);
        }

        private float currentHeight() {
            return HEIGHT_MIN + (float) value * (HEIGHT_MAX - HEIGHT_MIN);
        }

        @Override
        protected void updateMessage() {
            if (locked) {
                this.setMessage(Component.literal(String.format("Altura: %.2f bl", HEIGHT_INSETO)));
            } else {
                this.setMessage(Component.literal(String.format("Altura: %.2f bl", currentHeight())));
            }
        }

        @Override
        protected void applyValue() {
            if (locked) return;
            Screen current = Minecraft.getInstance().screen;
            if (current instanceof SeletorScreen screen) {
                screen.heightBlocks = screen.snapHeight(currentHeight());
            }
        }
    }

    private float snapHeight(float h) {
        return Math.round(h * 100f) / 100f;
    }
}