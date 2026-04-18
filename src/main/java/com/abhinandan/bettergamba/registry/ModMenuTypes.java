package com.abhinandan.bettergamba.registry;

import com.abhinandan.bettergamba.BetterGamba;
import com.abhinandan.bettergamba.screen.menu.LotteryMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, BetterGamba.MOD_ID);

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }

    public static final DeferredHolder<MenuType<?>, MenuType<LotteryMachineMenu>> LOTTERY_MACHINE_MENU = MENU_TYPES.register("gamba_wheel", () -> IMenuTypeExtension.create(LotteryMachineMenu::new));


}
