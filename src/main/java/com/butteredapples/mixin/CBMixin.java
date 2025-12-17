package com.butteredapples.mixin;

import com.butteredapples.util.BookEnum;
import com.butteredapples.util.ModProperties;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
@Mixin(ChiseledBookShelfBlock.class)
public abstract class CBMixin extends Block {

    @Shadow
    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult blockHitResult, Direction direction) {
        return null;
    }

    @Shadow
    private static int getHitSlot(Vec2 vec2) {
        return 0;
    }

    @Shadow
    private static void removeBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, int i) {
    }

    @Shadow
    private static void addBook(Level level, BlockPos blockPos, Player player, ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity, ItemStack itemStack, int i) {
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"))
    private boolean dontAddVanillaPropertiesToDefaultState(boolean original) {
        return false;
    }

    @ModifyVariable(method = "<init>", at = @At("STORE"))
    private BlockState addCustomPropertiesToDefaultState(BlockState state) {
        return state;
    }

    @WrapWithCondition(method = "createBlockStateDefinition", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private boolean dontAddVanillaProperties(List<?> instance, Consumer<?> consumer) {
        return false;
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void addCustomProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        ModProperties.BOOK_TYPE_PROPERTIES.forEach(builder::add);
    }

    public CBMixin(Properties properties) {
        super(properties);
    }


    /**
     *
     * @author ButteredApples
     * @reason Because the mod no longer uses vanilla slot properties anymore and uses an enum system instead,
     *      The entire method needs to be Overridden to fix up all this.
     */
    @Overwrite
    public @NotNull InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.getBlockEntity(blockPos) instanceof ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity) {
            Optional<Vec2> optional = getRelativeHitCoordinatesForBlockFace(blockHitResult, blockState.getValue(HorizontalDirectionalBlock.FACING));
            if (optional.isEmpty()) {
                return InteractionResult.PASS;
            } else {
                int i = getHitSlot(optional.get());
                BookEnum bookType = blockState.getValue(ModProperties.BOOK_TYPE_PROPERTIES.get(i));
                if (bookType != BookEnum.EMPTY) {
                    removeBook(level, blockPos, player, chiseledBookShelfBlockEntity, i);
                    BlockState newState = blockState.setValue(
                            ModProperties.BOOK_TYPE_PROPERTIES.get(i),
                            BookEnum.EMPTY
                    );
                    level.setBlock(blockPos, newState, Block.UPDATE_ALL);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    ItemStack itemStack = player.getItemInHand(interactionHand);
                    boolean isEnchanted = itemStack.is(Items.ENCHANTED_BOOK);
                    boolean isWriteable = itemStack.is(Items.WRITABLE_BOOK);
                    boolean isWritten = itemStack.is(Items.WRITTEN_BOOK);
                    if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
                        addBook(level, blockPos, player, chiseledBookShelfBlockEntity, itemStack, i);
                        BlockState newState = blockState.setValue(
                                ModProperties.BOOK_TYPE_PROPERTIES.get(i), isEnchanted ?
                                        BookEnum.ENCHANTED : isWriteable ?
                                        BookEnum.WRITABLE : isWritten ?
                                        BookEnum.WRITTEN : BookEnum.NORMAL
                        );
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

}