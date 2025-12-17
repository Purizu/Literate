package com.butteredapples.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
public class NeoChiseledBookshelfBlock extends BaseEntityBlock {

    public static final List<EnumProperty<BookEnum>> BOOK_TYPE_PROPERTIES = List.of(
            ModProperties.BOOK_TYPE_SLOT_0,
            ModProperties.BOOK_TYPE_SLOT_1,
            ModProperties.BOOK_TYPE_SLOT_2,
            ModProperties.BOOK_TYPE_SLOT_3,
            ModProperties.BOOK_TYPE_SLOT_4,
            ModProperties.BOOK_TYPE_SLOT_5
    );

    public NeoChiseledBookshelfBlock(Properties properties) {
        super(properties);
        BlockState blockState = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        for (EnumProperty<BookEnum> property : BOOK_TYPE_PROPERTIES) {
            blockState = blockState.setValue(property, BookEnum.EMPTY);
        }
        this.registerDefaultState(blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
        BOOK_TYPE_PROPERTIES.forEach(builder::add);
    }

    @Override
    public @NotNull InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.getBlockEntity(blockPos) instanceof NeoChiseledBookshelfBlockEntity chiseledBookShelfBlockEntity) {
            Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(blockHitResult, blockState.getValue(HorizontalDirectionalBlock.FACING));
            if (optional.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                int i = getHitSlot(optional.get());
                if ((boolean) blockState.getValue((Property)BOOK_TYPE_PROPERTIES.get(i))) {
                    removeBook(level, blockPos, player, chiseledBookShelfBlockEntity, i);
                    BlockState newState = blockState.setValue(BOOK_TYPE_PROPERTIES.get(i), BookEnum.EMPTY);
                    level.setBlock(blockPos, newState, Block.UPDATE_ALL);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    ItemStack itemStack = player.getItemInHand(interactionHand);
                    boolean isEnchanted = itemStack.is(Items.ENCHANTED_BOOK);
                    if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
                        addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, i);
                        BlockState newState = blockState.setValue(BOOK_TYPE_PROPERTIES.get(i), isEnchanted ? BookEnum.ENCHANTED : BookEnum.NORMAL);
                        level.setBlock(blockPos, newState, Block.UPDATE_ALL);
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    } else {
                        return InteractionResult.CONSUME;
                    }
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockHitResult, Direction direction) {
        Direction direction2 = blockHitResult.getDirection();
        if (direction != direction2) {
            return Optional.empty();
        } else {
            BlockPos blockPos = blockHitResult.getBlockPos().relative(direction2);
            Vec3 vec3 = blockHitResult.getLocation().subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            double d = vec3.x();
            double e = vec3.y();
            double f = vec3.z();

            return switch (direction2) {
                case NORTH -> Optional.of(new Vec2((float)(1.0 - d), (float)e));
                case SOUTH -> Optional.of(new Vec2((float)d, (float)e));
                case WEST -> Optional.of(new Vec2((float)f, (float)e));
                case EAST -> Optional.of(new Vec2((float)(1.0 - f), (float)e));
                case DOWN, UP -> Optional.empty();
            };
        }
    }

    private static int getHitSlot(Vec2 vec2) {
        int i = vec2.y >= 0.5F ? 0 : 1;
        int j = getSection(vec2.x);
        return j + i * 3;
    }

    private static int getSection(float f) {
        float g = 0.0625F;
        float h = 0.375F;
        if (f < 0.375F) {
            return 0;
        } else {
            float i = 0.6875F;
            return f < 0.6875F ? 1 : 2;
        }
    }

    private static void addBook(
            Level level, BlockPos blockPos, Player player, NeoChiseledBookshelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int i
    ) {
        if (!level.isClientSide) {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
            chiseledBookShelfBlockEntity.setItem(i, itemStack.split(1));
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player.isCreative()) {
                itemStack.grow(1);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }

    private static void removeBook(Level level, BlockPos blockPos, Player player, NeoChiseledBookshelfBlockEntity chiseledBookShelfBlockEntity, int i) {
        if (!level.isClientSide) {
            ItemStack itemStack = chiseledBookShelfBlockEntity.removeItem(i, 1);
            SoundEvent soundEvent = itemStack.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }

            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState.is(blockState2.getBlock())) {
            if (level.getBlockEntity(blockPos) instanceof NeoChiseledBookshelfBlockEntity chiseledBookShelfBlockEntity && !chiseledBookShelfBlockEntity.isEmpty()) {
                for (int i = 0; i < 6; i++) {
                    ItemStack itemStack = chiseledBookShelfBlockEntity.getItem(i);
                    if (!itemStack.isEmpty()) {
                        Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), itemStack);
                    }
                }

                chiseledBookShelfBlockEntity.clearContent();
                level.updateNeighbourForOutputSignal(blockPos, this);
            }

            super.onRemove(blockState, level, blockPos, blockState2, bl);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(blockState.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        if (level.isClientSide()) {
            return 0;
        } else {
            return level.getBlockEntity(blockPos) instanceof NeoChiseledBookshelfBlockEntity chiseledBookShelfBlockEntity
                    ? chiseledBookShelfBlockEntity.getLastInteractedSlot() + 1
                    : 0;
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new NeoChiseledBookshelfBlockEntity(blockPos, blockState);
    }
}


