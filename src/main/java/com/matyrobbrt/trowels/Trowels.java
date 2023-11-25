package com.matyrobbrt.trowels;

import com.matyrobbrt.trowels.upgrade.TrowelUpgrade;
import com.matyrobbrt.trowels.upgrade.UpgradeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod(Trowels.MOD_ID)
public class Trowels {
    public static final String MOD_ID = "trowels";
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> TROWEL = ITEMS.register("trowel", () -> new TrowelItem(new Item.Properties()
            .defaultDurability(Config.TROWEL_DURABILITY.getDefault())
            .rarity(Rarity.UNCOMMON),
            Config.TROWEL_DURABILITY::get, Config.TROWEL_USES_DURABILITY::get));

    public static final RegistryObject<Item> REFILL_UPGRADE = ITEMS.register("refill_upgrade", () -> new UpgradeItem(new Item.Properties(), TrowelUpgrade.REFILL));
    public static final RegistryObject<Item> BREAK_UPGRADE = ITEMS.register("break_upgrade", () -> new UpgradeItem(new Item.Properties(), TrowelUpgrade.BREAK));

    public Trowels() {
        TABS.register(MOD_ID, () -> CreativeModeTab.builder()
                .title(Component.translatable("creative_tab.trowels"))
                .icon(() -> new ItemStack(TROWEL.get()))
                .displayItems((params, output) -> output
                        .acceptAll(List.of(
                                TROWEL.get().getDefaultInstance(),
                                REFILL_UPGRADE.get().getDefaultInstance(),
                                BREAK_UPGRADE.get().getDefaultInstance()
                        ))).build());

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TABS.register(modEventBus);
        ITEMS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, MOD_ID + "-common.toml");

        if (!FMLLoader.isProduction()) {
            modEventBus.register(TrowelsDatagen.class);
        }

        MinecraftForge.EVENT_BUS.addListener(TrowelItem::onHit);
        MinecraftForge.EVENT_BUS.addListener(this::handleAnvilUpdate);
    }

    private void handleAnvilUpdate(final AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof TrowelItem
                && TrowelItem.acceptsUpgrades()
                && event.getRight().getItem() instanceof UpgradeItem upgradeItem) {
            event.setCost(1);

            final var existingUpgrades = TrowelItem.getUpgrades(event.getLeft());
            if (existingUpgrades.contains(upgradeItem.getUpgrade())
                    || !TrowelItem.acceptsUpgrade(event.getLeft(), upgradeItem.getUpgrade())) {
                event.setCanceled(true);
                return;
            }

            final ItemStack output = event.getLeft().copy();
            TrowelItem.getUpgrades(output).add(upgradeItem.getUpgrade());
            event.setOutput(output);

            event.setCost(1 + existingUpgrades.size() * 3);
        }
    }
}
