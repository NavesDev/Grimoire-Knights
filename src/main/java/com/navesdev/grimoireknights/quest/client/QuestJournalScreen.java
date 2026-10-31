package com.navesdev.grimoireknights.quest.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.Quest;
import com.navesdev.grimoireknights.quest.Reward;
import com.navesdev.grimoireknights.quest.attachment.QuestData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Quest journal (J, approved mockup): active and completed quests on the left; objective, progress, rewards and an optional Track button on the right. */
final class QuestJournalScreen extends Screen {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;
    private static final int LIST = 120;
    private static final int ROW = 12;
    private static final int PANEL = 0xF01E1D22;
    private static final int EDGE = 0xFF4A4652;
    private final List<ResourceLocation> active = new ArrayList<>();
    private final List<ResourceLocation> completed = new ArrayList<>();
    private Optional<ResourceLocation> selected = Optional.empty();
    private Button track;
    private int left;
    private int top;

    QuestJournalScreen() {
        super(Component.translatable(Ids.key("screen", "journal")));
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        QuestData data = QuestClient.data();
        active.clear();
        completed.clear();
        data.active().keySet().stream().sorted().forEach(active::add);
        data.completed().stream().sorted().forEach(completed::add);
        if (selected.isEmpty()) selected = QuestClient.tracked().or(() -> active.stream().findFirst()).or(() -> completed.stream().findFirst());
        track = addRenderableWidget(Button.builder(Component.empty(), button -> {
            selected.ifPresent(QuestClient::toggle);
            updateTrack();
        }).bounds(left + LIST + 12, top + HEIGHT - 28, 110, 20).build());
        updateTrack();
    }

    private void updateTrack() {
        boolean isActive = selected.map(active::contains).orElse(false);
        track.visible = isActive;
        track.setMessage(Component.translatable(Ids.key("screen", selected.map(QuestClient::isTracked).orElse(false) ? "journal.untrack" : "journal.track")));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left - 1, top - 1, left + WIDTH + 1, top + HEIGHT + 1, EDGE);
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, PANEL);
        graphics.fill(left + LIST, top + 6, left + LIST + 1, top + HEIGHT - 6, EDGE);
        graphics.drawString(font, title, left + 8, top + 6, 0xFFECE9E1);
        if (active.isEmpty() && completed.isEmpty()) {
            graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.empty")), left + 8, top + 24, 0xFF999999);
        }
        int y = top + 22;
        graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.active")), left + 8, y, 0xFFF3D989);
        y += ROW;
        for (ResourceLocation id : active) y = row(graphics, id, y, false, mouseX, mouseY);
        y += 4;
        graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.completed")), left + 8, y, 0xFF999999);
        y += ROW;
        for (ResourceLocation id : completed) y = row(graphics, id, y, true, mouseX, mouseY);
        selected.ifPresent(id -> detail(graphics, id));
        // widgets drawn again above the panel
        track.render(graphics, mouseX, mouseY, partialTick);
    }

    private int row(GuiGraphics graphics, ResourceLocation id, int y, boolean done, int mouseX, int mouseY) {
        if (selected.equals(Optional.of(id))) graphics.fill(left + 4, y - 2, left + LIST - 4, y + ROW - 2, 0xFF333333);
        Component name = Quest.title(id);
        graphics.drawString(font, font.plainSubstrByWidth((done ? "✓ " : "▸ ") + name.getString(), LIST - 14), left + 8, y, done ? 0xFF999999 : 0xFFECE9E1);
        return y + ROW;
    }

    private void detail(GuiGraphics graphics, ResourceLocation id) {
        int x = left + LIST + 12;
        int y = top + 22;
        graphics.drawString(font, Quest.title(id), x, y, 0xFFF3D989);
        y += ROW + 2;
        Optional<Quest> quest = QuestClient.quest(id);
        if (quest.isEmpty()) return;
        QuestData data = QuestClient.data();
        List<Integer> progress = data.active().getOrDefault(id, List.of());
        for (Component line : QuestClient.objectives(quest.get(), progress)) {
            graphics.drawWordWrap(font, line, x, y, WIDTH - LIST - 20, 0xFFDDDDDD);
            y += font.wordWrapHeight(line, WIDTH - LIST - 20) + 2;
        }
        y += 6;
        graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.rewards")).withStyle(ChatFormatting.GRAY), x, y, 0xFFBBBBBB);
        y += ROW;
        for (Reward reward : quest.get().rewards()) {
            graphics.drawString(font, reward.describe(), x + 4, y, 0xFFBBBBBB);
            y += ROW;
        }
        if (data.completed().contains(id)) graphics.drawString(font, Component.translatable(Ids.key("screen", "journal.done")).withStyle(ChatFormatting.GREEN), x, top + HEIGHT - 22, 0xFF5DCAA5);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= left + 4 && mouseX < left + LIST - 4) {
            int y = top + 22 + ROW;
            for (ResourceLocation id : active) {
                if (mouseY >= y - 2 && mouseY < y + ROW - 2) return select(id);
                y += ROW;
            }
            y += 4 + ROW;
            for (ResourceLocation id : completed) {
                if (mouseY >= y - 2 && mouseY < y + ROW - 2) return select(id);
                y += ROW;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean select(ResourceLocation id) {
        selected = Optional.of(id);
        updateTrack();
        return true;
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
