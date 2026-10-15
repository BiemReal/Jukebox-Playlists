package com.biem.jukeboxplaylists.mixin;

import com.biem.jukeboxplaylists.PlaylistHandler;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(JukeboxBlock.class)
public abstract class JukeboxBlockMixin {
    @SuppressWarnings("unchecked")
    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void jukeboxplaylists$emptyTicker(
            Level level, BlockState state, BlockEntityType<T> type,
            CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (level.isClientSide() || state.getValue(JukeboxBlock.HAS_RECORD) || type != BlockEntityType.JUKEBOX) return;
        BlockEntityTicker<JukeboxBlockEntity> ticker = (l, p, s, be) -> PlaylistHandler.tickEmpty(l, p, be);
        cir.setReturnValue((BlockEntityTicker<T>) (BlockEntityTicker<?>) ticker);
    }
}