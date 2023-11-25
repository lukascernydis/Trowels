package com.matyrobbrt.trowels.upgrade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public enum TrowelUpgrade {
    REFILL {
        @Override
        public void afterPlace(ItemStack trowel, UseOnContext context, int selectedSlot, ItemStack placedBlock) {
            //noinspection DataFlowIssue
            final Inventory inv = context.getPlayer().getInventory();
            while (placedBlock.getCount() < placedBlock.getMaxStackSize()) {
                final int matchingSlot = findNonHotbarSlotMatchingItem(inv, placedBlock);
                if (matchingSlot < 0) break;
                final ItemStack item = inv.items.get(matchingSlot);
                final int toRefill = Math.min(item.getCount(), placedBlock.getMaxStackSize() - placedBlock.getCount());
                item.shrink(toRefill);
                if (item.isEmpty()) inv.items.set(matchingSlot, ItemStack.EMPTY);
                placedBlock.grow(toRefill);
            }
        }

        private int findNonHotbarSlotMatchingItem(Inventory inv, ItemStack stack) {
            for (int i = Inventory.getSelectionSize(); i < inv.items.size(); i++) {
                if (!inv.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(stack, inv.items.get(i))) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public Component getName() {
            return Component.translatable("trowel_upgrade.refill").withStyle(ChatFormatting.GREEN);
        }

        @Override
        public Component getDescription() {
            return Component.translatable("trowel_upgrade.refill.desc");
        }
    },
    BREAK {
        @Override
        public Component getName() {
            return Component.translatable("trowel_upgrade.break").withStyle(ChatFormatting.GREEN);
        }

        @Override
        public Component getDescription() {
            return Component.translatable("trowel_upgrade.break.desc");
        }
    };

    public void afterPlace(ItemStack trowel, UseOnContext context, int selectedSlot, ItemStack placedBlock) {}

    public abstract Component getName();
    public abstract Component getDescription();
}
