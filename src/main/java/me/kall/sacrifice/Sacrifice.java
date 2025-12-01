package me.kall.sacrifice;

import me.kall.duplicationless.config.JsonConfig;
import me.kall.sacrifice.ext.SacrificeItem;
import me.kall.sacrifice.ext.Unattackable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mod(Sacrifice.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Sacrifice.MOD_ID)
public final class Sacrifice {
    public static final String MOD_ID = "sacrifice";

    private static final JsonConfig CONFIG = JsonConfig.create(MOD_ID, "1")
            .put("SacrificeItemsLifespan", 12000)
            .put("InvulnerabilityTickCountAfterSacrificing", 200)
            .put("SacrificeItemsProjectileDamageAmount", 2.0F)
            .put("SacrificableInventoryItemsPercent", 0.5F)
            .initialize();

    public static final int LIFESPAN = CONFIG.getInt("SacrificeItemsLifespan");
    public static final int INVULNERABILITY = CONFIG.getInt("InvulnerabilityTickCountAfterSacrificing");
    public static final float DAMAGE = CONFIG.getFloat("SacrificeItemsProjectileDamageAmount");
    public static final float PERCENT = CONFIG.getFloat("SacrificableInventoryItemsPercent");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void reachDeath(LivingDamageEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof ServerPlayer)) return;
        if (entity.getHealth() > event.getAmount()) return;

        Inventory inventory = ((ServerPlayer) entity).getInventory();
        Level level = entity.level;
        Random random = ThreadLocalRandom.current();

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        UUID uuid = entity.getUUID();

        long filledSlots = inventory.items.stream().filter(stack -> !stack.isEmpty()).count() +
                inventory.armor.stream().filter(stack -> !stack.isEmpty()).count() +
                inventory.offhand.stream().filter(stack -> !stack.isEmpty()).count();
        long totalSlots = inventory.getContainerSize();

        if ((float) filledSlots / totalSlots > PERCENT) {
            for (int i = 0; i < totalSlots; i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;
                ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack.copy());
                itemEntity.lifespan = LIFESPAN;
                itemEntity.setThrower(uuid);
                itemEntity.setOwner(uuid);
                itemEntity.setDeltaMovement(random.nextDouble() - 0.5, 0.3 + random.nextDouble() * 0.3, random.nextDouble() - 0.5);

                SacrificeItem.cast(itemEntity).sacrifice$set(true);
                SacrificeItem.cast(itemEntity).sacrifice$setPickableTick(60);

                level.addFreshEntity(itemEntity);
                inventory.setItem(i, ItemStack.EMPTY);
            }

            ((Unattackable)entity).sacrifice$setUnattackableTickCount(INVULNERABILITY);

            event.setCanceled(true);
        }
    }

    public static boolean isMoving(@NotNull ItemEntity item) {
        Vec3 deltaMovement = item.getDeltaMovement();
        return (Math.abs(deltaMovement.x) > 0.02) || (Math.abs(deltaMovement.z) > 0.02);
    }

    @SubscribeEvent
    public static void attackPlayer(@NotNull LivingAttackEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!(entity instanceof ServerPlayer)) return;
        if (((Unattackable)entity).sacrifice$isUnattackable()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void itemPick(@NotNull EntityItemPickupEvent event) {
        if (((SacrificeItem)event.getItem()).sacrifice$pickable()) return;
        event.setCanceled(true);
    }
}
