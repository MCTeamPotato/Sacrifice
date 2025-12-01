package me.kall.sacrifice.mixin;

import me.kall.sacrifice.ext.Unattackable;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements Unattackable {
    @Unique private int sacrifice$unattackableTickCount;

    @Override
    public int sacrifice$unattackableTickCount() {
        return this.sacrifice$unattackableTickCount;
    }

    @Override
    public void sacrifice$setUnattackableTickCount(int tick) {
        this.sacrifice$unattackableTickCount = tick;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (this.sacrifice$isUnattackable()) this.sacrifice$setUnattackableTickCount(this.sacrifice$unattackableTickCount() - 1);
    }
}
