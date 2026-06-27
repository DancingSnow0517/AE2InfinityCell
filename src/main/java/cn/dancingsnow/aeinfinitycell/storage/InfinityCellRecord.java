package cn.dancingsnow.aeinfinitycell.storage;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

public final class InfinityCellRecord {

    private static final String KEY_ITEMS = "items";
    private static final String KEY_FLUIDS = "fluids";
    private static final String KEY_AMOUNT = "amount";

    private final Map<ItemStackKey, Long> items = new LinkedHashMap<ItemStackKey, Long>();
    private final Map<FluidStackKey, Long> fluids = new LinkedHashMap<FluidStackKey, Long>();

    public long getItemAmount(ItemStackKey key) {
        Long amount = items.get(key);
        return amount == null ? 0L : amount.longValue();
    }

    public long getFluidAmount(FluidStackKey key) {
        Long amount = fluids.get(key);
        return amount == null ? 0L : amount.longValue();
    }

    public void addItem(ItemStackKey key, long amount) {
        add(items, key, amount);
    }

    public void addFluid(FluidStackKey key, long amount) {
        add(fluids, key, amount);
    }

    public long removeItem(ItemStackKey key, long requested) {
        return remove(items, key, requested);
    }

    public long removeFluid(FluidStackKey key, long requested) {
        return remove(fluids, key, requested);
    }

    public Map<ItemStackKey, Long> getItemsView() {
        return java.util.Collections.unmodifiableMap(items);
    }

    public Map<FluidStackKey, Long> getFluidsView() {
        return java.util.Collections.unmodifiableMap(fluids);
    }

    public long getUsedItemTypes() {
        return items.size();
    }

    public long getUsedFluidTypes() {
        return fluids.size();
    }

    public long getStoredItemUnits() {
        return sum(items);
    }

    public long getStoredFluidUnits() {
        return sum(fluids);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(KEY_ITEMS, writeItems());
        tag.setTag(KEY_FLUIDS, writeFluids());
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        items.clear();
        fluids.clear();
        readItems(tag.getTagList(KEY_ITEMS, 10));
        readFluids(tag.getTagList(KEY_FLUIDS, 10));
    }

    private NBTTagList writeItems() {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<ItemStackKey, Long> entry : items.entrySet()) {
            if (entry.getValue()
                .longValue() > 0L) {
                list.appendTag(
                    entry.getKey()
                        .writeToNBT(
                            entry.getValue()
                                .longValue()));
            }
        }
        return list;
    }

    private NBTTagList writeFluids() {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<FluidStackKey, Long> entry : fluids.entrySet()) {
            if (entry.getValue()
                .longValue() > 0L) {
                list.appendTag(
                    entry.getKey()
                        .writeToNBT(
                            entry.getValue()
                                .longValue()));
            }
        }
        return list;
    }

    private void readItems(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            long amount = entry.getLong(KEY_AMOUNT);
            if (amount > 0L) {
                items.put(ItemStackKey.readFromNBT(entry), Long.valueOf(amount));
            }
        }
    }

    private void readFluids(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            long amount = entry.getLong(KEY_AMOUNT);
            if (amount > 0L) {
                fluids.put(FluidStackKey.readFromNBT(entry), Long.valueOf(amount));
            }
        }
    }

    public ItemStack createItemStack(ItemStackKey key, long amount) {
        return key.toStack(amount);
    }

    public FluidStack createFluidStack(FluidStackKey key, long amount) {
        return key.toStack(amount);
    }

    private static <K> void add(Map<K, Long> map, K key, long amount) {
        if (amount <= 0L) {
            return;
        }
        long current = amount(map, key);
        long next = saturatedAdd(current, amount);
        map.put(key, Long.valueOf(next));
    }

    private static <K> long remove(Map<K, Long> map, K key, long requested) {
        if (requested <= 0L) {
            return 0L;
        }

        long current = amount(map, key);
        long extracted = Math.min(current, requested);
        long remaining = current - extracted;
        if (remaining > 0L) {
            map.put(key, Long.valueOf(remaining));
        } else {
            map.remove(key);
        }
        return extracted;
    }

    private static <K> long amount(Map<K, Long> map, K key) {
        Long current = map.get(key);
        return current == null ? 0L : current.longValue();
    }

    private static long sum(Map<?, Long> map) {
        long total = 0L;
        for (Long amount : map.values()) {
            total = saturatedAdd(total, amount.longValue());
        }
        return total;
    }

    private static long saturatedAdd(long a, long b) {
        long result = a + b;
        if (((a ^ result) & (b ^ result)) < 0L) {
            return Long.MAX_VALUE;
        }
        return result;
    }
}
