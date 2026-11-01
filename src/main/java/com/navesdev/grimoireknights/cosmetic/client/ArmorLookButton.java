package com.navesdev.grimoireknights.cosmetic.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.cosmetic.ArmorLook;
import com.navesdev.grimoireknights.cosmetic.network.ArmorLookPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/** Eye in the corner of the inventory's player preview: hides or shows the armor. Follows the screen when the recipe book moves it. */
public class ArmorLookButton extends AbstractButton {
    private static final int SIZE = 10;
    private static final ResourceLocation SHOWN = Ids.id("cosmetic/armor_shown");
    private static final ResourceLocation HIDDEN = Ids.id("cosmetic/armor_hidden");
    private final InventoryScreen screen;

    public ArmorLookButton(InventoryScreen screen) {
        super(0, 0, SIZE, SIZE, Component.empty());
        this.screen = screen;
    }

    private static boolean hidden() {
        return Minecraft.getInstance().player != null && ArmorLook.armorHidden(Minecraft.getInstance().player);
    }

    @Override
    public void onPress() {
        PacketDistributor.sendToServer(new ArmorLookPayload(!hidden()));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setPosition(screen.getGuiLeft() + 64, screen.getGuiTop() + 66);
        boolean hidden = hidden();
        setTooltip(Tooltip.create(Component.translatable(Ids.key("button", hidden ? "armor.show" : "armor.hide"))));
        graphics.blitSprite(hidden ? HIDDEN : SHOWN, getX(), getY(), SIZE, SIZE);
        if (isHoveredOrFocused()) graphics.fill(getX(), getY(), getX() + SIZE, getY() + SIZE, 0x30FFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
