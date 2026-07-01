package cn.dancingsnow.aeinfinitycell.storage;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import cn.dancingsnow.aeinfinitycell.AEInfinityCell;

public final class InfinityCellDataAccess {

    private InfinityCellDataAccess() {}

    public static InfinityCellRecord getOrCreate(UUID id, World world) {
        if (serverWorld(world) == null) {
            return null;
        }
        return InfinityCellStorage.getInstance().getOrCreate(id);
    }

    public static void markDirty(UUID id, World world) {
        if (serverWorld(world) == null) {
            return;
        }
        InfinityCellStorage.getInstance().markDirty(id);
    }

    /**
     * One-time migration from the old single-file WorldSavedData format to per-cell files.
     * Safe to call every server start — skips UUIDs that already have a per-cell file on disk.
     */
    public static void migrateLegacy(World world) {
        World serverWorld = serverWorld(world);
        if (serverWorld == null) {
            return;
        }

        InfinityCellLegacySavedData legacy = (InfinityCellLegacySavedData) serverWorld
            .loadItemData(InfinityCellLegacySavedData.class, InfinityCellLegacySavedData.DATA_NAME);
        if (legacy == null || legacy.getRecords().isEmpty()) {
            return;
        }

        InfinityCellStorage storage = InfinityCellStorage.getInstance();
        int migrated = 0;
        for (Map.Entry<UUID, InfinityCellRecord> entry : legacy.getRecords().entrySet()) {
            UUID id = entry.getKey();
            if (storage.hasCellFile(id)) {
                continue;
            }
            InfinityCellRecord dest = storage.getOrCreate(id);
            dest.readFromNBT(entry.getValue().writeToNBT());
            storage.markDirty(id);
            migrated++;
        }

        if (migrated > 0) {
            storage.saveAll();
            deleteLegacyFile();
        }
    }

    private static void deleteLegacyFile() {
        File saveRoot = DimensionManager.getCurrentSaveRootDirectory();
        if (saveRoot == null) {
            return;
        }
        File legacyFile = new File(saveRoot, "data/" + InfinityCellLegacySavedData.DATA_NAME + ".dat");
        if (legacyFile.exists() && !legacyFile.delete()) {
            AEInfinityCell.LOG.warn("Could not delete legacy cell data file: {}", legacyFile.getPath());
        }
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
