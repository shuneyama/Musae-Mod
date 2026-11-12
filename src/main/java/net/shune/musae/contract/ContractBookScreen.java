package net.shune.musae.contract;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shune.musae.network.NetworkHandler;
import net.shune.musae.network.ContractSignPacket;
import net.shune.musae.network.ContractUpdatePagesPacket;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ContractBookScreen extends Screen {

    private static final ResourceLocation BOOK_LOCATION =
            ResourceLocation.parse("textures/gui/book.png");

    private static final int IMAGE_WIDTH = 192;
    private static final int IMAGE_HEIGHT = 192;
    private static final int TEXT_X_OFFSET = 36;
    private static final int TEXT_Y_OFFSET = 30;
    private static final int TEXT_WIDTH = 114;
    private static final int MAX_LINES = 14;
    private static final int MAX_PAGES = 20;
    private static final int MAX_PAGE_LENGTH = 256;
    private static final int LINE_HEIGHT = 9;

    private final List<String> pages;
    private final ContractBookItem.ContractState state;
    private final InteractionHand hand;
    private final boolean isAuthor;
    private final String authorName;
    private final String cosignerName;
    private final String authorTimestamp;
    private final String cosignerTimestamp;

    private int currentPage = 0;
    private List<List<FormattedCharSequence>> cachedPageLines = new ArrayList<>();

    private PageButton nextButton;
    private PageButton prevButton;
    private Button signButton;
    private Button addPageButton;
    private Button doneButton;

    private boolean isEditing = false;
    private String editBuffer = "";
    private int cursorPos = 0;
    private int cursorTick = 0;

    private ContractBookScreen(List<String> pages, ContractBookItem.ContractState state,
                               InteractionHand hand, boolean isAuthor,
                               String authorName, String cosignerName,
                               String authorTimestamp, String cosignerTimestamp) {
        super(GameNarrator.NO_TITLE);
        this.pages = new ArrayList<>(pages);
        this.state = state;
        this.hand = hand;
        this.isAuthor = isAuthor;
        this.authorName = authorName;
        this.cosignerName = cosignerName;
        this.authorTimestamp = authorTimestamp;
        this.cosignerTimestamp = cosignerTimestamp;

        if (this.pages.isEmpty() && state == ContractBookItem.ContractState.BLANK) {
            this.pages.add("");
        }

        if (state == ContractBookItem.ContractState.BLANK && isAuthor) {
            this.isEditing = true;
            this.editBuffer = this.pages.isEmpty() ? "" : this.pages.get(0);
            this.cursorPos = this.editBuffer.length();
        }
    }

    public static void open(List<String> pages, ContractBookItem.ContractState state,
                            InteractionHand hand, boolean isAuthor,
                            String authorName, String cosignerName,
                            String authorTimestamp, String cosignerTimestamp) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
                new ContractBookScreen(pages, state, hand, isAuthor,
                        authorName, cosignerName, authorTimestamp, cosignerTimestamp));
    }

    @Override
    protected void init() {
        int bookLeft = (this.width - IMAGE_WIDTH) / 2;
        int bookTop = 2;

        nextButton = addRenderableWidget(new PageButton(
                bookLeft + 116, bookTop + 159, true,
                btn -> changePage(1), true));

        prevButton = addRenderableWidget(new PageButton(
                bookLeft + 43, bookTop + 159, false,
                btn -> changePage(-1), true));

        int buttonY = bookTop + IMAGE_HEIGHT + 8;

        if (state == ContractBookItem.ContractState.BLANK && isAuthor) {
            signButton = addRenderableWidget(Button.builder(
                            Component.translatable("item.musae.contract_book.btn_sign"),
                            btn -> signAsAuthor())
                    .bounds(this.width / 2 - 100, buttonY, 98, 20)
                    .build());

            doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
                    .bounds(this.width / 2 + 2, buttonY, 98, 20)
                    .build());

            addPageButton = addRenderableWidget(Button.builder(
                            Component.literal("+"),
                            btn -> addNewPage())
                    .bounds(bookLeft + IMAGE_WIDTH + 4, bookTop + IMAGE_HEIGHT - 28, 20, 20)
                    .build());
        } else if (state == ContractBookItem.ContractState.PENDING && !isAuthor) {
            signButton = addRenderableWidget(Button.builder(
                            Component.translatable("item.musae.contract_book.btn_cosign"),
                            btn -> signAsCosigner())
                    .bounds(this.width / 2 - 100, buttonY, 98, 20)
                    .build());

            doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
                    .bounds(this.width / 2 + 2, buttonY, 98, 20)
                    .build());
        } else {
            doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> onClose())
                    .bounds(this.width / 2 - 49, buttonY, 98, 20)
                    .build());
        }

        updateButtons();
        rebuildCache();
    }

    private void updateButtons() {
        int totalPages = getTotalPages();
        prevButton.visible = currentPage > 0;
        nextButton.visible = currentPage < totalPages - 1;

        if (addPageButton != null) {
            addPageButton.visible = pages.size() < MAX_PAGES;
        }

        if (signButton != null && state == ContractBookItem.ContractState.BLANK) {
            signButton.active = !pages.isEmpty() && pages.stream().anyMatch(p -> !p.trim().isEmpty());
        }
    }

    private int getTotalPages() {
        int count = pages.size();
        if (state == ContractBookItem.ContractState.SIGNED) {
            count++;
        }
        return Math.max(count, 1);
    }

    private void changePage(int delta) {
        if (isEditing && currentPage < pages.size()) {
            pages.set(currentPage, editBuffer);
        }

        currentPage += delta;
        currentPage = Math.max(0, Math.min(currentPage, getTotalPages() - 1));

        if (isEditing && currentPage < pages.size()) {
            editBuffer = pages.get(currentPage);
            cursorPos = editBuffer.length();
        }

        rebuildCache();
        updateButtons();
    }

    private void addNewPage() {
        if (pages.size() >= MAX_PAGES) return;

        if (isEditing && currentPage < pages.size()) {
            pages.set(currentPage, editBuffer);
        }

        pages.add("");
        currentPage = pages.size() - 1;
        editBuffer = "";
        cursorPos = 0;

        rebuildCache();
        updateButtons();
    }

    private void signAsAuthor() {
        if (isEditing && currentPage < pages.size()) {
            pages.set(currentPage, editBuffer);
        }

        NetworkHandler.CHANNEL.sendToServer(new ContractUpdatePagesPacket(hand, pages));
        NetworkHandler.CHANNEL.sendToServer(new ContractSignPacket(hand, ContractSignPacket.SignAction.AUTHOR));
        onClose();
    }

    private void signAsCosigner() {
        NetworkHandler.CHANNEL.sendToServer(new ContractSignPacket(hand, ContractSignPacket.SignAction.COSIGNER));
        onClose();
    }

    @Override
    public void onClose() {
        if (state == ContractBookItem.ContractState.BLANK && isAuthor && isEditing) {
            if (currentPage < pages.size()) {
                pages.set(currentPage, editBuffer);
            }
            NetworkHandler.CHANNEL.sendToServer(new ContractUpdatePagesPacket(hand, pages));
        }
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        this.setFocused(null);
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        cursorTick++;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isEditing || currentPage >= pages.size()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == 259) {
            if (cursorPos > 0) {
                editBuffer = editBuffer.substring(0, cursorPos - 1) + editBuffer.substring(cursorPos);
                cursorPos--;
                rebuildCache();
            }
            return true;
        }

        if (keyCode == 257 || keyCode == 335) {
            if (editBuffer.length() < MAX_PAGE_LENGTH) {
                String candidate = editBuffer.substring(0, cursorPos) + "\n" + editBuffer.substring(cursorPos);
                List<FormattedCharSequence> testLines = this.font.split(
                        Component.literal(candidate).withStyle(Style.EMPTY), TEXT_WIDTH);
                if (testLines.size() <= MAX_LINES) {
                    editBuffer = candidate;
                    cursorPos++;
                    rebuildCache();
                }
            }
            return true;
        }

        if (keyCode == 263) {
            cursorPos = Math.max(0, cursorPos - 1);
            return true;
        }
        if (keyCode == 262) {
            cursorPos = Math.min(editBuffer.length(), cursorPos + 1);
            return true;
        }

        if (keyCode == 268) {
            cursorPos = 0;
            return true;
        }
        if (keyCode == 269) {
            cursorPos = editBuffer.length();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (!isEditing || currentPage >= pages.size()) {
            return super.charTyped(c, modifiers);
        }

        if (c == 167) return false;

        if (editBuffer.length() < MAX_PAGE_LENGTH) {
            String candidate = editBuffer.substring(0, cursorPos) + c + editBuffer.substring(cursorPos);
            List<FormattedCharSequence> testLines = this.font.split(
                    Component.literal(candidate).withStyle(Style.EMPTY), TEXT_WIDTH);
            if (testLines.size() <= MAX_LINES) {
                editBuffer = candidate;
                cursorPos++;
                rebuildCache();
                return true;
            }
        }

        return false;
    }

    private void rebuildCache() {
        cachedPageLines.clear();

        for (int i = 0; i < pages.size(); i++) {
            String text;
            if (isEditing && i == currentPage) {
                text = editBuffer;
            } else {
                text = pages.get(i);
            }
            List<FormattedCharSequence> lines = this.font.split(
                    Component.literal(text).withStyle(Style.EMPTY), TEXT_WIDTH);
            cachedPageLines.add(lines);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int bookLeft = (this.width - IMAGE_WIDTH) / 2;
        int bookTop = 2;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(BOOK_LOCATION, bookLeft, bookTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        int textX = bookLeft + TEXT_X_OFFSET;
        int textY = bookTop + TEXT_Y_OFFSET;

        int totalPages = getTotalPages();
        Component pageIndicator = Component.translatable("book.pageIndicator", currentPage + 1, totalPages);
        int pageIndWidth = this.font.width(pageIndicator);
        graphics.drawString(this.font, pageIndicator,
                bookLeft + (IMAGE_WIDTH - pageIndWidth) / 2 + 12,
                bookTop + 18, 0x000000, false);

        boolean isSignaturePage = state == ContractBookItem.ContractState.SIGNED && currentPage == pages.size();

        if (isSignaturePage) {
            renderSignaturePage(graphics, textX, textY);
        } else if (currentPage < cachedPageLines.size()) {
            List<FormattedCharSequence> lines = cachedPageLines.get(currentPage);
            for (int i = 0; i < lines.size() && i < MAX_LINES; i++) {
                graphics.drawString(this.font, lines.get(i), textX, textY + i * LINE_HEIGHT, 0x000000, false);
            }

            if (isEditing && currentPage < pages.size() && (cursorTick / 6) % 2 == 0) {
                renderCursor(graphics, textX, textY);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderSignaturePage(GuiGraphics graphics, int textX, int textY) {
        int y = textY;

        Component title = Component.translatable("item.musae.contract_book.signatures_title");
        graphics.drawString(this.font, title, textX, y, 0x5A3A1A, false);
        y += LINE_HEIGHT * 2;

        Component authorLabel = Component.translatable("item.musae.contract_book.sig_author");
        graphics.drawString(this.font, authorLabel, textX, y, 0x000000, false);
        y += LINE_HEIGHT;

        graphics.drawString(this.font, Component.literal("  " + authorName), textX, y, 0x333333, false);
        y += LINE_HEIGHT;

        if (!authorTimestamp.isEmpty()) {
            graphics.drawString(this.font, Component.literal("  " + authorTimestamp), textX, y, 0x666666, false);
        }
        y += LINE_HEIGHT * 2;

        Component cosignerLabel = Component.translatable("item.musae.contract_book.sig_cosigner");
        graphics.drawString(this.font, cosignerLabel, textX, y, 0x000000, false);
        y += LINE_HEIGHT;

        graphics.drawString(this.font, Component.literal("  " + cosignerName), textX, y, 0x333333, false);
        y += LINE_HEIGHT;

        if (!cosignerTimestamp.isEmpty()) {
            graphics.drawString(this.font, Component.literal("  " + cosignerTimestamp), textX, y, 0x666666, false);
        }
    }

    private void renderCursor(GuiGraphics graphics, int textX, int textY) {
        String beforeCursor = editBuffer.substring(0, cursorPos);
        List<FormattedCharSequence> lines = this.font.split(
                Component.literal(beforeCursor).withStyle(Style.EMPTY), TEXT_WIDTH);

        if (lines.isEmpty()) {
            graphics.fill(textX, textY, textX + 1, textY + LINE_HEIGHT, 0xFF000000);
            return;
        }

        int cursorLine = lines.size() - 1;
        FormattedCharSequence lastLine = lines.get(cursorLine);
        int cursorX = textX + this.font.width(lastLine);
        int cursorY = textY + cursorLine * LINE_HEIGHT;

        graphics.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFF000000);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}