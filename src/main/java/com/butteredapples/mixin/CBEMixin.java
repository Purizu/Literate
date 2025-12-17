package com.butteredapples.mixin;

import com.butteredapples.util.ModProperties;
import com.butteredapples.util.BookEnum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ChiseledBookShelfBlockEntity.class)
public abstract class CBEMixin extends BlockEntity {

    @Shadow
    private int lastInteractedSlot;

    public CBEMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow
    public abstract ItemStack getItem(int i);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "updateState", at = @At("HEAD"), cancellable = true)
    private void updateShape(int i, CallbackInfo ci){
        ci.cancel();
        if (i >= 0 && i < 6) {
            this.lastInteractedSlot = i;
            BlockState blockState = this.getBlockState();

            for (int j = 0; j < ModProperties.BOOK_TYPE_PROPERTIES.size(); j++) {
                ItemStack stack = this.getItem(j);
                BookEnum type = stack.isEmpty() ?
                        BookEnum.EMPTY : stack.is(Items.ENCHANTED_BOOK) ?
                        BookEnum.ENCHANTED : stack.is(Items.WRITABLE_BOOK) ?
                        BookEnum.WRITABLE : stack.is(Items.WRITTEN_BOOK) ?
                        BookEnum.WRITTEN : BookEnum.NORMAL;
                blockState = blockState.setValue(ModProperties.BOOK_TYPE_PROPERTIES.get(j), type);
            }

            Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockState, 3);
        } else {
            LOGGER.error("Expected slot 0-5, got {}", i);
        }
    }

}
