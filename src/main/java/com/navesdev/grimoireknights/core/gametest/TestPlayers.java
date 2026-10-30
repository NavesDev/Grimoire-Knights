package com.navesdev.grimoireknights.core.gametest;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Fresh server player per test. A fake player, because the vanilla mock player has no negotiated connection
 * and other mods' payloads (Curios) throw on it. Fake players cannot be hurt.
 */
public final class TestPlayers {
    private TestPlayers() {}

    public static ServerPlayer create(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "gk-test"));
    }
}
