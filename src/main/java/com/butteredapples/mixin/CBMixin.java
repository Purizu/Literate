package com.butteredapples.mixin;

import com.butteredapples.ModProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(value = ChiseledBookShelfBlock.class)
public abstract class CBMixin extends Block {

    @Shadow
    @Final
    public static List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES;
    @Unique
    private static final List<BooleanProperty> ENCHANTED_SLOTS = List.of(
            ModProperties.ENCHANTED_SLOT_0_OCCUPIED,
            ModProperties.ENCHANTED_SLOT_1_OCCUPIED,
            ModProperties.ENCHANTED_SLOT_2_OCCUPIED,
            ModProperties.ENCHANTED_SLOT_3_OCCUPIED,
            ModProperties.ENCHANTED_SLOT_4_OCCUPIED,
            ModProperties.ENCHANTED_SLOT_5_OCCUPIED
    );

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"
            )
    )
    private void onRegisterDefaultState(ChiseledBookShelfBlock instance, BlockState state) {
        BlockState modifiedState = state;
        for (BooleanProperty prop : ENCHANTED_SLOTS) {
            modifiedState = modifiedState.setValue(prop, false);
        }
        this.registerDefaultState(modifiedState);
    }

    public CBMixin(Properties properties) {
        super(properties);
    }

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;addBook(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;Lnet/minecraft/world/item/ItemStack;I)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onBookAdded(BlockState state, Level level, BlockPos pos, Player player,
                             InteractionHand hand, BlockHitResult hit,
                             CallbackInfoReturnable<InteractionResult> cir,
                             ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity,
                             Optional<Vec2> optional, int slot, ItemStack itemStack) {

        if (level.isClientSide) return;
        boolean isEnchanted = itemStack.is(Items.ENCHANTED_BOOK);
        BlockState newState = state.setValue(ENCHANTED_SLOTS.get(slot), isEnchanted);
        newState = newState.setValue(SLOT_OCCUPIED_PROPERTIES.get(slot), true);
        level.setBlock(pos, newState, Block.UPDATE_ALL);
    }

    @Inject(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/ChiseledBookShelfBlock;removeBook(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/ChiseledBookShelfBlockEntity;I)V",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onBookRemoved(BlockState state, Level level, BlockPos pos, Player player,
                               InteractionHand hand, BlockHitResult hit,
                               CallbackInfoReturnable<InteractionResult> cir,
                               ChiseledBookShelfBlockEntity chiseledBookShelfBlockEntity,
                               Optional<Vec2> optional, int slot) {

        if (level.isClientSide) return;
        BlockState newState = state
                .setValue(ENCHANTED_SLOTS.get(slot), false)
                .setValue(SLOT_OCCUPIED_PROPERTIES.get(slot), false);

        level.setBlock(pos, newState, Block.UPDATE_ALL);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void addEnchantedProperties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        ENCHANTED_SLOTS.forEach(builder::add);
    }
}