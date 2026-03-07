package com.abhinandan.bettergamba.client;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.registry.ModMenuTypes;
import com.abhinandan.bettergamba.screen.LotteryMachineScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = BetterGamba.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onRegisterMenuScreens(@NotNull RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.LOTTERY_MACHINE_MENU.get(), LotteryMachineScreen::new);
    }
}
