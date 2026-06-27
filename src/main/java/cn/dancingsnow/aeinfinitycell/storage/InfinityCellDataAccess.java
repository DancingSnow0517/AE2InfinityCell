package cn.dancingsnow.aeinfinitycell.storage;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public final class InfinityCellDataAccess {

    private InfinityCellDataAccess() {}

    public static InfinityCellSavedData get(World world) {
        World serverWorld = serverWorld(world);
        if (serverWorld == null || serverWorld.isRemote) {
            return null;
        }

        InfinityCellSavedData data = (InfinityCellSavedData) serverWorld
            .loadItemData(InfinityCellSavedData.class, InfinityCellSavedData.DATA_NAME);
        if (data == null) {
            data = new InfinityCellSavedData(InfinityCellSavedData.DATA_NAME);
            serverWorld.setItemData(InfinityCellSavedData.DATA_NAME, data);
        }
        return data;
    }

    private static World serverWorld(World world) {
        if (world != null && !world.isRemote) {
            return world;
        }

        WorldServer overworld = DimensionManager.getWorld(0);
        if (overworld != null) {
            return overworld;
        }

        WorldServer[] worlds = DimensionManager.getWorlds();
        return worlds.length == 0 ? null : worlds[0];
    }
}
