package com.butteredapples.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Predicate;

public class NeoChiseledBookshelfBlockEntity extends BlockEntity implements Container {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private int lastInteractedSlot = -1;

    public NeoChiseledBookshelfBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(null, blockPos, blockState);
    }

    private void updateState(int i) {
        if (i >= 0 && i < 6) {
            this.lastInteractedSlot = i;
            BlockState blockState = this.getBlockState();

            for (int j = 0; j < NeoChiseledBookshelfBlock.BOOK_TYPE_PROPERTIES.size(); j++) {
                ItemStack stack = this.getItem(j);
                BookEnum type = stack.isEmpty() ? BookEnum.EMPTY : stack.is(Items.ENCHANTED_BOOK) ? BookEnum.ENCHANTED : BookEnum.NORMAL;
                blockState = blockState.setValue(NeoChiseledBookshelfBlock.BOOK_TYPE_PROPERTIES.get(j), type);
            }

            Objects.requireNonNull(this.level).setBlock(this.worldPosition, blockState, 3);
        } else {
            LOGGER.error("Expected slot 0-5, got {}", i);
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        this.items.clear();
        ContainerHelper.loadAllItems(compoundTag, this.items);
        this.lastInteractedSlot = compoundTag.getInt("last_interacted_slot");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        ContainerHelper.saveAllItems(compoundTag, this.items, true);
        compoundTag.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    public int count() {
        return (int)this.items.stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int getContainerSize() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        ItemStack itemStack = Objects.requireNonNullElse(this.items.get(i), ItemStack.EMPTY);
        this.items.set(i, ItemStack.EMPTY);
        if (!itemStack.isEmpty()) {
            this.updateState(i);
        }

        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return this.removeItem(i, 1);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (itemStack.is(ItemTags.BOOKSHELF_BOOKS)) {
            this.items.set(i, itemStack);
            this.updateState(i);
        }
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return container.hasAnyMatching(
                itemStack2 -> itemStack2.isEmpty()
                        ? true
                        : ItemStack.isSameItemSameTags(itemStack, itemStack2)
                        && itemStack2.getCount() + itemStack.getCount() <= Math.min(itemStack2.getMaxStackSize(), container.getMaxStackSize())
        );
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return itemStack.is(ItemTags.BOOKSHELF_BOOKS) && this.getItem(i).isEmpty();
    }

    public int getLastInteractedSlot() {
        return this.lastInteractedSlot;
    }
}
