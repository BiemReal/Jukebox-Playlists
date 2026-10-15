package com.biem.jukeboxplaylists;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = JukeboxPlaylists.MODID)
public final class PlaylistNetwork {
    private PlaylistNetwork() {}

   public static final class ClientState {
        public static volatile int containerId = -1;
        public static volatile int lastSlot = -1;

        public static void clear() {
            containerId = -1;
            lastSlot = -1;
        }
    }

    public record PlaylistPosPayload(int containerId, int lastSlot) implements CustomPacketPayload {
        public static final Type<PlaylistPosPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(JukeboxPlaylists.MODID, "playlist_pos"));
        public static final StreamCodec<ByteBuf, PlaylistPosPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, PlaylistPosPayload::containerId,
                ByteBufCodecs.VAR_INT, PlaylistPosPayload::lastSlot,
                PlaylistPosPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(PlaylistPosPayload.TYPE, PlaylistPosPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    ClientState.containerId = payload.containerId();
                    ClientState.lastSlot = payload.lastSlot();
                }));
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getContainer() instanceof ChestMenu menu)) return;
        if (!(menu.getContainer() instanceof BlockEntity chest)) return;

        ServerLevel level = player.serverLevel(); // newer versions: (ServerLevel) player.level()
        BlockPos jukeboxPos = chest.getBlockPos().below();
        if (!PlaylistHandler.isPlaylistMode(level, jukeboxPos)) return;
        if (!(level.getBlockEntity(jukeboxPos) instanceof JukeboxBlockEntity jukebox)) return;

        PacketDistributor.sendToPlayer(player,
                new PlaylistPosPayload(menu.containerId, jukebox.getData(JukeboxPlaylists.LAST_SLOT)));
    }

    public static void syncViewers(ServerLevel level, BlockPos jukeboxPos, int lastSlot) {
        BlockPos chestPos = jukeboxPos.above();
        for (ServerPlayer player : level.players()) {
            if (player.containerMenu instanceof ChestMenu menu
                    && menu.getContainer() instanceof BlockEntity chest
                    && chest.getBlockPos().equals(chestPos)) {
                PacketDistributor.sendToPlayer(player, new PlaylistPosPayload(menu.containerId, lastSlot));
            }
        }
    }
}