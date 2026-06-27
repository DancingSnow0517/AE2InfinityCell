package cn.dancingsnow.aeinfinitycell.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.junit.Test;

public class NbtKeyTest {

    @Test
    public void equalTagsProduceEqualKeys() {
        NBTTagCompound first = new NBTTagCompound();
        first.setString("mode", "exact");
        first.setInteger("tier", 4);

        NBTTagCompound second = new NBTTagCompound();
        second.setInteger("tier", 4);
        second.setString("mode", "exact");

        assertEquals(NbtKey.of(first), NbtKey.of(second));
        assertEquals(
            NbtKey.of(first)
                .hashCode(),
            NbtKey.of(second)
                .hashCode());
    }

    @Test
    public void keyDoesNotChangeWhenSourceTagMutates() {
        NBTTagCompound source = new NBTTagCompound();
        source.setString("name", "before");

        NbtKey key = NbtKey.of(source);
        source.setString("name", "after");

        assertNotEquals(key, NbtKey.of(source));
        assertEquals(
            "before",
            key.copyTag()
                .getString("name"));
    }

    @Test
    public void nullTagCreatesNullKey() {
        assertEquals(NbtKey.NONE, NbtKey.of(null));
        assertNotNull(NbtKey.NONE.copyTag());
    }

    @Test
    public void listTagsAreCopiedWithoutMutatingSource() {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagString("first"));
        list.appendTag(new NBTTagString("second"));
        NBTTagCompound source = new NBTTagCompound();
        source.setTag("list", list);

        NbtKey key = NbtKey.of(source);

        assertEquals(2, list.tagCount());
        assertEquals(
            2,
            key.copyTag()
                .getTagList("list", 8)
                .tagCount());
    }

    @Test
    public void byteArraysCompareByContentNotLengthOnly() {
        NBTTagCompound first = new NBTTagCompound();
        first.setByteArray("bytes", new byte[] { 1, 2 });
        NBTTagCompound second = new NBTTagCompound();
        second.setByteArray("bytes", new byte[] { 1, 3 });

        assertNotEquals(NbtKey.of(first), NbtKey.of(second));
    }
}
