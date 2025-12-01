package me.kall.sacrifice.mixin;

import me.kall.sacrifice.Sacrifice;
import me.kall.sacrifice.ext.SacrificeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements SacrificeItem {
    @Unique
    private int sacrifice$pickableTick;
    @Unique
    private boolean sacrifice$isSacrifice;

    @Override
    public void sacrifice$setPickableTick(int pickableTick) {
        this.sacrifice$pickableTick = pickableTick;
    }

    @Override
    public int sacrifice$pickableTick() {
        return this.sacrifice$pickableTick;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickPickable(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        Level level = entity.level;
        if (!(level instanceof ServerLevel)) return;

        if (!this.sacrifice$pickable()) this.sacrifice$setPickableTick(this.sacrifice$pickableTick() - 1);

        if (this.sacrifice$get() && Sacrifice.isMoving(entity)) {
            UUID owner = entity.getOwner();
            if (owner == null) return;
            Entity source = ((ServerLevel)level).getEntity(owner);
            if (source instanceof LivingEntity) {
                DamageSource damageSource = DamageSource.indirectMobAttack(entity, (LivingEntity) source);
                for (Entity target : level.getEntities(entity, entity.getBoundingBox(), Entity::isAlive)) {
                    target.hurt(damageSource, Sacrifice.DAMAGE);
                }
            }
        }
    }

    @Override
    public boolean sacrifice$get() {
        return this.sacrifice$isSacrifice;
    }

    @Override
    public void sacrifice$set(boolean isSacrifice) {
        this.sacrifice$isSacrifice = isSacrifice;
    }
}
