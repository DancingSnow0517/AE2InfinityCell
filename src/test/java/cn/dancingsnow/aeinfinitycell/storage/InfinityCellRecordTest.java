package cn.dancingsnow.aeinfinitycell.storage;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.Test;

public class InfinityCellRecordTest {

    @Test
    public void itemAmountsStoreBeyondLongAndClampForAe() {
        InfinityCellRecord record = new InfinityCellRecord();
        ItemStackKey key = itemKey("minecraft:stone", 0);

        record.addItem(key, Long.MAX_VALUE);
        record.addItem(key, 1L);

        assertEquals(Long.MAX_VALUE, record.getItemAmount(key));
        assertEquals(
            BigInteger.valueOf(Long.MAX_VALUE)
                .add(BigInteger.ONE),
            record.getItemsView()
                .get(key));
    }

    @Test
    public void bigIntegerAmountsRoundTripThroughNbt() {
        InfinityCellRecord record = new InfinityCellRecord();
        ItemStackKey key = itemKey("minecraft:dirt", 0);
        BigInteger amount = BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.valueOf(42L));

        record.addItem(key, amount);

        InfinityCellRecord loaded = new InfinityCellRecord();
        NBTTagCompound serialized = record.writeToNBT();
        loaded.readFromNBT(serialized);

        assertEquals(Long.MAX_VALUE, loaded.getItemAmount(key));
        assertEquals(
            amount,
            loaded.getItemsView()
                .get(key));
        assertEquals(
            amount.toString(),
            serialized.getTagList("items", 10)
                .getCompoundTagAt(0)
                .getString("amount"));
    }

    @Test
    public void essentiaAmountsRoundTripThroughNbt() {
        InfinityCellRecord record = new InfinityCellRecord();
        EssentiaStackKey key = essentiaKey("aer");
        BigInteger amount = BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.valueOf(99L));

        record.addEssentia(key, amount);

        InfinityCellRecord loaded = new InfinityCellRecord();
        NBTTagCompound serialized = record.writeToNBT();
        loaded.readFromNBT(serialized);

        assertEquals(Long.MAX_VALUE, loaded.getEssentiaAmount(key));
        assertEquals(
            amount,
            loaded.getEssentiaView()
                .get(key));
        assertEquals(
            "aer",
            serialized.getTagList("essentia", 10)
                .getCompoundTagAt(0)
                .getString("aspect"));
        assertEquals(
            amount.toString(),
            serialized.getTagList("essentia", 10)
                .getCompoundTagAt(0)
                .getString("amount"));
    }

    @Test
    public void euAmountRoundTripsThroughNbtBeyondLongMax() {
        InfinityCellRecord record = new InfinityCellRecord();
        BigInteger amount = BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.valueOf(123L));

        record.addEU(amount);

        InfinityCellRecord loaded = new InfinityCellRecord();
        NBTTagCompound serialized = record.writeToNBT();
        loaded.readFromNBT(serialized);

        assertEquals(Long.MAX_VALUE, loaded.getEUAmount());
        assertEquals(amount, loaded.getEUAmountExact());
        assertEquals(1L, loaded.getUsedEUTypes());
        assertEquals(amount.toString(), serialized.getString("eu"));

        assertEquals(Long.MAX_VALUE, loaded.removeEU(Long.MAX_VALUE));
        assertEquals(BigInteger.valueOf(123L), loaded.getEUAmountExact());
    }

    @Test
    public void legacyLongAmountsAreIgnored() {
        InfinityCellRecord record = new InfinityCellRecord();
        ItemStackKey key = itemKey("minecraft:cobblestone", 0);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("item", "minecraft:cobblestone");
        tag.setInteger("damage", 0);
        tag.setLong("amount", 1234L);
        NBTTagCompound root = new NBTTagCompound();
        net.minecraft.nbt.NBTTagList items = new net.minecraft.nbt.NBTTagList();
        items.appendTag(tag);
        root.setTag("items", items);

        record.readFromNBT(root);

        assertEquals(0L, record.getItemAmount(key));
        assertEquals(
            0,
            record.getItemsView()
                .size());
    }

    private static ItemStackKey itemKey(String id, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id);
        tag.setInteger("meta", meta);
        return ItemStackKey.readFromNBT(tag);
    }

    private static EssentiaStackKey essentiaKey(String aspect) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("aspect", aspect);
        return EssentiaStackKey.readFromNBT(tag);
    }
}
