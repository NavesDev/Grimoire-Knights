package com.navesdev.grimoireknights.progression.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.Spells;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import com.navesdev.grimoireknights.grimoire.GrimoireService;
import com.navesdev.grimoireknights.progression.Masteries;
import com.navesdev.grimoireknights.progression.MasteryService;
import com.navesdev.grimoireknights.progression.ProgressionModule;
import com.navesdev.grimoireknights.progression.RankService;
import com.navesdev.grimoireknights.progression.rules.Rank;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class DamageGameTest {
    private static final ResourceLocation FIRE = Ids.id("fire");

    /** Iron golems: plenty of health, no armor. */
    private static float lost(GameTestHelper helper, java.util.function.Consumer<LivingEntity> hit) {
        LivingEntity golem = helper.spawn(EntityType.IRON_GOLEM, 1, 2, 1);
        float before = golem.getHealth();
        hit.accept(golem);
        return before - golem.getHealth();
    }

    @GameTest(template = "empty")
    public static void spellsGrowWithTheElementMastery(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, FIRE, GrimoireService.CEREMONY_LEAVES);
        float fresh = lost(helper, golem -> golem.hurt(Spells.source(helper.getLevel(), Spells.FIRE, player, player), 5));
        RankService.set(player, Rank.ELITE_KNIGHT);
        MasteryService.set(player, FIRE, 30);
        float strong = lost(helper, golem -> golem.hurt(Spells.source(helper.getLevel(), Spells.FIRE, player, player), 5));
        float punch = lost(helper, golem -> golem.hurt(player.damageSources().playerAttack(player), 5));
        helper.assertTrue(Math.abs(fresh - 5) < 1e-3 && Math.abs(strong - 10.8) < 1e-3, "x1 then x2.16, got " + fresh + " then " + strong);
        helper.assertTrue(Math.abs(punch - 5) < 1e-3, "a punch uses Body (level 1), got " + punch);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mobsHitWithTheirDataMapLevel(GameTestHelper helper) {
        LivingEntity blaze = helper.spawn(EntityType.BLAZE, 3, 2, 3);
        LivingEntity zombie = helper.spawn(EntityType.ZOMBIE, 3, 2, 3);
        float strong = lost(helper, golem -> golem.hurt(blaze.damageSources().mobAttack(blaze), 4));
        float plain = lost(helper, golem -> golem.hurt(zombie.damageSources().mobAttack(zombie), 4));
        helper.assertTrue(Math.abs(strong - 6.24) < 1e-3 && Math.abs(plain - 4) < 1e-3, "blaze x1.56, zombie x1, got " + strong + " and " + plain);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spellResistanceCutsOnlySpells(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.create(helper);
        GrimoireService.give(player, FIRE, GrimoireService.CEREMONY_LEAVES);
        float spell = lost(helper, golem -> {
            golem.getAttribute(ProgressionModule.SPELL_RESISTANCE).setBaseValue(0.32);
            golem.hurt(Spells.source(helper.getLevel(), Spells.FIRE, player, player), 10);
        });
        float punch = lost(helper, golem -> {
            golem.getAttribute(ProgressionModule.SPELL_RESISTANCE).setBaseValue(0.32);
            golem.hurt(player.damageSources().playerAttack(player), 10);
        });
        helper.assertTrue(Math.abs(spell - 6.8) < 1e-3 && Math.abs(punch - 10) < 1e-3, "32% off the spell only, got " + spell + " and " + punch);
        helper.succeed();
    }
}
