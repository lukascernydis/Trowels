package com.matyrobbrt.trowels;

import com.matyrobbrt.trowels.upgrade.TrowelUpgrade;
import com.matyrobbrt.trowels.util.DelegatedCollection;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

// TODO - breaking with the trowel should be an upgrade
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

    public static boolean acceptsUpgrades() {
        return true;
    }

    public static boolean acceptsUpgrade(ItemStack stack, TrowelUpgrade upgrade) {
        return acceptsUpgrades();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        stack.hurtAndBreak(1, entity, p -> p.broadcastBreakEvent(entity.getUsedItemHand()));
        //stack.getOrCreateTag().getList("PlacedBlockPos", Tag.TAG_COMPOUND).remove(serializePos(pos, level));
        return false;
    }

    static void onHit(final PlayerInteractEvent.LeftClickBlock e) {
        final Level level = e.getLevel();
        if (level.isClientSide)
            return;

        final Player player = e.getEntity();
        final ItemStack useItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (player.isCreative() || !player.isShiftKeyDown())
            return;
        if (!(useItem.getItem() instanceof TrowelItem))
            return;

        final BlockPos hitPos = e.getPos();
        final BlockState block = level.getBlockState(hitPos);
        final CompoundTag nbtPos = serializePos(hitPos, level);
        final CompoundTag tag = useItem.getOrCreateTag();

        if (!getUpgrades(useItem).contains(TrowelUpgrade.BREAK))
            return;

        ListTag clickedPoses = useItem.getOrCreateTag().getList("PlacedBlockPos", Tag.TAG_COMPOUND);
        Iterator<Tag> clickedPosesIterator = clickedPoses.iterator();
        while (clickedPosesIterator.hasNext()) {
            Tag pos = clickedPosesIterator.next();
            if (pos.equals(nbtPos)) {
                clickedPosesIterator.remove();
                ItemStack drop = new ItemStack(block.getBlock().asItem());
                if (!player.addItem(drop)) {
                    if (!drop.isEmpty())
                        player.drop(drop, false);
                }
                else
                    level.playSeededSound(null,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                            0.4f, 2.0f, 0);
                level.destroyBlock(hitPos, false);
                tag.put("PlacedBlockPos", clickedPoses);
                return;
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.FAIL;

        final IntList targets = new IntArrayList();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (isValidTarget(player.getInventory().getItem(i))) targets.add(i);
        }

        if (targets.isEmpty())
            return InteractionResult.PASS;

        final InteractionHand hand = context.getHand();
        final ItemStack trowel = player.getItemInHand(hand);
        final CompoundTag tag = trowel.getOrCreateTag();
        final Random rand = new Random(tag.getLong("Seed"));
        tag.putLong("Seed", rand.nextLong());

        final int roll = targets.getInt(rand.nextInt(targets.size()));
        final ItemStack target = player.getInventory().getItem(roll);
        final int count = target.getCount();
        final BlockPos placePos =
                context.getLevel().getBlockState(context.getClickedPos()).canBeReplaced()
                        ? context.getClickedPos()
                        : context.getClickedPos().relative(context.getClickedFace());


        final InteractionResult result = placeBlock(roll, target, context);

        if (result != InteractionResult.CONSUME)
            return result;

        if (player.getAbilities().instabuild)
            target.setCount(count);

        if (result.consumesAction() && consumesDurability.getAsBoolean()) {
            context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }

        Collection<TrowelUpgrade> upgrades = getUpgrades(trowel);
        upgrades.forEach(upgrade -> upgrade.afterPlace(trowel, context, roll, target));
        if (!upgrades.contains(TrowelUpgrade.BREAK))
            return result;

        final ListTag clickedPoses = tag.getList("PlacedBlockPos", Tag.TAG_COMPOUND);
        final CompoundTag posSer = serializePos(placePos, context.getLevel(), target.getItem().toString());
        if (!clickedPoses.contains(posSer))
            clickedPoses.add(posSer);

        if (clickedPoses.size() > 5)
            clickedPoses.remove(0);

        tag.put("PlacedBlockPos", clickedPoses);

        return result;
    }

    public static Collection<TrowelUpgrade> getUpgrades(ItemStack stack) {
        final CompoundTag tag = stack.getOrCreateTag();
        final ListTag upgradesTag = tag.contains("Upgrades") ? tag.getList("Upgrades", Tag.TAG_STRING) : Util.make(new ListTag(), it -> tag.put("Upgrades", it));
        final EnumSet<TrowelUpgrade> upgrades = EnumSet.noneOf(TrowelUpgrade.class);
        upgradesTag.forEach(it -> upgrades.add(TrowelUpgrade.valueOf(it.getAsString())));
        return new DelegatedCollection<>(upgrades) {
            @Override
            public boolean add(TrowelUpgrade trowelUpgrade) {
                if (!acceptsUpgrade(stack, trowelUpgrade)) return false;

                final boolean flag = super.add(trowelUpgrade);
                if (flag) {
                    upgradesTag.add(StringTag.valueOf(trowelUpgrade.name()));
                }
                return flag;
            }
        };
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("desc.trowels.trowel").withStyle(ChatFormatting.AQUA));

        final var upgrades = getUpgrades(stack);
        if (upgrades.isEmpty()) {
            components.add(Component.translatable("desc.trowels.trowel.upgrades")
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        if (upgrades.contains(TrowelUpgrade.BREAK))
            components.add(Component.translatable("desc.trowels.trowel.destroy_recent")
                    .withStyle(ChatFormatting.GOLD));

        components.add(Component.literal(" "));
        components.add(Component.translatable("tooltip.trowels.upgrades"));
        upgrades.forEach(up -> components.add(up.getName()));
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
        return serializePos(pos, level, null);
    }

    private static CompoundTag serializePos(BlockPos pos, Level level, @Nullable String block) {
        // GET DEFAULT INSTANCE
        final CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("dimension", level.dimension().location().toString());
        tag.putString("block",
                Objects.requireNonNullElseGet(block, () -> level.getBlockState(pos).getBlock().asItem().toString()));
        return tag;
    }

}
