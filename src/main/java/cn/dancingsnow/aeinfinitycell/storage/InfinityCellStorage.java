package cn.dancingsnow.aeinfinitycell.storage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

import cn.dancingsnow.aeinfinitycell.AEInfinityCell;

public final class InfinityCellStorage {

    private static final String DIR_NAME = "AEInfinityCell";
    private static final InfinityCellStorage INSTANCE = new InfinityCellStorage();

    private final Map<UUID, InfinityCellRecord> cache = new LinkedHashMap<UUID, InfinityCellRecord>();
    private final Set<UUID> dirty = new LinkedHashSet<UUID>();

    private InfinityCellStorage() {}

    public static InfinityCellStorage getInstance() {
        return INSTANCE;
    }

    public InfinityCellRecord getOrCreate(UUID id) {
        InfinityCellRecord cached = cache.get(id);
        if (cached != null) {
            return cached;
        }
        InfinityCellRecord record = loadFromDisk(id);
        cache.put(id, record);
        return record;
    }

    public void markDirty(UUID id) {
        if (cache.containsKey(id)) {
            dirty.add(id);
        }
    }

    public void saveAll() {
        if (dirty.isEmpty()) {
            return;
        }
        File dir = dataDir();
        if (dir == null) {
            return;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Set<UUID> toSave = new LinkedHashSet<UUID>(dirty);
        dirty.clear();
        for (UUID id : toSave) {
            InfinityCellRecord record = cache.get(id);
            if (record != null) {
                saveToDisk(id, record, dir);
            }
        }
    }

    public boolean hasCellFile(UUID id) {
        File file = cellFile(id);
        return file != null && file.exists();
    }

    public void clear() {
        cache.clear();
        dirty.clear();
    }

    private InfinityCellRecord loadFromDisk(UUID id) {
        InfinityCellRecord record = new InfinityCellRecord();
        File file = cellFile(id);
        if (file == null || !file.exists()) {
            return record;
        }
        try {
            NBTTagCompound tag = CompressedStreamTools.read(file);
            if (tag != null) {
                record.readFromNBT(tag);
            }
        } catch (IOException e) {
            AEInfinityCell.LOG.error("Failed to load cell data for {}: {}", id, e.getMessage());
        }
        return record;
    }

    private void saveToDisk(UUID id, InfinityCellRecord record, File dir) {
        File file = new File(dir, id.toString() + ".dat");
        try {
            CompressedStreamTools.safeWrite(record.writeToNBT(), file);
        } catch (IOException e) {
            AEInfinityCell.LOG.error("Failed to save cell data for {}: {}", id, e.getMessage());
        }
    }

    private File cellFile(UUID id) {
        File dir = dataDir();
        return dir == null ? null : new File(dir, id.toString() + ".dat");
    }

    private static File dataDir() {
        File saveRoot = DimensionManager.getCurrentSaveRootDirectory();
        return saveRoot == null ? null : new File(saveRoot, "data/" + DIR_NAME);
    }
}
