package com.navesdev.grimoireknights.quest.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.Objective;
import com.navesdev.grimoireknights.quest.Quest;
import com.navesdev.grimoireknights.quest.Reward;
import com.navesdev.grimoireknights.quest.network.DialoguePayload;
import com.navesdev.grimoireknights.quest.network.QuestAnswerPayload;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/** NPC dialogue (approved mockup): the NPC on the left, its line, and the offered quest's objective and reward with Accept and Decline. */
final class DialogueScreen extends Screen {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 120;
    private static final int PANEL = 0xF01E1D22;
    private static final int EDGE = 0xFF4A4652;
    private final DialoguePayload line;
    private final Optional<Quest> quest;
    private int left;
    private int top;

    DialogueScreen(DialoguePayload line) {
        super(Component.empty());
        this.line = line;
        this.quest = line.quest().flatMap(QuestClient::quest);
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = height - HEIGHT - 40;
        int y = top + HEIGHT - 26;
        if (line.quest().isPresent()) {
            addRenderableWidget(Button.builder(Component.translatable(Ids.key("screen", "dialogue.accept")), button -> answer(true))
                    .bounds(left + 74, y, 80, 20).build());
            addRenderableWidget(Button.builder(Component.translatable(Ids.key("screen", "dialogue.decline")), button -> answer(false))
                    .bounds(left + 160, y, 80, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable(Ids.key("screen", "dialogue.close")), button -> onClose())
                    .bounds(left + 74, y, 80, 20).build());
        }
    }

    private void answer(boolean accept) {
        line.quest().ifPresent(id -> PacketDistributor.sendToServer(new QuestAnswerPayload(id, accept)));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(left - 1, top - 1, left + WIDTH + 1, top + HEIGHT + 1, EDGE);
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, PANEL);
        Entity npc = minecraft.level == null ? null : minecraft.level.getEntity(line.npc());
        graphics.fill(left + 8, top + 8, left + 64, top + 78, EDGE);
        graphics.fill(left + 9, top + 9, left + 63, top + 77, 0xFF121116);
        if (npc instanceof LivingEntity living) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, left + 9, top + 9, left + 63, top + 77, 30, 0.0625f, mouseX, mouseY, living);
            graphics.drawString(font, living.getDisplayName(), left + 74, top + 8, 0xFFF3D989);
        }
        int y = top + 22;
        for (FormattedCharSequence text : font.split(line.text(), WIDTH - 84)) {
            graphics.drawString(font, text, left + 74, y, 0xFFDDDDDD);
            y += 10;
        }
        if (quest.isPresent()) {
            y += 4;
            for (Objective objective : quest.get().objectives()) {
                graphics.drawString(font, Component.translatable(Ids.key("screen", "dialogue.objective"), objective.describe()).withStyle(ChatFormatting.GRAY), left + 74, y, 0xFFBBBBBB);
                y += 10;
            }
            for (Reward reward : quest.get().rewards()) {
                graphics.drawString(font, Component.translatable(Ids.key("screen", "dialogue.reward"), reward.describe()).withStyle(ChatFormatting.GRAY), left + 74, y, 0xFFBBBBBB);
                y += 10;
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
