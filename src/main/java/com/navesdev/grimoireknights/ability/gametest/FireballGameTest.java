package com.navesdev.grimoireknights.ability.gametest;

import com.navesdev.grimoireknights.GrimoireKnights;
import com.navesdev.grimoireknights.ability.AbilityModule;
import com.navesdev.grimoireknights.ability.config.AbilityConfig;
import com.navesdev.grimoireknights.ability.entity.FireballProjectile;
import com.navesdev.grimoireknights.core.gametest.TestPlayers;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GrimoireKnights.MODID)
@PrefixGameTestTemplate(false)
public class FireballGameTest {
    @GameTest(template = "empty")
    public static void explosionHurtsTheAreaLessThanTheDirectHit(GameTestHelper helper) {
        ServerPlayer caster = TestPlayers.create(helper);
        Pig hit = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 1.5));
        Pig near = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 3.5));
        Pig far = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(1.5, 1, 1.5 + AbilityConfig.FIREBALL_RADIUS.get() + 2));
        float health = hit.getMaxHealth();
        Vec3 center = hit.position().add(0, 0.5, 0);
        caster.setPos(center);

        FireballProjectile fireball = new FireballProjectile(helper.getLevel(), caster);
        fireball.setPos(center);
        fireball.explode(hit);

        helper.assertTrue(hit.getHealth() == health - AbilityConfig.FIREBALL_DAMAGE.get().floatValue(), "direct hit takes the full damage");
        helper.assertTrue(near.getHealth() == health - AbilityConfig.FIREBALL_AREA_DAMAGE.get().floatValue(), "area takes the area damage");
        helper.assertTrue(far.getHealth() == health, "outside the radius is untouched");
        helper.assertTrue(hit.hasData(AbilityModule.BURN) && near.hasData(AbilityModule.BURN), "direct hit and area both burn");
        helper.assertFalse(far.hasData(AbilityModule.BURN), "outside the radius does not burn");
        helper.assertTrue(fireball.isRemoved(), "the fireball is gone after exploding");
        helper.succeed();
    }
}
