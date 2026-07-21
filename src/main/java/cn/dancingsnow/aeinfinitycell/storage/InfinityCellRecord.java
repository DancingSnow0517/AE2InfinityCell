package cn.dancingsnow.aeinfinitycell.storage;

import java.math.BigInteger;
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

    private static final BigInteger BIG_ZERO = BigInteger.ZERO;
    private static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private final Map<ItemStackKey, BigInteger> items = new LinkedHashMap<>();
    private final Map<FluidStackKey, BigInteger> fluids = new LinkedHashMap<>();
    private final Map<EssentiaStackKey, BigInteger> essentia = new LinkedHashMap<>();
    private BigInteger eu = BIG_ZERO;

    public long getItemAmount(ItemStackKey key) {
        return clampToLong(amount(items, key));
    }

    public long getFluidAmount(FluidStackKey key) {
        return clampToLong(amount(fluids, key));
    }

    public long getEssentiaAmount(EssentiaStackKey key) {
        return clampToLong(amount(essentia, key));
    }

    public long getEUAmount() {
        return clampToLong(eu);
    }

    public BigInteger getEUAmountExact() {
        return eu;
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

    public void addEssentia(EssentiaStackKey key, long amount) {
        addEssentia(key, BigInteger.valueOf(amount));
    }

    public void addEssentia(EssentiaStackKey key, BigInteger amount) {
        add(essentia, key, amount);
    }

    public void addEU(long amount) {
        addEU(BigInteger.valueOf(amount));
    }

    public void addEU(BigInteger amount) {
        if (amount != null && amount.signum() > 0) {
            eu = eu.add(amount);
        }
    }

    public long removeItem(ItemStackKey key, long requested) {
        return remove(items, key, requested);
    }

    public long removeFluid(FluidStackKey key, long requested) {
        return remove(fluids, key, requested);
    }

    public long removeEssentia(EssentiaStackKey key, long requested) {
        return remove(essentia, key, requested);
    }

    public long removeEU(long requested) {
        if (requested <= 0L) {
            return 0L;
        }
        BigInteger extracted = eu.min(BigInteger.valueOf(requested));
        eu = eu.subtract(extracted);
        return clampToLong(extracted);
    }

    public Map<ItemStackKey, BigInteger> getItemsView() {
        return java.util.Collections.unmodifiableMap(items);
    }

    public Map<FluidStackKey, BigInteger> getFluidsView() {
        return java.util.Collections.unmodifiableMap(fluids);
    }

    public Map<EssentiaStackKey, BigInteger> getEssentiaView() {
        return java.util.Collections.unmodifiableMap(essentia);
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
        return eu.signum() > 0 ? 1L : 0L;
    }

    public long getStoredItemUnits() {
        return clampToLong(sum(items));
    }

    public long getStoredFluidUnits() {
        return clampToLong(sum(fluids));
    }

    public long getStoredEssentiaUnits() {
        return clampToLong(sum(essentia));
    }

    public long getStoredEUUnits() {
        return clampToLong(eu);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(KEY_ITEMS, writeItems());
        tag.setTag(KEY_FLUIDS, writeFluids());
        tag.setTag(KEY_ESSENTIA, writeEssentia());
        tag.setString(KEY_EU, eu.toString());
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        items.clear();
        fluids.clear();
        essentia.clear();
        eu = readAmount(tag, KEY_EU);
        readItems(tag.getTagList(KEY_ITEMS, 10));
        readFluids(tag.getTagList(KEY_FLUIDS, 10));
        readEssentia(tag.getTagList(KEY_ESSENTIA, 10));
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

    private NBTTagList writeEssentia() {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<EssentiaStackKey, BigInteger> entry : essentia.entrySet()) {
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

    private void readEssentia(NBTTagList list) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            BigInteger amount = readAmount(entry);
            if (amount.signum() > 0) {
                essentia.put(EssentiaStackKey.readFromNBT(entry), amount);
            }
        }
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
        tag.setString(KEY_AMOUNT, amount.toString());
    }

    private static BigInteger readAmount(NBTTagCompound tag) {
        return readAmount(tag, KEY_AMOUNT);
    }

    private static BigInteger readAmount(NBTTagCompound tag, String key) {
        if (!tag.hasKey(key, 8)) {
            return BIG_ZERO;
        }
        try {
            BigInteger amount = new BigInteger(tag.getString(key));
            return amount.signum() > 0 ? amount : BIG_ZERO;
        } catch (NumberFormatException ignored) {
            return BIG_ZERO;
        }
    }
}
