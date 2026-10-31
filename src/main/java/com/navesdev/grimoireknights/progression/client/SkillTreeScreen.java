package com.navesdev.grimoireknights.progression.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.Masteries;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillNode;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.attachment.SkillTreeData;
import com.navesdev.grimoireknights.progression.network.BuyNodePayload;
import com.navesdev.grimoireknights.progression.rules.RankRules;
import com.navesdev.grimoireknights.progression.rules.SkillTreeRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The skill tree as a graph (approved mockup): item icons in circles linked by their prerequisites, three branches side by side.
 * Hovering shows name, cost and description; clicking buys the next rank. Row 0 of the grimoire branch holds the element's spells.
 */
final class SkillTreeScreen extends Screen {
    private static final int CELL = 40;
    private static final int COLUMNS = 10;
    private static final int ROWS = 4;
    private static final int RADIUS = 13;
    private static final int PANEL = 0xF01E1D22;
    private static final int PANEL_EDGE = 0xFF4A4652;
    private static final int[] BOUGHT = {0xFF0F3B31, 0xFF5DCAA5};
    private static final int[] AVAILABLE = {0xFF3A3214, 0xFFE8C45A};
    private static final int[] LOCKED = {0xFF26252B, 0xFF666666};
    private static final int LINE_ON = 0xFF5DCAA5;
    private static final int LINE_OFF = 0xFF555555;

    private record Spot(ResourceLocation id, SkillNode node, int x, int y) {}

    private record Spell(ResourceLocation id, int slot, int x, int y) {}

    private final List<Spot> spots = new ArrayList<>();
    private final List<Spell> spells = new ArrayList<>();
    private int left;
    private int top;

    SkillTreeScreen() {
        super(Component.translatable(Ids.key("screen", "skill_tree")));
    }

    @Override
    protected void init() {
        left = (width - COLUMNS * CELL) / 2;
        top = (height - (ROWS + 1) * CELL) / 2 + 14;
        spots.clear();
        spells.clear();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        SkillTreeService.nodes(player).filter(entry -> SkillTreeService.active(player, entry.getValue()))
                .forEach(entry -> spots.add(new Spot(entry.getKey(), entry.getValue(), cx(entry.getValue().x()), cy(entry.getValue().y()))));
        GrimoireService.element(player).ifPresent(element -> {
            for (int slot = 0; slot < element.abilities().size(); slot++) spells.add(new Spell(element.abilities().get(slot), slot, cx(7 + slot), cy(0)));
        });
    }

    private int cx(int column) {
        return left + column * CELL + CELL / 2;
    }

    private int cy(int row) {
        return top + (ROWS - row) * CELL + CELL / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        SkillTreeData data = SkillTreeService.data(player);
        graphics.fill(left - 8, top - 30, left + COLUMNS * CELL + 8, top + (ROWS + 1) * CELL + 22, PANEL_EDGE);
        graphics.fill(left - 7, top - 29, left + COLUMNS * CELL + 7, top + (ROWS + 1) * CELL + 21, PANEL);
        graphics.drawString(font, Component.translatable(Ids.key("screen", "skill_tree.rank"), RankService.name(RankService.rank(player))), left, top - 22, 0xFFECE9E1);

        header(graphics, player, data, Masteries.BODY, 0, 2);
        header(graphics, player, data, MasteryService.resourceMastery(player), 3, 3);
        MasteryService.elementMastery(player).ifPresent(element -> header(graphics, player, data, element, 7, 3));

        for (Spot spot : spots) {
            for (ResourceLocation required : spot.node().requires()) {
                spots.stream().filter(other -> other.id().equals(required)).findFirst().ifPresent(other ->
                        line(graphics, other.x(), other.y(), spot.x(), spot.y(), data.rank(spot.id()) > 0 ? LINE_ON : LINE_OFF));
            }
            if (spot.node().effect().ability().isPresent() && spot.node().requires().isEmpty()) {
                spells.stream().filter(spell -> spot.node().effect().ability().get().equals(spell.id())).findFirst().ifPresent(spell ->
                        line(graphics, spell.x(), spell.y(), spot.x(), spot.y(), data.rank(spot.id()) > 0 ? LINE_ON : LINE_OFF));
            }
        }
        for (Spell spell : spells) {
            boolean unlocked = RankService.slotUnlocked(player, spell.slot());
            circle(graphics, spell.x(), spell.y(), unlocked ? BOUGHT : LOCKED);
            graphics.blit(Ids.id("textures/gui/ability/" + spell.id().getPath() + ".png"), spell.x() - 8, spell.y() - 8, 0, 0, 16, 16, 16, 16);
        }
        for (Spot spot : spots) {
            circle(graphics, spot.x(), spot.y(), state(player, data, spot));
            graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.getOptional(spot.node().icon()).orElse(Items.BARRIER)), spot.x() - 8, spot.y() - 8);
            if (spot.node().maxRank() > 1) {
                graphics.drawString(font, data.rank(spot.id()) + "/" + spot.node().maxRank(), spot.x() + 6, spot.y() + 8, 0xFFECE9E1, true);
            }
        }
        hovered(mouseX, mouseY).ifPresent(spot -> graphics.renderComponentTooltip(font, tooltip(player, data, spot), mouseX, mouseY));
        spells.stream().filter(spell -> near(spell.x(), spell.y(), mouseX, mouseY)).findFirst().ifPresent(spell ->
                graphics.renderComponentTooltip(font, spellTooltip(player, spell), mouseX, mouseY));
    }

    private void header(GuiGraphics graphics, LocalPlayer player, SkillTreeData data, ResourceLocation mastery, int column, int span) {
        Component text = Component.translatable(Ids.key("screen", "skill_tree.branch"), MasteryService.name(mastery),
                MasteryService.get(player, mastery).level(), data.points(mastery));
        graphics.drawCenteredString(font, text, left + column * CELL + span * CELL / 2, top - 8, 0xFFF3D989);
    }

    private int[] state(LocalPlayer player, SkillTreeData data, Spot spot) {
        if (SkillTreeService.check(player, spot.id()) == SkillTreeRules.Result.OK) return AVAILABLE;
        return data.rank(spot.id()) > 0 ? BOUGHT : LOCKED;
    }

    private List<Component> tooltip(LocalPlayer player, SkillTreeData data, Spot spot) {
        String key = "skill_node." + spot.id().getNamespace() + "." + spot.id().getPath().replace('/', '.');
        int rank = data.rank(spot.id());
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(key).withStyle(ChatFormatting.GOLD));
        if (spot.node().maxRank() > 1) lines.add(Component.translatable(Ids.key("screen", "skill_tree.rank_of"), rank, spot.node().maxRank()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(key + ".desc"));
        SkillTreeRules.Result result = SkillTreeService.check(player, spot.id());
        if (result == SkillTreeRules.Result.MAXED) {
            lines.add(Component.translatable(Ids.key("screen", "skill_tree.maxed")).withStyle(ChatFormatting.GREEN));
        } else {
            lines.add(Component.translatable(Ids.key("screen", "skill_tree.cost"), SkillTreeRules.nextCost(spot.node().rules(), rank)).withStyle(ChatFormatting.YELLOW));
            if (result == SkillTreeRules.Result.OK) lines.add(Component.translatable(Ids.key("screen", "skill_tree.click")).withStyle(ChatFormatting.GRAY));
            else if (result == SkillTreeRules.Result.RANK_TOO_LOW) lines.add(Component.translatable(Ids.key("screen", "skill_tree.needs_rank"),
                    RankService.name(spot.node().requiredRank())).withStyle(ChatFormatting.RED));
            else lines.add(Component.translatable(Ids.key("screen", "skill_tree." + result.name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private List<Component> spellTooltip(LocalPlayer player, Spell spell) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("ability." + spell.id().getNamespace() + "." + spell.id().getPath()).withStyle(ChatFormatting.GOLD));
        if (!RankService.slotUnlocked(player, spell.slot())) {
            lines.add(Component.translatable(Ids.key("screen", "skill_tree.needs_rank"), RankService.name(RankRules.slotRank(spell.slot()))).withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private Optional<Spot> hovered(double mouseX, double mouseY) {
        return spots.stream().filter(spot -> near(spot.x(), spot.y(), mouseX, mouseY)).findFirst();
    }

    private static boolean near(int x, int y, double mouseX, double mouseY) {
        return (mouseX - x) * (mouseX - x) + (mouseY - y) * (mouseY - y) <= RADIUS * RADIUS;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Optional<Spot> spot = hovered(mouseX, mouseY);
        if (button == 0 && spot.isPresent() && minecraft.player != null
                && SkillTreeService.check(minecraft.player, spot.get().id()) == SkillTreeRules.Result.OK) {
            PacketDistributor.sendToServer(new BuyNodePayload(spot.get().id()));
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ProgressionClientModule.TREE.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Filled disc with a 2px ring, drawn row by row. */
    private static void circle(GuiGraphics graphics, int x, int y, int[] colors) {
        disc(graphics, x, y, RADIUS, colors[1]);
        disc(graphics, x, y, RADIUS - 2, colors[0]);
    }

    private static void disc(GuiGraphics graphics, int x, int y, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            graphics.fill(x - dx, y + dy, x + dx + 1, y + dy + 1, color);
        }
    }

    private static void line(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= steps; i++) {
            int x = x0 + (x1 - x0) * i / Math.max(1, steps);
            int y = y0 + (y1 - y0) * i / Math.max(1, steps);
            graphics.fill(x - 1, y - 1, x + 1, y + 1, color);
        }
    }
}
