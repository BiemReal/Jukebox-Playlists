package com.biem.jukeboxplaylists;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = JukeboxPlaylists.MODID)
public final class ModPacks {
    private ModPacks() {}

    @SubscribeEvent
    public static void addPacks(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(JukeboxPlaylists.MODID, "resourcepacks/old_jukebox"),
                PackType.CLIENT_RESOURCES,
                Component.literal("Old Jukebox"),
                PackSource.BUILT_IN,
                false,               // false = off by default, the player enables it in Resource Packs
                Pack.Position.TOP);
    }
}