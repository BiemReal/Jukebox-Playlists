package com.biem.jukeboxplaylists.client;

import com.biem.jukeboxplaylists.JukeboxPlaylists;
import com.biem.jukeboxplaylists.PlaylistNetwork;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = JukeboxPlaylists.MODID, value = Dist.CLIENT)
public final class PlayingNextTooltip {
    private PlayingNextTooltip() {}

    private static final Component PLAYING_NEXT = Component.translatable("tooltip.jukeboxplaylists.playing_next");

    private static ItemStack target = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre event) {
        target = ItemStack.EMPTY;
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        if (!(screen.getMenu() instanceof ChestMenu menu)) return;
        if (PlaylistNetwork.ClientState.containerId != menu.containerId) return;

        int last = PlaylistNetwork.ClientState.lastSlot;
        if (last < 0) return; // nothing taken from the chest yet (same as vanilla: no marker)

        Slot hovered = screen.getSlotUnderMouse();
        int size = menu.getRowCount() * 9;
        if (hovered == null || hovered.index >= size || !hovered.hasItem()) return;

        if (hovered.index == findNext(menu, size, last)) {
            target = hovered.getItem();
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!target.isEmpty() && event.getItemStack() == target) {
            event.getToolTip().add(PLAYING_NEXT);
        }
    }

    @SubscribeEvent
    public static void onClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?>) {
            PlaylistNetwork.ClientState.clear();
            target = ItemStack.EMPTY;
        }
    }

    private static int findNext(ChestMenu menu, int size, int last) {
        for (int offset = 1; offset <= size; offset++) {
            int slot = Math.floorMod(last + offset, size);
            if (menu.getSlot(slot).getItem().has(DataComponents.JUKEBOX_PLAYABLE)) return slot;
        }
        return -1;
    }
}