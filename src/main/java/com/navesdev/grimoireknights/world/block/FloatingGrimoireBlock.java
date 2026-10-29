package com.navesdev.grimoireknights.world.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Decoration of the Grimoire Tower: a grimoire floating among the shelves. */
public class FloatingGrimoireBlock extends BaseEntityBlock {
    public static final MapCodec<FloatingGrimoireBlock> CODEC = simpleCodec(FloatingGrimoireBlock::new);
    private static final VoxelShape SHAPE = Block.box(4, 2, 4, 12, 14, 12);

    public FloatingGrimoireBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FloatingGrimoireBlockEntity(pos, state);
    }
}
