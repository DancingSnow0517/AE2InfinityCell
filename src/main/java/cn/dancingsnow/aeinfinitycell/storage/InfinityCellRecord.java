package cn.dancingsnow.aeinfinitycell.storage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import thaumicenergistics.common.storage.AEEssentiaStack;

public final class InfinityCellRecord {

    private static final String KEY_ITEMS = "items";
    private static final String KEY_FLUIDS = "fluids";
    private static final String KEY_ESSENTIA = "essentia";
    private static final String KEY_EU = "eu";
    private static final String KEY_AMOUNT = "amount";

    private final Map<ItemStackKey, CellCount> items = new LinkedHashMap<>();
    private final Map<FluidStackKey, CellCount> fluids = new LinkedHashMap<>();
    private final Map<EssentiaStackKey, CellCount> essentia = new LinkedHashMap<>();
    private final CellCount eu = new CellCount();

    public long getItemAmount(ItemStackKey key) {
        return amount(items, key);
    }

    public long getFluidAmount(FluidStackKey key) {
        return amount(fluids, key);
    }

    public long getEssentiaAmount(EssentiaStackKey key) {
        return amount(essentia, key);
    }

    public long getEUAmount() {
        return eu.longValue();
    }

    public CellCount getEUCount() {
        return eu;
    }

    public void addItem(ItemStackKey key, long amount) {
        add(items, key, amount);
    }

    public void addFluid(FluidStackKey key, long amount) {
        add(fluids, key, amount);
    }

    public void addEssentia(EssentiaStackKey key, long amount) {
        add(essentia, key, amount);
    }

    public void addEU(long amount) {
        eu.add(amount);
    }

    /**
     * 取出至多 requested 的数量并返回实际取出量；modulate 为 false 时只模拟不修改存储。
     */
    public long extractItem(ItemStackKey key, long requested, boolean modulate) {
        return extract(items, key, requested, modulate);
    }

    public long extractFluid(FluidStackKey key, long requested, boolean modulate) {
        return extract(fluids, key, requested, modulate);
    }

    public long extractEssentia(EssentiaStackKey key, long requested, boolean modulate) {
        return extract(essentia, key, requested, modulate);
    }

    public long extractEU(long requested, boolean modulate) {
        if (requested <= 0L) {
            return 0L;
        }
        long extracted = Math.min(eu.longValue(), requested);
        if (modulate && extracted > 0L) {
            eu.extract(extracted);
        }
        return extracted;
    }

    public Map<ItemStackKey, CellCount> getItemsView() {
        return Collections.unmodifiableMap(items);
    }

    public Map<FluidStackKey, CellCount> getFluidsView() {
        return Collections.unmodifiableMap(fluids);
    }

    public Map<EssentiaStackKey, CellCount> getEssentiaView() {
        return Collections.unmodifiableMap(essentia);
    }

    public long getUsedItemTypes() {
        return items.size();
    }

    public long getUsedFluidTypes() {
        return fluids.size();
    }

    public long getUsedEssentiaTypes() {
        return essentia.size();
    }

    public long getUsedEUTypes() {
        return eu.isPositive() ? 1L : 0L;
    }

    public long getStoredItemUnits() {
        return sum(items);
    }

    public long getStoredFluidUnits() {
        return sum(fluids);
    }

    public long getStoredEssentiaUnits() {
        return sum(essentia);
    }

    public long getStoredEUUnits() {
        return eu.longValue();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(KEY_ITEMS, writeEntries(items, ItemStackKey::writeToNBT));
        tag.setTag(KEY_FLUIDS, writeEntries(fluids, FluidStackKey::writeToNBT));
        tag.setTag(KEY_ESSENTIA, writeEntries(essentia, EssentiaStackKey::writeToNBT));
        tag.setString(KEY_EU, eu.toString());
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        items.clear();
        fluids.clear();
        essentia.clear();
        eu.clear();
        if (tag.hasKey(KEY_EU, 8)) {
            eu.add(CellCount.parse(tag.getString(KEY_EU)));
        }
        readEntries(tag.getTagList(KEY_ITEMS, 10), items, ItemStackKey::readFromNBT);
        readEntries(tag.getTagList(KEY_FLUIDS, 10), fluids, FluidStackKey::readFromNBT);
        readEntries(tag.getTagList(KEY_ESSENTIA, 10), essentia, EssentiaStackKey::readFromNBT);
    }

    public ItemStack createItemStack(ItemStackKey key, long amount) {
        return key.toStack(amount);
    }

    public FluidStack createFluidStack(FluidStackKey key, long amount) {
        return key.toStack(amount);
    }

    public AEEssentiaStack createEssentiaStack(EssentiaStackKey key, long amount) {
        return key.toStack(amount);
    }

    private static <K> NBTTagList writeEntries(Map<K, CellCount> entries, EntryWriter<K> writer) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<K, CellCount> entry : entries.entrySet()) {
            CellCount count = entry.getValue();
            if (count.isPositive()) {
                NBTTagCompound tag = writer.write(entry.getKey(), count.longValue());
                tag.setString(KEY_AMOUNT, count.toString());
                list.appendTag(tag);
            }
        }
        return list;
    }

    private static <K> void readEntries(NBTTagList list, Map<K, CellCount> entries, KeyReader<K> reader) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            // 旧格式用 long 保存数量，无法表示超限数量，迁移时直接忽略
            if (!entry.hasKey(KEY_AMOUNT, 8)) {
                continue;
            }
            CellCount count = CellCount.parse(entry.getString(KEY_AMOUNT));
            if (count.isPositive()) {
                entries.put(reader.read(entry), count);
            }
        }
    }

    private static <K> void add(Map<K, CellCount> map, K key, long amount) {
        if (amount <= 0L) {
            return;
        }
        map.computeIfAbsent(key, k -> new CellCount())
            .add(amount);
    }

    private static <K> long extract(Map<K, CellCount> map, K key, long requested, boolean modulate) {
        if (requested <= 0L) {
            return 0L;
        }
        CellCount current = map.get(key);
        if (current == null) {
            return 0L;
        }
        long extracted = Math.min(current.longValue(), requested);
        if (modulate && extracted > 0L) {
            current.extract(extracted);
            if (current.isZero()) {
                map.remove(key);
            }
        }
        return extracted;
    }

    private static <K> long amount(Map<K, CellCount> map, K key) {
        CellCount current = map.get(key);
        return current == null ? 0L : current.longValue();
    }

    private static long sum(Map<?, CellCount> map) {
        CellCount total = new CellCount();
        for (CellCount count : map.values()) {
            total.add(count);
        }
        return total.longValue();
    }

    private interface EntryWriter<K> {

        NBTTagCompound write(K key, long amount);
    }

    private interface KeyReader<K> {

        K read(NBTTagCompound tag);
    }
}
