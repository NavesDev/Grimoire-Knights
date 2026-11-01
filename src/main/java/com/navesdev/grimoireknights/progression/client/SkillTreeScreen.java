package com.navesdev.grimoireknights.progression.client;

import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.Masteries;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.SkillNode;
import com.navesdev.grimoireknights.progression.SkillTreeService;
import com.navesdev.grimoireknights.progression.attachment.MasteryData;
import com.navesdev.grimoireknights.progression.attachment.SkillTreeData;
import com.navesdev.grimoireknights.progression.network.BuyNodePayload;
import com.navesdev.grimoireknights.progression.rules.MasteryRules;
import com.navesdev.grimoireknights.progression.rules.RankRules;
import com.navesdev.grimoireknights.progression.rules.SkillTreeRules;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The skill tree as a graph (approved mockup, black theme): a translucent black window, not the whole screen, with one card per branch (Body,
 * Mana, the element) holding its level bar, points and nodes; own pixel icons in circles linked by their prerequisites.
 * Hovering shows name, cost and description; clicking buys the next rank. Row 0 of the grimoire branch holds the element's spells.
 */
final class SkillTreeScreen extends Screen {
    private static final int COLUMNS = 10;
    /** Grid rows 0 to ROWS. */
    private static final int ROWS = 3;
    private static final int PAD = 8;
    private static final int TITLE = 16;
    private static final int HEADER = 32;
    private static final int LEGEND = 14;
    private static final int WINDOW = 0xD8101010;
    private static final int WINDOW_EDGE = 0xFF3A3A3A;
    private static final int CARD = 0x70000000;
    private static final int CARD_EDGE = 0xFF2E2E2E;
    private static final int GOLD = 0xFFE8C45A;
    private static final int TEXT = 0xFFECE9E1;
    private static final int MUTED = 0xFF9A9A9A;
    private static final int[] BOUGHT = {0xFF0F3B31, 0xFF5DCAA5};
    private static final int[] AVAILABLE = {0xFF3A3214, 0xFFE8C45A};
    private static final int[] LOCKED = {0xFF1A1A1A, 0xFF4A4A4A};
    private static final int LINE_ON = 0xFF5DCAA5;
    private static final int LINE_OFF = 0xFF4A4A4A;
    private static final int BODY_BAR = 0xFFD85A30;
    private static final int MANA_BAR = 0xFF378ADD;
    private static final int FIRE_BAR = 0xFFF0701E;
    private static final int WIND_BAR = 0xFF5DCAA5;
    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};

    /** Each branch's card: first grid column and width in columns. The cards fill the width so the headers fit; nodes are
     *  centered in their card whatever columns their JSON gives, so new nodes need no layout tweak. */
    private record Card(int column, int span) {}

    private static final Map<SkillNode.Branch, Card> CARDS = Map.of(SkillNode.Branch.BODY, new Card(0, 3),
            SkillNode.Branch.MANA, new Card(3, 4), SkillNode.Branch.GRIMOIRE, new Card(7, 3));
    /** The element's spells sit in row 0 of the grimoire card, from this column. */
    private static final int SPELL_COLUMN = 7;

    private record Spot(ResourceLocation id, SkillNode node, int x, int y) {}

    private record Spell(ResourceLocation id, int slot, int x, int y) {}

    private final List<Spot> spots = new ArrayList<>();
    private final List<Spell> spells = new ArrayList<>();
    private int cell;
    private int radius;
    private int left;
    private int top;

    SkillTreeScreen() {
        super(Component.translatable(Ids.key("screen", "skill_tree")));
    }

    @Override
    protected void init() {
        TreeHint.seen();
        // about three quarters of the screen at most, so it never fills it
        int fit = Math.min((width * 3 / 4 - 2 * PAD) / COLUMNS, (height * 4 / 5 - TITLE - HEADER - LEGEND - 2 * PAD) / (ROWS + 1));
        cell = Math.max(26, Math.min(40, fit));
        radius = Math.max(10, cell * 9 / 25);
        left = (width - COLUMNS * cell) / 2;
        top = (height - (ROWS + 1) * cell - TITLE - HEADER - LEGEND) / 2 + TITLE + HEADER;
        spots.clear();
        spells.clear();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        List<Map.Entry<ResourceLocation, SkillNode>> nodes = SkillTreeService.nodes(player).filter(entry -> SkillTreeService.active(player, entry.getValue())).toList();
        List<ResourceLocation> abilities = GrimoireService.element(player).map(element -> element.abilities()).orElse(List.of());
        Map<SkillNode.Branch, int[]> used = new EnumMap<>(SkillNode.Branch.class);
        nodes.forEach(entry -> widen(used, entry.getValue().branch(), entry.getValue().x()));
        for (int slot = 0; slot < abilities.size(); slot++) widen(used, SkillNode.Branch.GRIMOIRE, SPELL_COLUMN + slot);
        Map<SkillNode.Branch, Integer> shift = new EnumMap<>(SkillNode.Branch.class);
        used.forEach((branch, range) -> {
            Card card = CARDS.get(branch);
            shift.put(branch, (int) Math.round((card.column() + card.span() / 2.0 - (range[0] + range[1] + 1) / 2.0) * cell));
        });
        nodes.forEach(entry -> spots.add(new Spot(entry.getKey(), entry.getValue(),
                cx(entry.getValue().x()) + shift.get(entry.getValue().branch()), cy(entry.getValue().y()))));
        for (int slot = 0; slot < abilities.size(); slot++) {
            spells.add(new Spell(abilities.get(slot), slot, cx(SPELL_COLUMN + slot) + shift.get(SkillNode.Branch.GRIMOIRE), cy(0)));
        }
    }

    /** Grows the branch's used column range {min, max} to hold {@code column}. */
    private static void widen(Map<SkillNode.Branch, int[]> used, SkillNode.Branch branch, int column) {
        int[] range = used.computeIfAbsent(branch, b -> new int[] {column, column});
        range[0] = Math.min(range[0], column);
        range[1] = Math.max(range[1], column);
    }

    private int cx(int column) {
        return left + column * cell + cell / 2;
    }

    private int cy(int row) {
        return top + (ROWS - row) * cell + cell / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        SkillTreeData data = SkillTreeService.data(player);
        int x0 = left - PAD;
        int y0 = top - HEADER - TITLE - PAD;
        int x1 = left + COLUMNS * cell + PAD;
        int y1 = top + (ROWS + 1) * cell + LEGEND + PAD;
        box(graphics, x0, y0, x1, y1, WINDOW, WINDOW_EDGE);
        graphics.drawString(font, title, left, y0 + PAD, GOLD, false);
        Component rank = Component.translatable(Ids.key("screen", "skill_tree.rank"), RankService.name(RankService.rank(player)));
        graphics.drawString(font, rank, x1 - PAD - font.width(rank), y0 + PAD, MUTED, false);
        graphics.fill(left, y0 + PAD + 11, x1 - PAD, y0 + PAD + 12, WINDOW_EDGE);

        branch(graphics, player, data, Masteries.BODY, CARDS.get(SkillNode.Branch.BODY), BODY_BAR);
        branch(graphics, player, data, MasteryService.resourceMastery(player), CARDS.get(SkillNode.Branch.MANA), MANA_BAR);
        MasteryService.elementMastery(player).ifPresent(element -> branch(graphics, player, data, element, CARDS.get(SkillNode.Branch.GRIMOIRE),
                element.getPath().equals("fire") ? FIRE_BAR : WIND_BAR));

        List<Spot> shown = spots.stream().filter(spot -> revealed(data, spot)).toList();
        for (Spot spot : shown) {
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
            icon(graphics, Ids.id("ability/" + spell.id().getPath()), spell.x(), spell.y());
        }
        for (Spot spot : shown) {
            circle(graphics, spot.x(), spot.y(), state(player, data, spot));
            icon(graphics, spot.node().icon(), spot.x(), spot.y());
            String badge = spot.node().maxRank() > 1 ? data.rank(spot.id()) + "/" + spot.node().maxRank() : upgradeNumeral(spot.id());
            if (!badge.isEmpty()) graphics.drawString(font, badge, spot.x() + radius - 4, spot.y() + radius - 6, TEXT, true);
        }
        legend(graphics, x1 - PAD, y1 - PAD - 8);
        hovered(data, mouseX, mouseY).ifPresent(spot -> graphics.renderComponentTooltip(font, tooltip(player, data, spot), mouseX, mouseY));
        spells.stream().filter(spell -> near(spell.x(), spell.y(), mouseX, mouseY)).findFirst().ifPresent(spell ->
                graphics.renderComponentTooltip(font, spellTooltip(player, spell), mouseX, mouseY));
    }

    /** A branch card: name and level on the first line; the bar toward the next level and the points to spend below it. */
    private void branch(GuiGraphics graphics, LocalPlayer player, SkillTreeData data, ResourceLocation mastery, Card card, int bar) {
        int x0 = left + card.column() * cell + 2;
        int x1 = left + (card.column() + card.span()) * cell - 2;
        int y0 = top - HEADER;
        box(graphics, x0, y0, x1, top + (ROWS + 1) * cell, CARD, CARD_EDGE);
        MasteryData.Entry entry = MasteryService.get(player, mastery);
        Component name = MasteryService.name(mastery);
        graphics.drawString(font, name, x0 + 6, y0 + 6, TEXT, false);
        graphics.drawString(font, Component.translatable(Ids.key("screen", "skill_tree.level"), entry.level()), x0 + 10 + font.width(name), y0 + 6, MUTED, false);
        int points = data.points(mastery);
        int barEnd = x1 - 6;
        if (points > 0) {
            String text = Component.translatable(Ids.key("screen", "skill_tree.points"), points).getString();
            int w = font.width(text) + 6;
            box(graphics, x1 - 5 - w, y0 + 16, x1 - 5, y0 + 27, AVAILABLE[0], AVAILABLE[0]);
            graphics.drawString(font, text, x1 - 2 - w, y0 + 18, GOLD, false);
            barEnd = x1 - 10 - w;
        }
        double share = entry.progress() / MasteryRules.required(entry.level(), MasteryService.curve());
        graphics.fill(x0 + 6, y0 + 21, barEnd, y0 + 23, CARD_EDGE);
        graphics.fill(x0 + 6, y0 + 21, x0 + 6 + (int) ((barEnd - x0 - 6) * Math.min(1, share)), y0 + 23, bar);
    }

    private void legend(GuiGraphics graphics, int right, int y) {
        int x = right;
        for (int i = 2; i >= 0; i--) {
            int[] colors = i == 0 ? BOUGHT : i == 1 ? AVAILABLE : LOCKED;
            Component text = Component.translatable(Ids.key("screen", "skill_tree.legend." + (i == 0 ? "bought" : i == 1 ? "available" : "locked")));
            x -= font.width(text);
            graphics.drawString(font, text, x, y, MUTED, false);
            x -= 9;
            disc(graphics, x + 3, y + 3, 3, colors[1]);
            x -= 10;
        }
    }

    /** Upgrade nodes end in their number ({@code fireball_2}): shown in roman numerals over the spell's icon. */
    private static String upgradeNumeral(ResourceLocation id) {
        String path = id.getPath();
        int number = Character.isDigit(path.charAt(path.length() - 1)) && path.charAt(path.length() - 2) == '_' ? path.charAt(path.length() - 1) - '0' : 0;
        return number > 0 && number < ROMAN.length ? ROMAN[number] : "";
    }

    /** {@code namespace:path} is the 16px texture {@code textures/gui/path.png}. */
    private static void icon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        graphics.blit(ResourceLocation.fromNamespaceAndPath(icon.getNamespace(), "textures/gui/" + icon.getPath() + ".png"), x - 8, y - 8, 0, 0, 16, 16, 16, 16);
    }

    /** A panel with a 1px edge and its corners cut, for a softer look. */
    private static void box(GuiGraphics graphics, int x0, int y0, int x1, int y1, int fill, int edge) {
        graphics.fill(x0 + 1, y0, x1 - 1, y1, edge);
        graphics.fill(x0, y0 + 1, x1, y1 - 1, edge);
        graphics.fill(x0 + 1, y0 + 1, x1 - 1, y1 - 1, fill);
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

    /** Positions come from every node, so the tree doesn't shift as it reveals itself. */
    private static boolean revealed(SkillTreeData data, Spot spot) {
        return SkillTreeRules.revealed(spot.node().rules(), data.rank(spot.id()), data.nodes());
    }

    private Optional<Spot> hovered(SkillTreeData data, double mouseX, double mouseY) {
        return spots.stream().filter(spot -> revealed(data, spot) && near(spot.x(), spot.y(), mouseX, mouseY)).findFirst();
    }

    private boolean near(int x, int y, double mouseX, double mouseY) {
        return (mouseX - x) * (mouseX - x) + (mouseY - y) * (mouseY - y) <= radius * radius;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (minecraft.player == null) return super.mouseClicked(mouseX, mouseY, button);
        Optional<Spot> spot = hovered(SkillTreeService.data(minecraft.player), mouseX, mouseY);
        if (button == 0 && spot.isPresent()
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
    private void circle(GuiGraphics graphics, int x, int y, int[] colors) {
        disc(graphics, x, y, radius, colors[1]);
        disc(graphics, x, y, radius - 2, colors[0]);
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
