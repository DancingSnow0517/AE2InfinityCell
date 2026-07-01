package cn.dancingsnow.aeinfinitycell.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cn.dancingsnow.aeinfinitycell.AEInfinityCell;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellDataAccess;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;

public final class ItemInfinityStorageCell extends Item {

    private static final String TAG_ROOT = AEInfinityCell.MODID;
    private static final String TAG_STORAGE_ID = "storageId";

    public ItemInfinityStorageCell() {
        setUnlocalizedName(AEInfinityCell.MODID + ".infinity_storage_cell");
        setTextureName(AEInfinityCell.MODID + ":infinity_storage_cell");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public void addInformation(ItemStack stack, net.minecraft.entity.player.EntityPlayer player, List<String> tooltip,
        boolean advanced) {
        UUID id = getStorageId(stack);
        tooltip.add(StatCollector.translateToLocal("tooltip.aeinfinitycell.infinity_storage_cell"));
        if (id != null && advanced) {
            tooltip.add(id.toString());
        }
    }

    public static UUID getStorageId(ItemStack stack) {
        NBTTagCompound root = getRootTag(stack, false);
        if (root == null || !root.hasKey(TAG_STORAGE_ID)) {
            return null;
        }
        try {
            return UUID.fromString(root.getString(TAG_STORAGE_ID));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static UUID getOrCreateStorageId(ItemStack stack) {
        UUID existing = getStorageId(stack);
        if (existing != null) {
            return existing;
        }

        UUID created = UUID.randomUUID();
        getRootTag(stack, true).setString(TAG_STORAGE_ID, created.toString());
        return created;
    }

    public static InfinityCellRecord getRecord(ItemStack stack, World world) {
        if (stack == null || !(stack.getItem() instanceof ItemInfinityStorageCell)) {
            return null;
        }

        UUID id = getOrCreateStorageId(stack);
        return InfinityCellDataAccess.getOrCreate(id, world);
    }

    public static void markDirty(ItemStack stack, World world) {
        UUID id = getStorageId(stack);
        if (id == null) {
            return;
        }
        InfinityCellDataAccess.markDirty(id, world);
    }

    private static NBTTagCompound getRootTag(ItemStack stack, boolean create) {
        if (stack == null) {
            return null;
        }
        if (stack.stackTagCompound == null) {
            if (!create) {
                return null;
            }
            stack.stackTagCompound = new NBTTagCompound();
        }
        if (!stack.stackTagCompound.hasKey(TAG_ROOT, 10)) {
            if (!create) {
                return null;
            }
            stack.stackTagCompound.setTag(TAG_ROOT, new NBTTagCompound());
        }
        return stack.stackTagCompound.getCompoundTag(TAG_ROOT);
    }
}
