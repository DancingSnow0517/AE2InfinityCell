package cn.dancingsnow.aeinfinitycell.ae;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStackType;
import cn.dancingsnow.aeinfinitycell.item.ItemInfinityStorageCell;

public final class InfinityCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() instanceof ItemInfinityStorageCell;
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack is, ISaveProvider host, IAEStackType<?> type) {
        if (!isCell(is)) {
            return null;
        }
        if (type == AEItemStackType.ITEM_STACK_TYPE) {
            return new InfinityItemInventoryHandler(is, host);
        }
        if (type == AEFluidStackType.FLUID_STACK_TYPE) {
            return new InfinityFluidInventoryHandler(is, host);
        }
        return null;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return null;
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return null;
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return null;
    }

    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler,
        IMEInventoryHandler inv, ItemStack is, StorageChannel chan) {}

    @Override
    public int getStatusForCell(ItemStack is, IMEInventory handler) {
        if (handler instanceof AbstractInfinityInventoryHandler) {
            return ((AbstractInfinityInventoryHandler) handler).getCellStatus();
        }
        return 1;
    }

    @Override
    public double cellIdleDrain(ItemStack is, IMEInventory handler) {
        return 0.5D;
    }
}
