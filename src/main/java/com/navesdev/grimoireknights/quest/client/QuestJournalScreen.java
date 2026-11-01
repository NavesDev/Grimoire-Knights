package com.navesdev.grimoireknights.quest.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.Objective;
import com.navesdev.grimoireknights.quest.Quest;
import com.navesdev.grimoireknights.quest.Reward;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

/**
 * Quest journal (J, approved mockup): a framed window in the skill tree's colors; active and completed quests on the
 * left, and the selected quest's card on the right with a bar per objective, the rewards and a chip to show it on screen.
 */
final class QuestJournalScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 190;
    private static final int LIST = 118;
    private static final int ROW = 14;
    private static final int TITLE = 22;
    private static final int WINDOW = 0xD8101010;
    private static final int WINDOW_EDGE = 0xFF3A3A3A;
    private static final int CARD = 0x70000000;
    private static final int CARD_EDGE = 0xFF2E2E2E;
    private static final int SELECTED = 0x50FFFFFF;
    private static final int GOLD = 0xFFE8C45A;
    private static final int GREEN = 0xFF5DCAA5;
    private static final int TEXT = 0xFFECE9E1;
    private static final int MUTED = 0xFF9A9A9A;
    private final List<ResourceLocation> active = new ArrayList<>();
    private final List<ResourceLocation> completed = new ArrayList<>();
    private Optional<ResourceLocation> selected = Optional.empty();
    private int left;
    private int top;
    /** Where the show-on-screen chip was last drawn; zero width when hidden. */
    private int chipX0;
    private int chipX1;
    private int chipY0;

    QuestJournalScreen() {
        super(Component.translatable(Ids.key("screen", "journal")));
    }

    @Override
    protected void init() {
        JournalHint.seen();
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        QuestData data = QuestClient.data();
        active.clear();
        completed.clear();
        data.active().keySet().stream().sorted().forEach(active::add);
        data.completed().stream().sorted().forEach(completed::add);
        if (selected.isEmpty()) selected = QuestClient.tracked().or(() -> active.stream().findFirst()).or(() -> completed.stream().findFirst());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        box(graphics, left, top, left + WIDTH, top + HEIGHT, WINDOW, WINDOW_EDGE);
        graphics.drawString(font, title, left + 8, top + 8, GOLD, false);
        Component count = Component.translatable(Ids.key("screen", "journal.count"), active.size(), completed.size());
        graphics.drawString(font, count, left + WIDTH - 8 - font.width(count), top + 8, MUTED, false);
        graphics.fill(left + 8, top + TITLE - 3, left + WIDTH - 8, top + TITLE - 2, WINDOW_EDGE);
        chipX1 = chipX0;
        if (active.isEmpty() && completed.isEmpty()) {
            graphics.drawWordWrap(font, Component.translatable(Ids.key("screen", "journal.empty")), left + 8, top + TITLE + 6, WIDTH - 16, MUTED);
            return;
        }
        int y = top + TITLE + 4;
        graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.active")), left + 8, y, MUTED, false);
        y += ROW - 2;
        for (ResourceLocation id : active) y = row(graphics, id, y, false);
        y += 4;
        graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.completed")), left + 8, y, MUTED, false);
        y += ROW - 2;
        for (ResourceLocation id : completed) y = row(graphics, id, y, true);
        selected.ifPresent(id -> detail(graphics, id, mouseX, mouseY));
    }

    private int row(GuiGraphics graphics, ResourceLocation id, int y, boolean done) {
        boolean isSelected = selected.equals(Optional.of(id));
        if (isSelected) {
            graphics.fill(left + 6, y - 3, left + LIST - 4, y + ROW - 4, SELECTED);
            graphics.fill(left + 6, y - 3, left + 7, y + ROW - 4, GOLD);
        }
        String name = (done ? "✓ " : "") + Quest.title(id).getString();
        graphics.drawString(font, font.plainSubstrByWidth(name, LIST - 18), left + 11, y, done ? MUTED : isSelected ? TEXT : 0xFFC8C8C8, false);
        return y + ROW;
    }

    private void detail(GuiGraphics graphics, ResourceLocation id, int mouseX, int mouseY) {
        int x0 = left + LIST;
        int x1 = left + WIDTH - 8;
        int wrap = x1 - x0 - 16;
        box(graphics, x0, top + TITLE + 2, x1, top + HEIGHT - 8, CARD, CARD_EDGE);
        int x = x0 + 8;
        int y = top + TITLE + 9;
        graphics.drawString(font, font.plainSubstrByWidth(Quest.title(id).getString(), wrap), x, y, GOLD, false);
        y += ROW + 2;
        Optional<Quest> quest = QuestClient.quest(id);
        if (quest.isEmpty()) return;
        QuestData data = QuestClient.data();
        List<Integer> progress = data.active().getOrDefault(id, List.of());
        for (int i = 0; i < quest.get().objectives().size(); i++) {
            Objective objective = quest.get().objectives().get(i);
            int done = i < progress.size() ? progress.get(i) : objective.required();
            String tally = Math.min(done, objective.required()) + "/" + objective.required();
            graphics.drawString(font, font.plainSubstrByWidth(objective.describe().getString(), wrap - font.width(tally) - 6), x, y, TEXT, false);
            graphics.drawString(font, tally, x + wrap - font.width(tally), y, MUTED, false);
            y += 11;
            graphics.fill(x, y, x + wrap, y + 2, CARD_EDGE);
            graphics.fill(x, y, x + wrap * Math.min(done, objective.required()) / Math.max(1, objective.required()), y + 2,
                    done >= objective.required() ? GREEN : GOLD);
            y += 8;
        }
        y += 2;
        graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.rewards")), x, y, MUTED, false);
        y += ROW - 2;
        for (Reward reward : quest.get().rewards()) {
            graphics.drawString(font, font.plainSubstrByWidth("✦ " + reward.describe().getString(), wrap), x, y, TEXT, false);
            y += ROW - 3;
        }
        int bottom = top + HEIGHT - 26;
        if (data.completed().contains(id)) {
            graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.done")), x, bottom + 4, GREEN, false);
        } else if (active.contains(id)) {
            Component chip = Component.translatable(Ids.key("screen", QuestClient.isTracked(id) ? "journal.untrack" : "journal.track"));
            chipX1 = x1 - 8;
            chipX0 = chipX1 - font.width(chip) - 10;
            chipY0 = bottom;
            boolean hover = mouseX >= chipX0 && mouseX < chipX1 && mouseY >= chipY0 && mouseY < chipY0 + 14;
            box(graphics, chipX0, chipY0, chipX1, chipY0 + 14, hover ? SELECTED : CARD, QuestClient.isTracked(id) ? GREEN : GOLD);
            graphics.drawString(font, chip, chipX0 + 5, chipY0 + 3, QuestClient.isTracked(id) ? GREEN : GOLD, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (chipX1 > chipX0 && mouseX >= chipX0 && mouseX < chipX1 && mouseY >= chipY0 && mouseY < chipY0 + 14) {
            selected.ifPresent(QuestClient::toggle);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            return true;
        }
        if (mouseX >= left + 6 && mouseX < left + LIST - 4) {
            int y = top + TITLE + 4 + ROW - 2;
            for (ResourceLocation id : active) {
                if (mouseY >= y - 3 && mouseY < y + ROW - 3) return select(id);
                y += ROW;
            }
            y += 4 + ROW - 2;
            for (ResourceLocation id : completed) {
                if (mouseY >= y - 3 && mouseY < y + ROW - 3) return select(id);
                y += ROW;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean select(ResourceLocation id) {
        selected = Optional.of(id);
        return true;
    }

    /** A panel with a 1px edge and its corners cut, like the skill tree's. */
    private static void box(GuiGraphics graphics, int x0, int y0, int x1, int y1, int fill, int edge) {
        graphics.fill(x0 + 1, y0, x1 - 1, y1, edge);
        graphics.fill(x0, y0 + 1, x1, y1 - 1, edge);
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, fill);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (QuestClientModule.JOURNAL.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
