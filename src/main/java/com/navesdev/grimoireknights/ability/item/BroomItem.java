package com.navesdev.grimoireknights.ability.item;

import com.navesdev.grimoireknights.ability.entity.BroomEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** Placed on the ground like a boat, leaving the inventory; right click the placed broom to fly it (Mana Control III). */
public class BroomItem extends Item {
    public BroomItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) return InteractionResult.PASS;
        Level level = context.getLevel();
        BroomEntity broom = new BroomEntity(level, context.getClickLocation(), context.getRotation());
        if (!level.noCollision(broom, broom.getBoundingBox())) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            level.addFreshEntity(broom);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, context.getClickLocation());
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
