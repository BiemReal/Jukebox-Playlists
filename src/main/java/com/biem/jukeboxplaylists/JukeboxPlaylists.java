package com.biem.jukeboxplaylists;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(JukeboxPlaylists.MODID)
public class JukeboxPlaylists {
    public static final String MODID = "jukeboxplaylists";

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> LAST_SLOT =
            ATTACHMENTS.register("last_slot",
                    () -> AttachmentType.builder(() -> -1).serialize(Codec.INT).build());

    public static final TagKey<Block> PLAYLIST_CONTAINERS =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, "playlist_containers"));

    public JukeboxPlaylists(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        modBus.addListener(PlaylistNetwork::register);
    }
}