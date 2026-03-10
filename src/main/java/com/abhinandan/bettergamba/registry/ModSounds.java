package com.abhinandan.bettergamba.registry;

import com.abhinandan.bettergamba.BetterGamba;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, BetterGamba.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIN_START = register("spin_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> REWARD_DROP = register("reward_drop");

    private static @NotNull DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(BetterGamba.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createFixedRangeEvent(resourceLocation, 16f));
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}
