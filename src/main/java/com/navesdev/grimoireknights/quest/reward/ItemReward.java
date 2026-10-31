package com.navesdev.grimoireknights.quest.reward;

import com.mojang.serialization.MapCodec;
import com.navesdev.grimoireknights.core.Ids;
import com.navesdev.grimoireknights.quest.QuestModule;
import com.navesdev.grimoireknights.quest.Reward;
import com.navesdev.grimoireknights.quest.RewardType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** {@code item}: gives an item stack; a full inventory drops it at the player's feet. */
public record ItemReward(ItemStack stack) implements Reward {
    public static final MapCodec<ItemReward> CODEC = ItemStack.CODEC.fieldOf("item").xmap(ItemReward::new, ItemReward::stack);

    @Override
    public RewardType<?> type() {
        return QuestModule.ITEM.get();
    }

    @Override
    public void grant(ServerPlayer player) {
        ItemStack copy = stack.copy();
        if (!player.getInventory().add(copy)) player.drop(copy, false);
    }

    @Override
    public Component describe() {
        return Component.translatable(Ids.key("reward", "item"), stack.getCount(), stack.getHoverName());
    }
}
