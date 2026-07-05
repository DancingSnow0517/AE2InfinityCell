package cn.dancingsnow.aeinfinitycell.nei;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.Test;

import cn.dancingsnow.aeinfinitycell.storage.EssentiaStackKey;
import cn.dancingsnow.aeinfinitycell.storage.FluidStackKey;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import cn.dancingsnow.aeinfinitycell.storage.ItemStackKey;

public class InfinityCellViewPreviewTest {

    @Test
    public void itemPreviewSortsByAmountDescendingAndCapsToLimit() {
        InfinityCellRecord record = new InfinityCellRecord();
        for (int i = 1; i <= 70; i++) {
            record.addItem(itemKey("test:item_" + i, i), BigInteger.valueOf(i));
        }

        List<InfinityCellViewPreview.Entry<ItemStackKey>> entries = InfinityCellViewPreview.items(record, 63);

        assertEquals(63, entries.size());
        assertEquals(
            "test:item_70",
            entries.get(0)
                .getKey()
                .getItemName());
        assertEquals(
            BigInteger.valueOf(70L),
            entries.get(0)
                .getAmount());
        assertEquals(
            "test:item_8",
            entries.get(62)
                .getKey()
                .getItemName());
        assertEquals(
            BigInteger.valueOf(8L),
            entries.get(62)
                .getAmount());
    }

    @Test
    public void eachChannelIsLimitedIndependently() {
        InfinityCellRecord record = new InfinityCellRecord();
        for (int i = 1; i <= 70; i++) {
            record.addItem(itemKey("test:item_" + i, i), i);
            record.addFluid(fluidKey("fluid_" + i), i);
            record.addEssentia(essentiaKey("aspect_" + i), i);
        }

        assertEquals(
            63,
            InfinityCellViewPreview.items(record, 63)
                .size());
        assertEquals(
            63,
            InfinityCellViewPreview.fluids(record, 63)
                .size());
        assertEquals(
            63,
            InfinityCellViewPreview.essentia(record, 63)
                .size());
    }

    @Test
    public void entryStackSizeClampsToLongMaxForRendering() {
        InfinityCellRecord record = new InfinityCellRecord();
        ItemStackKey key = itemKey("test:overflow", 0);
        BigInteger amount = BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.ONE);
        record.addItem(key, amount);

        InfinityCellViewPreview.Entry<ItemStackKey> entry = InfinityCellViewPreview.items(record, 63)
            .get(0);

        assertEquals(amount, entry.getAmount());
        assertEquals(Long.MAX_VALUE, entry.getStackSize());
    }

    @Test
    public void nonPositiveLimitProducesNoEntries() {
        InfinityCellRecord record = new InfinityCellRecord();
        record.addItem(itemKey("test:item", 0), 1L);

        assertEquals(
            0,
            InfinityCellViewPreview.items(record, 0)
                .size());
    }

    @Test
    public void pagesAreCreatedOnlyForPopulatedChannelsInFixedOrder() {
        InfinityCellRecord record = new InfinityCellRecord();
        record.addFluid(fluidKey("steam"), 1000L);
        record.addEssentia(essentiaKey("aer"), 10L);

        List<InfinityCellViewPreview.Page> pages = InfinityCellViewPreview.pages(record, 63);

        assertEquals(2, pages.size());
        assertEquals(
            InfinityCellViewPreview.Channel.FLUIDS,
            pages.get(0)
                .getChannel());
        assertEquals(
            InfinityCellViewPreview.Channel.ESSENTIA,
            pages.get(1)
                .getChannel());
        assertEquals(
            1L,
            pages.get(0)
                .getTotalTypes());
        assertEquals(
            1,
            pages.get(0)
                .getEntries()
                .size());
    }

    private static ItemStackKey itemKey(String id, int damage) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("item", id);
        tag.setInteger("damage", damage);
        return ItemStackKey.readFromNBT(tag);
    }

    private static FluidStackKey fluidKey(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id);
        return FluidStackKey.readFromNBT(tag);
    }

    private static EssentiaStackKey essentiaKey(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("aspect", id);
        return EssentiaStackKey.readFromNBT(tag);
    }
}
