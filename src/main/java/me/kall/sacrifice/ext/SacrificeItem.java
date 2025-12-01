package me.kall.sacrifice.ext;

import net.minecraft.world.entity.item.ItemEntity;

public interface SacrificeItem {
    boolean sacrifice$get();
    void sacrifice$set(boolean isSacrifice);

    void sacrifice$setPickableTick(int pickableTick);
    int sacrifice$pickableTick();

    default boolean sacrifice$pickable() {
        return this.sacrifice$pickableTick() == 0;
    }

    static SacrificeItem cast(ItemEntity itemEntity) {
        return (SacrificeItem) itemEntity;
    }
}
