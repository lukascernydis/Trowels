package com.matyrobbrt.trowels;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class TrowelPlaceContext extends BlockPlaceContext {
    private final int rolledSlot;
    public TrowelPlaceContext(UseOnContext context, ItemStack stack, int rolledIndex) {
        super(context.getLevel(), context.getPlayer(), context.getHand(), stack,
                new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));
        this.rolledSlot = rolledIndex;
    }

    public int getRolledSlot() {
        return rolledSlot;
    }
}
