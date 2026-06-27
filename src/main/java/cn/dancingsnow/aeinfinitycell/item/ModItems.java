package cn.dancingsnow.aeinfinitycell.item;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static final ItemInfinityStorageCell INFINITY_STORAGE_CELL = new ItemInfinityStorageCell();

    private ModItems() {}

    public static void register() {
        GameRegistry.registerItem(INFINITY_STORAGE_CELL, "infinity_storage_cell");
    }
}
