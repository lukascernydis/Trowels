package com.matyrobbrt.trowels;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TrowelItem extends Item {
    private final IntSupplier durabilityAmount;
    private final BooleanSupplier consumesDurability;
    public TrowelItem(Properties properties, IntSupplier durabilityAmount, BooleanSupplier consumesDurability) {
        super(properties);
        this.durabilityAmount = durabilityAmount;
        this.consumesDurability = consumesDurability;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return canDestroyPlacedBlock();
    }

    public boolean canDestroyPlacedBlock() {
        return true;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return canDestroyPlacedBlock();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        stack.hurtAndBreak(1, entity, p -> p.broadcastBreakEvent(entity.getUsedItemHand()));
        stack.getOrCreateTag().getList("PlacedBlockPos", Tag.TAG_COMPOUND).remove(serializePos(pos, level));
        return false;
    }

    static void onDestroySpeed(final PlayerEvent.BreakSpeed event) {
        final ItemStack useItem = event.getEntity().getItemInHand(event.getEntity().getUsedItemHand());
        if (useItem.getItem() instanceof TrowelItem item && item.canDestroyPlacedBlock()) {
            if (!event.getEntity().isShiftKeyDown()) {
                event.setCanceled(true);
                return;
            }

            event.getPosition().ifPresentOrElse(pos -> {
                final ListTag clickedPoses = useItem.getOrCreateTag().getList("PlacedBlockPos", Tag.TAG_COMPOUND);
                final CompoundTag nbtPos = serializePos(pos, event.getEntity().getLevel());
                if (clickedPoses.contains(nbtPos)) {
                    event.setNewSpeed(18f);
                } else {
                    event.setCanceled(true);
                }
            }, () -> event.setCanceled(true));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        final InteractionHand hand = context.getHand();

        final IntList targets = new IntArrayList();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (isValidTarget(player.getInventory().getItem(i))) targets.add(i);
        }
        if (targets.isEmpty()) return InteractionResult.PASS;

        final ItemStack trowel = player.getItemInHand(hand);
        final CompoundTag tag = trowel.getOrCreateTag();
        final Random rand = new Random(tag.getLong("Seed"));
        tag.putLong("Seed", rand.nextLong());

        final int roll = targets.getInt(rand.nextInt(targets.size()));
        final ItemStack target = player.getInventory().getItem(roll);
        final int count = target.getCount();

        final InteractionResult result = placeBlock(roll, target, context);

        if (player.getAbilities().instabuild) target.setCount(count);

        if (result.consumesAction() && consumesDurability.getAsBoolean()) {
            context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }

        final BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        final ListTag clickedPoses = tag.getList("PlacedBlockPos", Tag.TAG_COMPOUND);
        if (clickedPoses.size() > 5) {
            clickedPoses.remove(0);
        }

        final CompoundTag posSer = serializePos(placePos, context.getLevel());
        if (!clickedPoses.contains(posSer)) clickedPoses.add(posSer);

        tag.put("PlacedBlockPos", clickedPoses);

        return result;
    }

    private InteractionResult placeBlock(int rolledSlot, ItemStack rolledStack, UseOnContext context) {
        final Player player = context.getPlayer();
        @SuppressWarnings("DataFlowIssue") final ItemStack toRestore = player.getItemInHand(context.getHand());
        InteractionResult res = rolledStack.useOn(new TrowelPlaceContext(context, rolledStack, rolledSlot));
        player.setItemInHand(context.getHand(), toRestore); // Restore the trowel, just in case useOn decides that the placed block should be in the main hand
        return res;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return consumesDurability.getAsBoolean() ? durabilityAmount.getAsInt() : 0;
    }

    @Override
    public boolean canBeDepleted() {
        return consumesDurability.getAsBoolean();
    }

    @Override
    public void appendHoverText(ItemStack stacks, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("desc.trowels.trowel").withStyle(ChatFormatting.AQUA));
        components.add(Component.translatable("desc.trowels.trowel.destroy_recent").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.SILK_TOUCH) return true;
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    private static boolean isValidTarget(ItemStack stack) {
        final Item item = stack.getItem();
        return !stack.isEmpty() && item instanceof BlockItem;
    }

    private static CompoundTag serializePos(BlockPos pos, Level level) {
        final CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("dimension", level.dimension().location().toString());
        return tag;
    }

}
