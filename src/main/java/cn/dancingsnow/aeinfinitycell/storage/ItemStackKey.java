package cn.dancingsnow.aeinfinitycell.storage;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ItemStackKey {

    private static final String KEY_ITEM = "item";
    private static final String KEY_DAMAGE = "damage";
    private static final String KEY_TAG = "tag";
    private static final String KEY_AMOUNT = "amount";

    private final String itemName;
    private final int damage;
    private final NbtKey tag;

    private ItemStackKey(String itemName, int damage, NbtKey tag) {
        this.itemName = itemName;
        this.damage = damage;
        this.tag = tag;
    }

    public static ItemStackKey from(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            throw new IllegalArgumentException("stack");
        }

        GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        if (id == null) {
            throw new IllegalArgumentException("unregistered item " + stack.getItem());
        }
        return new ItemStackKey(id.toString(), stack.getItemDamage(), NbtKey.of(stack.stackTagCompound));
    }

    public static ItemStackKey readFromNBT(NBTTagCompound serialized) {
        String itemName = serialized.getString(KEY_ITEM);
        int damage = serialized.getInteger(KEY_DAMAGE);
        NbtKey tag = serialized.hasKey(KEY_TAG, 10) ? NbtKey.of(serialized.getCompoundTag(KEY_TAG)) : NbtKey.NONE;
        return new ItemStackKey(itemName, damage, tag);
    }

    public NBTTagCompound writeToNBT(long amount) {
        NBTTagCompound serialized = new NBTTagCompound();
        serialized.setString(KEY_ITEM, itemName);
        serialized.setInteger(KEY_DAMAGE, damage);
        if (!tag.isEmpty()) {
            serialized.setTag(KEY_TAG, tag.copyTag());
        }
        serialized.setLong(KEY_AMOUNT, amount);
        return serialized;
    }

    public ItemStack toStack(long amount) {
        GameRegistry.UniqueIdentifier id;
        try {
            id = new GameRegistry.UniqueIdentifier(itemName);
        } catch (RuntimeException ignored) {
            return null;
        }
        net.minecraft.item.Item item = GameRegistry.findItem(id.modId, id.name);
        if (item == null) {
            return null;
        }
        ItemStack stack = new ItemStack(item, saturatedInt(amount), damage);
        if (!tag.isEmpty()) {
            stack.stackTagCompound = tag.copyTag();
        }
        stack.stackSize = saturatedInt(amount);
        return stack;
    }

    public String getItemName() {
        return itemName;
    }

    public int getDamage() {
        return damage;
    }

    public NbtKey getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemStackKey)) {
            return false;
        }
        ItemStackKey that = (ItemStackKey) o;
        return damage == that.damage && Objects.equals(itemName, that.itemName) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemName, damage, tag);
    }

    @Override
    public String toString() {
        return "ItemStackKey{" + itemName + ':' + damage + ", tag=" + tag + '}';
    }

    private static int saturatedInt(long amount) {
        if (amount > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (amount < 0L) {
            return 0;
        }
        return (int) amount;
    }
}
