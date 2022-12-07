package com.matyrobbrt.trowels.upgrade;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class UpgradeItem extends Item {
    private final TrowelUpgrade upgrade;
    public UpgradeItem(Properties props, TrowelUpgrade upgrade) {
        super(props);
        this.upgrade = upgrade;
    }

    public TrowelUpgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> comps, TooltipFlag flag) {
        comps.add(upgrade.getDescription());
    }

    @Override
    public Component getDescription() {
        return upgrade.getName();
    }
}
