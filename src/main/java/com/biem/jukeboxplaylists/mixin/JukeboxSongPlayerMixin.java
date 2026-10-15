package com.biem.jukeboxplaylists.mixin;

import com.biem.jukeboxplaylists.PlaylistHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxSongPlayer.class)
public abstract class JukeboxSongPlayerMixin {
    @Shadow private long ticksSinceSongStarted;
    @Shadow private Holder<JukeboxSong> song;
    @Shadow @Final private BlockPos blockPos;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void jukeboxplaylists$onTick(LevelAccessor level, BlockState state, CallbackInfo ci) {
        if (level.isClientSide() || this.song == null) return;
        if (this.song.value().hasFinished(this.ticksSinceSongStarted)
                && PlaylistHandler.onSongFinished(level, this.blockPos)) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "spawnMusicParticles",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private static int jukeboxplaylists$higherParticles(ServerLevel level, ParticleOptions type,
                                                        double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        BlockPos jukeboxPos = BlockPos.containing(x, y - 1.0, z);
        if (PlaylistHandler.isPlaylistMode(level, jukeboxPos)) {
            y += 1.0;
        }
        return level.sendParticles(type, x, y, z, count, dx, dy, dz, speed);
    }
}