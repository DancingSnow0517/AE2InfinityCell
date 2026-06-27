package cn.dancingsnow.aeinfinitycell.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

public final class InfinityCellSavedData extends WorldSavedData {

    public static final String DATA_NAME = "aeinfinitycell_infinity_cells";

    private static final String KEY_RECORDS = "records";
    private static final String KEY_ID = "id";
    private static final String KEY_DATA = "data";

    private final Map<UUID, InfinityCellRecord> records = new LinkedHashMap<UUID, InfinityCellRecord>();

    public InfinityCellSavedData(String name) {
        super(name);
    }

    public InfinityCellRecord getOrCreate(UUID id) {
        InfinityCellRecord record = records.get(id);
        if (record == null) {
            record = new InfinityCellRecord();
            records.put(id, record);
            markDirty();
        }
        return record;
    }

    public InfinityCellRecord get(UUID id) {
        return records.get(id);
    }

    public int size() {
        return records.size();
    }

    public Map<UUID, InfinityCellRecord> getRecordsView() {
        return java.util.Collections.unmodifiableMap(records);
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
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, InfinityCellRecord> entry : records.entrySet()) {
            NBTTagCompound recordTag = new NBTTagCompound();
            recordTag.setString(
                KEY_ID,
                entry.getKey()
                    .toString());
            recordTag.setTag(
                KEY_DATA,
                entry.getValue()
                    .writeToNBT());
            list.appendTag(recordTag);
        }
        tag.setTag(KEY_RECORDS, list);
    }

    private static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
