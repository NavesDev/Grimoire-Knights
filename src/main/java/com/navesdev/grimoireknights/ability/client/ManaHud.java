package com.navesdev.grimoireknights.ability.client;

import com.navesdev.grimoireknights.ability.Ability;
import com.navesdev.grimoireknights.ability.AbilityRegistries;
import com.navesdev.grimoireknights.ability.AbilityService;
import com.navesdev.grimoireknights.ability.attachment.CombatData;
import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.Element;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillEffects;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.client.ProgressionClientModule;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * The one combat bar (approved mockup): mana standing left of the hotbar, slots Z, X and C in a column beside it
 * (darkening while cooling down, a lock while the rank keeps them closed), the dash icon, Tailwind stacks and the broom's
 * mana per second (red while gliding) when they apply.
 */
final class ManaHud {
    private static final int SLOT = 20;
    private static final int GAP = 2;
    private static final int BAR_WIDTH = 6;
    private static final int COLUMN = 3 * SLOT + 2 * GAP;
    /** Left of the hotbar (half width 91) and of the offhand slot (29). */
    private static final int FROM_CENTER = 91 + 29 + 6 + SLOT;
    private static final int BORDER = 0xFF0B0E1C;
    private static final int EMPTY = 0xFF141A33;
    private static final int MANA = 0xFF4F7CFF;
    private static final int SLOT_BG = 0xFF3A2210;
    private static final int SLOT_EDGE = 0xFFC98B2D;
    private static final int LOCKED_BG = 0xFF26252B;
    private static final int LOCKED_EDGE = 0xFF666666;
    private static final int COOLDOWN = 0xB0000000;
    private static final int TAILWIND = 0xFF9FE1CB;
    private static final int TAILWIND_OFF = 0xFF3C4A5A;

    /** Set by the Tailwind payload. */
    static int tailwind;

    private ManaHud() {}

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.options.hideGui || player.isSpectator()) return;
        Optional<Element> element = GrimoireService.element(player);
        CombatData data = AbilityService.data(player);
        if (element.isEmpty() || !data.manaSet()) return;

        int x = graphics.guiWidth() / 2 - FROM_CENTER;
        int bottom = graphics.guiHeight() - 2;
        int columnTop = bottom - COLUMN;
        int barX = x - BAR_WIDTH - 3;

        double max = AbilityService.maxMana(player);
        int filled = max <= 0 ? 0 : (int) Math.round((COLUMN - 2) * Math.min(1, data.mana() / max));
        graphics.fill(barX, columnTop, barX + BAR_WIDTH, bottom, BORDER);
        graphics.fill(barX + 1, columnTop + 1, barX + BAR_WIDTH - 1, bottom - 1, EMPTY);
        graphics.fill(barX + 1, bottom - 1 - filled, barX + BAR_WIDTH - 1, bottom - 1, MANA);

        for (int slot = 0; slot < AbilityService.SLOTS; slot++) {
            slot(graphics, minecraft, player, element.get(), data, slot, x, columnTop + slot * (SLOT + GAP));
        }
        if (SkillTreeService.effectRank(player, SkillEffects.DASH) > 0) dash(graphics, barX - 15, bottom - 14);
        if (tailwind > 0) {
            for (int i = 0; i < 5; i++) graphics.fill(x + i * 4, columnTop - 5, x + i * 4 + 3, columnTop - 2, i < tailwind ? TAILWIND : TAILWIND_OFF);
        }
        if (player.getVehicle() instanceof BroomEntity broom) {
            String cost = String.format(Locale.ROOT, "-%.1f", BroomEntity.manaPerSecond(player));
            Component text = Component.translatable(Ids.key("hud", "broom"), cost);
            graphics.drawString(minecraft.font, text, barX, columnTop - 16, broom.gliding() ? 0xFFE24B4A : 0xFF85B7EB, true);
        }
    }

    private static void slot(GuiGraphics graphics, Minecraft minecraft, LocalPlayer player, Element element, CombatData data, int slot, int x, int y) {
        Optional<ResourceLocation> id = element.ability(slot);
        boolean unlocked = RankService.slotUnlocked(player, slot);
        graphics.fill(x, y, x + SLOT, y + SLOT, unlocked ? SLOT_EDGE : LOCKED_EDGE);
        graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, unlocked ? SLOT_BG : LOCKED_BG);
        if (id.isEmpty()) return;
        if (!unlocked) {
            lock(graphics, x + 6, y + 5);
            return;
        }
        graphics.blit(Ids.id("textures/gui/ability/" + id.get().getPath() + ".png"), x + 2, y + 2, 0, 0, 16, 16, 16, 16);
        Optional<Ability> ability = id.flatMap(AbilityRegistries.ABILITIES::getOptional);
        long left = data.cooldownLeft(id.get(), player.level().getGameTime());
        if (ability.isPresent() && left > 0 && ability.get().cooldownTicks() > 0) {
            int shade = (int) Math.ceil((SLOT - 2) * Math.min(1, (double) left / ability.get().cooldownTicks()));
            graphics.fill(x + 1, y + SLOT - 1 - shade, x + SLOT - 1, y + SLOT - 1, COOLDOWN);
        }
        String key = AbilityClientModule.ABILITIES[slot].getTranslatedKeyMessage().getString();
        graphics.drawString(minecraft.font, key.length() == 1 ? key : key.substring(0, 1), x + SLOT - 6, y + SLOT - 8, 0xFFFFFFFF, true);
    }

    /** Padlock drawn with fills: shackle and body. */
    private static void lock(GuiGraphics graphics, int x, int y) {
        int color = 0xFF9A9A9A;
        graphics.fill(x + 1, y, x + 7, y + 1, color);
        graphics.fill(x, y + 1, x + 1, y + 5, color);
        graphics.fill(x + 7, y + 1, x + 8, y + 5, color);
        graphics.fill(x - 1, y + 5, x + 9, y + 11, color);
        graphics.fill(x + 3, y + 7, x + 5, y + 9, LOCKED_BG);
    }

    private static void dash(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 14, y + 14, LOCKED_EDGE);
        graphics.fill(x + 1, y + 1, x + 13, y + 13, BORDER);
        graphics.pose().pushPose();
        graphics.pose().translate(x + 1, y + 1, 0);
        graphics.pose().scale(0.75f, 0.75f, 1);
        graphics.blit(Ids.id("textures/gui/ability/dash.png"), 0, 0, 0, 0, 16, 16, 16, 16);
        graphics.pose().popPose();
        float cooldown = ProgressionClientModule.dashCooldownFraction();
        if (cooldown > 0) graphics.fill(x + 1, y + 13 - (int) Math.ceil(12 * cooldown), x + 13, y + 13, COOLDOWN);
    }
}
