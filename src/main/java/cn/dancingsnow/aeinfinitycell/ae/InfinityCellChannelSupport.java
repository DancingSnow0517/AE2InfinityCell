package cn.dancingsnow.aeinfinitycell.ae;

import net.minecraft.item.ItemStack;

import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStackType;

public interface InfinityCellChannelSupport {

    IAEStackType<?> getStackType();

    IMEInventoryHandler<?> createInventory(ItemStack cellStack, ISaveProvider saveProvider);
}
