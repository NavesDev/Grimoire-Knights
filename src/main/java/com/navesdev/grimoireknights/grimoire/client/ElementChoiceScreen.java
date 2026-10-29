package com.navesdev.grimoireknights.grimoire.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.grimoire.network.ChooseElementPayload;
import com.navesdev.grimoireknights.grimoire.network.ElementChoicePayload;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/** Free choice at the ceremony: one card per element (big icon and name). Closing it postpones the choice. */
class ElementChoiceScreen extends Screen {
    private static final int CARD_WIDTH = 72;
    private static final int CARD_HEIGHT = 84;
    private static final int GAP = 10;
    private static final int ICON = 48;

    private final List<ResourceLocation> elements;

    private ElementChoiceScreen(List<ResourceLocation> elements) {
        super(Component.translatable(Ids.key("screen", "element_choice.title")));
        this.elements = elements;
    }

    static void open(ElementChoicePayload payload) {
        Minecraft.getInstance().setScreen(new ElementChoiceScreen(payload.elements()));
    }

    @Override
    protected void init() {
        int perRow = Math.max(1, Math.min(elements.size(), (width - GAP) / (CARD_WIDTH + GAP)));
        int rows = (elements.size() + perRow - 1) / perRow;
        int top = (height - rows * (CARD_HEIGHT + GAP)) / 2 + 8;
        for (int i = 0; i < elements.size(); i++) {
            int inRow = Math.min(perRow, elements.size() - i / perRow * perRow);
            int left = (width - inRow * CARD_WIDTH - (inRow - 1) * GAP) / 2;
            addRenderableWidget(new Card(left + i % perRow * (CARD_WIDTH + GAP), top + i / perRow * (CARD_HEIGHT + GAP), elements.get(i)));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int top = children().isEmpty() ? height / 2 : ((Card) children().getFirst()).getY();
        graphics.drawCenteredString(font, title, width / 2, top - 28, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(Ids.key("screen", "element_choice.hint")), width / 2, top - 14, 0xBBBBBB);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class Card extends AbstractButton {
        private static final int BG = 0xFF2A2A2E;
        private static final int BG_HOVER = 0xFF3A2210;
        private static final int EDGE = 0xFF444444;
        private static final int EDGE_HOVER = 0xFFFFCF5A;

        private final ResourceLocation element;
        private final ResourceLocation icon;

        Card(int x, int y, ResourceLocation element) {
            super(x, y, CARD_WIDTH, CARD_HEIGHT, GrimoireService.elementName(element));
            this.element = element;
            this.icon = Ids.id("textures/gui/element/" + element.getPath() + ".png");
        }

        @Override
        public void onPress() {
            PacketDistributor.sendToServer(new ChooseElementPayload(element));
            onClose();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean lit = isHoveredOrFocused();
            graphics.fill(getX(), getY(), getRight(), getBottom(), lit ? EDGE_HOVER : EDGE);
            graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, lit ? BG_HOVER : BG);
            graphics.blit(icon, getX() + (CARD_WIDTH - ICON) / 2, getY() + 8, ICON, ICON, 0, 0, 16, 16, 16, 16);
            graphics.drawCenteredString(font, getMessage(), getX() + CARD_WIDTH / 2, getBottom() - 18, lit ? 0xFFB347 : 0xDDDDDD);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
