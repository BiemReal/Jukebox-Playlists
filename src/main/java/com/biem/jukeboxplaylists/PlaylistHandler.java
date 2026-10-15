package com.biem.jukeboxplaylists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class PlaylistHandler {
    private PlaylistHandler() {}

    public static boolean isPlaylistMode(ServerLevel level, BlockPos jukeboxPos) {
        BlockState state = level.getBlockState(jukeboxPos.above());
        if (!state.is(JukeboxPlaylists.PLAYLIST_CONTAINERS)) return false;
        return !(state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE);
    }

    @Nullable
    private static IItemHandler findPlaylist(ServerLevel level, BlockPos jukeboxPos) {
        if (!isPlaylistMode(level, jukeboxPos)) return null;
        return level.getCapability(Capabilities.ItemHandler.BLOCK, jukeboxPos.above(), Direction.DOWN);
    }

    private static boolean isDisc(ServerLevel level, ItemStack stack) {
        return !stack.isEmpty() && JukeboxSong.fromStack(level.registryAccess(), stack).isPresent();
    }

    private static int findNextSlot(ServerLevel level, IItemHandler handler, int last) {
        int size = handler.getSlots();
        for (int offset = 1; offset <= size; offset++) {
            int slot = Math.floorMod(last + offset, size);
            if (isDisc(level, handler.getStackInSlot(slot))) return slot;
        }
        return -1;
    }

    private static void setLast(JukeboxBlockEntity jukebox, int slot) {
        if (jukebox.getData(JukeboxPlaylists.LAST_SLOT) != slot) {
            jukebox.setData(JukeboxPlaylists.LAST_SLOT, slot);
            jukebox.setChanged();
            if (jukebox.getLevel() instanceof ServerLevel sl) {
                PlaylistNetwork.syncViewers(sl, jukebox.getBlockPos(), slot);
            }
        }
    }

    public static boolean onSongFinished(LevelAccessor accessor, BlockPos pos) {
        if (!(accessor instanceof ServerLevel level)) return false;
        if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)) return false;
        IItemHandler handler = findPlaylist(level, pos);
        if (handler == null) return false;

        ItemStack current = jukebox.getTheItem();
        if (current.isEmpty()) return false;

        int size = handler.getSlots();
        int last = jukebox.getData(JukeboxPlaylists.LAST_SLOT);
        for (int offset = 1; offset <= size; offset++) {
            int slot = Math.floorMod(last + offset, size);
            if (!isDisc(level, handler.getStackInSlot(slot))) continue;

            ItemStack next = handler.extractItem(slot, 1, false);
            if (next.isEmpty()) continue;
            ItemStack rest = handler.insertItem(slot, current.copy(), false);
            if (!rest.isEmpty()) { // container refused it: undo, try the next slot
                handler.insertItem(slot, next, false);
                continue;
            }
            setLast(jukebox, slot);
            jukebox.setTheItem(next);
            return true;
        }

        setLast(jukebox, -1);
        jukebox.setTheItem(current);
        return true;
    }

    public static void tickEmpty(Level world, BlockPos pos, JukeboxBlockEntity jukebox) {
        if (!(world instanceof ServerLevel level) || level.getGameTime() % 5 != 0) return;
        if (!jukebox.getTheItem().isEmpty()) return;
        IItemHandler handler = findPlaylist(level, pos);
        if (handler == null) return;

        int slot = findNextSlot(level, handler, jukebox.getData(JukeboxPlaylists.LAST_SLOT));
        if (slot < 0) {
            setLast(jukebox, -1);
            return;
        }
        ItemStack next = handler.extractItem(slot, 1, false);
        if (next.isEmpty()) return;
        setLast(jukebox, slot);
        jukebox.setTheItem(next);
    }
}