package cn.dancingsnow.aeinfinitycell.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

public final class NbtKey {

    public static final NbtKey NONE = new NbtKey(new NBTTagCompound(), "{}");

    private final NBTTagCompound tag;
    private final String canonical;

    private NbtKey(NBTTagCompound tag, String canonical) {
        this.tag = tag;
        this.canonical = canonical;
    }

    public static NbtKey of(NBTTagCompound tag) {
        if (tag == null) {
            return NONE;
        }

        NBTTagCompound canonicalTag = canonicalizeCompound(tag);
        return new NbtKey(canonicalTag, canonicalString(canonicalTag));
    }

    public NBTTagCompound copyTag() {
        return (NBTTagCompound) tag.copy();
    }

    public boolean isEmpty() {
        return this == NONE || tag.func_150296_c()
            .isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NbtKey)) {
            return false;
        }
        NbtKey nbtKey = (NbtKey) o;
        return Objects.equals(canonical, nbtKey.canonical);
    }

    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    @Override
    public String toString() {
        return canonical;
    }

    private static NBTTagCompound canonicalizeCompound(NBTTagCompound source) {
        NBTTagCompound result = new NBTTagCompound();
        List<String> keys = sortedKeys(source);
        for (String key : keys) {
            result.setTag(key, canonicalizeTag(source.getTag(key)));
        }
        return result;
    }

    private static NBTBase canonicalizeTag(NBTBase tag) {
        if (tag instanceof NBTTagCompound) {
            return canonicalizeCompound((NBTTagCompound) tag);
        }
        if (tag instanceof NBTTagList) {
            NBTTagList result = new NBTTagList();
            NBTTagList source = (NBTTagList) tag.copy();
            while (source.tagCount() > 0) {
                result.appendTag(canonicalizeTag(source.removeTag(0)));
            }
            return result;
        }
        return tag.copy();
    }

    private static String canonicalString(NBTBase tag) {
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) tag;
            StringBuilder builder = new StringBuilder("{");
            List<String> keys = sortedKeys(compound);
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) {
                    builder.append(',');
                }
                String key = keys.get(i);
                builder.append(key.length())
                    .append(':')
                    .append(key)
                    .append('=')
                    .append(canonicalString(compound.getTag(key)));
            }
            return builder.append('}')
                .toString();
        }

        if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag.copy();
            StringBuilder builder = new StringBuilder("[");
            int index = 0;
            while (list.tagCount() > 0) {
                if (index > 0) {
                    builder.append(',');
                }
                builder.append(canonicalString(list.removeTag(0)));
                index++;
            }
            return builder.append(']')
                .toString();
        }

        if (tag instanceof NBTTagByteArray) {
            return tag.getId() + ":" + Arrays.toString(((NBTTagByteArray) tag).func_150292_c());
        }

        if (tag instanceof NBTTagIntArray) {
            return tag.getId() + ":" + Arrays.toString(((NBTTagIntArray) tag).func_150302_c());
        }

        return tag.getId() + ":" + tag.toString();
    }

    private static List<String> sortedKeys(NBTTagCompound compound) {
        List<String> keys = new ArrayList<String>(compound.func_150296_c());
        Collections.sort(keys);
        return keys;
    }
}
