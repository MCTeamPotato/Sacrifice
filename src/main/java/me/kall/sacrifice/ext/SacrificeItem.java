package me.kall.sacrifice.ext;

import net.minecraft.world.entity.item.ItemEntity;

public interface SacrificeItem {
    boolean sacrifice$get();
    void sacrifice$set(boolean isSacrifice);

    static SacrificeItem cast(ItemEntity itemEntity) {
        return (SacrificeItem) itemEntity;
    }
}
