package com.matyrobbrt.trowels;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Trowels.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TrowelsClient {
    @SubscribeEvent
    static void onRegisterColours(final RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex == 1) {
                return 0xE78E45;
            }
            return 0xffffff;
        }, Trowels.TROWEL.get());
    }
}
