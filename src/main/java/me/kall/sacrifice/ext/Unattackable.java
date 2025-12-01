package me.kall.sacrifice.ext;

public interface Unattackable {
    int sacrifice$unattackableTickCount();
    void sacrifice$setUnattackableTickCount(int tick);

    default boolean sacrifice$isUnattackable() {
        return sacrifice$unattackableTickCount() != 0;
    }
}
