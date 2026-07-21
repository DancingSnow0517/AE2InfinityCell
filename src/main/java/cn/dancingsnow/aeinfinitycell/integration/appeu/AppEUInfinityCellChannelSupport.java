package cn.dancingsnow.aeinfinitycell.integration.appeu;

import net.minecraft.item.ItemStack;

import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStackType;
import cn.dancingsnow.aeinfinitycell.ae.InfinityCellChannelSupport;
import cn.dancingsnow.appeu.storage.EUStackType;

public final class AppEUInfinityCellChannelSupport implements InfinityCellChannelSupport {

    @Override
    public IAEStackType<?> getStackType() {
        return EUStackType.INSTANCE;
    }

    @Override
    public IMEInventoryHandler<?> createInventory(ItemStack cellStack, ISaveProvider saveProvider) {
        return new InfinityEUInventoryHandler(cellStack, saveProvider);
    }
}
