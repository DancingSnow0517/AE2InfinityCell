package cn.dancingsnow.aeinfinitycell.storage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

public final class InfinityCellLegacySavedData extends WorldSavedData {

    public static final String DATA_NAME = "aeinfinitycell_infinity_cells";

    private static final String KEY_RECORDS = "records";
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";

    private final Map<UUID, InfinityCellRecord> records = new LinkedHashMap<UUID, InfinityCellRecord>();

    public InfinityCellLegacySavedData(String name) {
        super(name);
    }

    public Map<UUID, InfinityCellRecord> getRecords() {
        return Collections.unmodifiableMap(records);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        records.clear();
        NBTTagList list = tag.getTagList(KEY_RECORDS, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            UUID id = parseUuid(entry.getString(KEY_ID));
            if (id == null) {
                continue;
            }
            InfinityCellRecord record = new InfinityCellRecord();
            record.readFromNBT(entry.getCompoundTag(KEY_DATA));
            records.put(id, record);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        // read-only: only used during migration, never rewritten
    }

    private static UUID parseUuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
