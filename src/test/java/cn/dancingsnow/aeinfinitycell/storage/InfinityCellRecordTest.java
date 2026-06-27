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
        loaded.readFromNBT(record.writeToNBT());

        assertEquals(Long.MAX_VALUE, loaded.getItemAmount(key));
        assertEquals(
            amount,
            loaded.getItemsView()
                .get(key));
    }

    @Test
    public void legacyLongAmountsStillLoad() {
        InfinityCellRecord record = new InfinityCellRecord();
        ItemStackKey key = itemKey("minecraft:cobblestone", 0);
        NBTTagCompound tag = key.writeToNBT(1234L);
        NBTTagCompound root = new NBTTagCompound();
        net.minecraft.nbt.NBTTagList items = new net.minecraft.nbt.NBTTagList();
        items.appendTag(tag);
        root.setTag("items", items);

        record.readFromNBT(root);

        assertEquals(1234L, record.getItemAmount(key));
        assertEquals(
            BigInteger.valueOf(1234L),
            record.getItemsView()
                .get(key));
    }

    private static ItemStackKey itemKey(String id, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id);
        tag.setInteger("meta", meta);
        return ItemStackKey.readFromNBT(tag);
    }
}
