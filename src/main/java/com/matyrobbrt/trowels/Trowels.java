package com.matyrobbrt.trowels;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Trowels.MOD_ID)
public class Trowels {
    public static final String MOD_ID = "trowels";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> TROWEL = ITEMS.register("trowel", () -> new TrowelItem(new Item.Properties()
            .defaultDurability(Config.TROWEL_DURABILITY.getDefault())
            .tab(CreativeModeTab.TAB_TOOLS)
            .rarity(Rarity.UNCOMMON),
            Config.TROWEL_DURABILITY::get, Config.TROWEL_USES_DURABILITY::get));

    public Trowels() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, MOD_ID + "-common.toml");

        if (!FMLLoader.isProduction()) {
            modEventBus.register(TrowelsDatagen.class);
        }

        MinecraftForge.EVENT_BUS.addListener(TrowelItem::onDestroySpeed);
    }
}
