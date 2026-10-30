package com.navesdev.grimoireknights.ability.client;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.AbilityRegistries;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.attachment.CombatData;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.Element;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import java.util.Optional;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/** The one combat bar: mana standing left of the hotbar, slot Z under it darkening while it cools down. */
final class ManaHud {
    private static final int SLOT = 20;
    private static final int BAR_WIDTH = 6;
    private static final int BAR_HEIGHT = 40;
    /** Left of the hotbar (half width 91) and of the offhand slot (29). */
    private static final int FROM_CENTER = 91 + 29 + 6 + SLOT;
    private static final int BORDER = 0xFF0B0E1C;
    private static final int EMPTY = 0xFF141A33;
    private static final int MANA = 0xFF4F7CFF;
    private static final int SLOT_BG = 0xFF3A2210;
    private static final int SLOT_EDGE = 0xFFC98B2D;
    private static final int COOLDOWN = 0xB0000000;

    private ManaHud() {}

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) return;
        Optional<Element> element = GrimoireService.element(player);
        CombatData data = AbilityService.data(player);
        if (element.isEmpty() || !data.manaSet()) return;

        int x = graphics.guiWidth() / 2 - FROM_CENTER;
        int slotY = graphics.guiHeight() - SLOT - 2;
        int barX = x + (SLOT - BAR_WIDTH) / 2;
        int barY = slotY - BAR_HEIGHT - 3;

        double max = AbilityService.maxMana(player);
        int filled = max <= 0 ? 0 : (int) Math.round((BAR_HEIGHT - 2) * Math.min(1, data.mana() / max));
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BORDER);
        graphics.fill(barX + 1, barY + 1, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT - 1, EMPTY);
        graphics.fill(barX + 1, barY + BAR_HEIGHT - 1 - filled, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT - 1, MANA);

        graphics.fill(x, slotY, x + SLOT, slotY + SLOT, SLOT_EDGE);
        graphics.fill(x + 1, slotY + 1, x + SLOT - 1, slotY + SLOT - 1, SLOT_BG);
        Optional<ResourceLocation> id = element.get().ability(0);
        if (id.isEmpty()) return;
        graphics.blit(Ids.id("textures/gui/ability/" + id.get().getPath() + ".png"), x + 2, slotY + 2, 0, 0, 16, 16, 16, 16);
        Optional<Ability> ability = id.flatMap(AbilityRegistries.ABILITIES::getOptional);
        long left = data.cooldownLeft(id.get(), player.level().getGameTime());
        if (ability.isPresent() && left > 0 && ability.get().cooldownTicks() > 0) {
            int shade = (int) Math.ceil((SLOT - 2) * Math.min(1, (double) left / ability.get().cooldownTicks()));
            graphics.fill(x + 1, slotY + SLOT - 1 - shade, x + SLOT - 1, slotY + SLOT - 1, COOLDOWN);
        }
        graphics.drawString(minecraft.font, "Z", x + SLOT - 6, slotY + SLOT - 8, 0xFFFFFFFF, true);
    }
}
