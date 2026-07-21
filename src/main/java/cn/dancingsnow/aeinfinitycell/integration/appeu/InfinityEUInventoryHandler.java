package cn.dancingsnow.aeinfinitycell.integration.appeu;

import net.minecraft.item.ItemStack;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IItemList;
import cn.dancingsnow.aeinfinitycell.ae.AbstractInfinityInventoryHandler;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import cn.dancingsnow.appeu.storage.EUStack;
import cn.dancingsnow.appeu.storage.EUStackType;

public final class InfinityEUInventoryHandler extends AbstractInfinityInventoryHandler<EUStack> {

    public InfinityEUInventoryHandler(ItemStack cellStack, ISaveProvider saveProvider) {
        super(cellStack, saveProvider, EUStackType.INSTANCE, null, ICellCacheRegistry.TYPE.ITEM);
    }

    @Override
    protected void add(InfinityCellRecord record, EUStack input, long amount) {
        record.addEU(amount);
    }

    @Override
    protected long extract(InfinityCellRecord record, EUStack request, long amount, boolean modulate) {
        long extracted = Math.min(record.getEUAmount(), amount);
        if (modulate && extracted > 0L) {
            record.removeEU(extracted);
        }
        return extracted;
    }

    @Override
    protected long amount(InfinityCellRecord record, EUStack request) {
        return record.getEUAmount();
    }

    @Override
    protected void addAvailable(InfinityCellRecord record, IItemList<EUStack> out) {
        long amount = record.getEUAmount();
        if (amount > 0L) {
            out.addStorage(new EUStack(amount));
        }
    }

    @Override
    protected long usedTypes(InfinityCellRecord record) {
        return record.getUsedEUTypes();
    }
}
