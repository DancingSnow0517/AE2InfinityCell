package cn.dancingsnow.aeinfinitycell.storage;

import java.math.BigInteger;
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
    private static final String KEY_BIG_AMOUNT = "amountBig";

    private static final BigInteger BIG_ZERO = BigInteger.ZERO;
    private static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final Map<ItemStackKey, BigInteger> items = new LinkedHashMap<ItemStackKey, BigInteger>();
    private final Map<FluidStackKey, BigInteger> fluids = new LinkedHashMap<FluidStackKey, BigInteger>();

    public long getItemAmount(ItemStackKey key) {
        return clampToLong(amount(items, key));
    }

    public long getFluidAmount(FluidStackKey key) {
        return clampToLong(amount(fluids, key));
    }

    public void addItem(ItemStackKey key, long amount) {
        addItem(key, BigInteger.valueOf(amount));
    }

    public void addItem(ItemStackKey key, BigInteger amount) {
        add(items, key, amount);
    }

    public void addFluid(FluidStackKey key, long amount) {
        addFluid(key, BigInteger.valueOf(amount));
    }

    public void addFluid(FluidStackKey key, BigInteger amount) {
        add(fluids, key, amount);
    }

    public long removeItem(ItemStackKey key, long requested) {
        return remove(items, key, requested);
    }

    public long removeFluid(FluidStackKey key, long requested) {
        return remove(fluids, key, requested);
    }

    public Map<ItemStackKey, BigInteger> getItemsView() {
        return java.util.Collections.unmodifiableMap(items);
    }

    public Map<FluidStackKey, BigInteger> getFluidsView() {
        return java.util.Collections.unmodifiableMap(fluids);
    }

    public long getUsedItemTypes() {
        return items.size();
    }

    public long getUsedFluidTypes() {
        return fluids.size();
    }

    public long getStoredItemUnits() {
        return clampToLong(sum(items));
    }

    public long getStoredFluidUnits() {
        return clampToLong(sum(fluids));
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
        for (Map.Entry<ItemStackKey, BigInteger> entry : items.entrySet()) {
            if (entry.getValue()
                .signum() > 0) {
                NBTTagCompound tag = entry.getKey()
                    .writeToNBT(clampToLong(entry.getValue()));
                writeAmount(tag, entry.getValue());
                list.appendTag(tag);
            }
        }
        return list;
    }

    private NBTTagList writeFluids() {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<FluidStackKey, BigInteger> entry : fluids.entrySet()) {
            if (entry.getValue()
                .signum() > 0) {
                NBTTagCompound tag = entry.getKey()
                    .writeToNBT(clampToLong(entry.getValue()));
                writeAmount(tag, entry.getValue());
                list.appendTag(tag);
            }
        }
        return list;
    }

    private void readItems(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            BigInteger amount = readAmount(entry);
            if (amount.signum() > 0) {
                items.put(ItemStackKey.readFromNBT(entry), amount);
            }
        }
    }

    private void readFluids(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            BigInteger amount = readAmount(entry);
            if (amount.signum() > 0) {
                fluids.put(FluidStackKey.readFromNBT(entry), amount);
            }
        }
    }

    public ItemStack createItemStack(ItemStackKey key, long amount) {
        return key.toStack(amount);
    }

    public FluidStack createFluidStack(FluidStackKey key, long amount) {
        return key.toStack(amount);
    }

    private static <K> void add(Map<K, BigInteger> map, K key, BigInteger amount) {
        if (amount == null || amount.signum() <= 0) {
            return;
        }
        map.put(key, amount(map, key).add(amount));
    }

    private static <K> long remove(Map<K, BigInteger> map, K key, long requested) {
        if (requested <= 0L) {
            return 0L;
        }

        BigInteger current = amount(map, key);
        BigInteger requestedAmount = BigInteger.valueOf(requested);
        BigInteger extracted = current.min(requestedAmount);
        BigInteger remaining = current.subtract(extracted);
        if (remaining.signum() > 0) {
            map.put(key, remaining);
        } else {
            map.remove(key);
        }
        return clampToLong(extracted);
    }

    private static <K> BigInteger amount(Map<K, BigInteger> map, K key) {
        BigInteger current = map.get(key);
        return current == null ? BIG_ZERO : current;
    }

    private static BigInteger sum(Map<?, BigInteger> map) {
        BigInteger total = BIG_ZERO;
        for (BigInteger amount : map.values()) {
            total = total.add(amount);
        }
        return total;
    }

    private static long clampToLong(BigInteger amount) {
        if (amount.compareTo(BIG_LONG_MAX) > 0) {
            return Long.MAX_VALUE;
        }
        if (amount.signum() < 0) {
            return 0L;
        }
        return amount.longValue();
    }

    private static void writeAmount(NBTTagCompound tag, BigInteger amount) {
        tag.setLong(KEY_AMOUNT, clampToLong(amount));
        tag.setString(KEY_BIG_AMOUNT, amount.toString());
    }

    private static BigInteger readAmount(NBTTagCompound tag) {
        if (tag.hasKey(KEY_BIG_AMOUNT, 8)) {
            try {
                return new BigInteger(tag.getString(KEY_BIG_AMOUNT));
            } catch (NumberFormatException ignored) {
                return BIG_ZERO;
            }
        }
        return BigInteger.valueOf(tag.getLong(KEY_AMOUNT));
    }
}
