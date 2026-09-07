package cn.dancingsnow.aeinfinitycell.ae;

import java.util.Map;

import net.minecraft.item.ItemStack;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IItemList;
import cn.dancingsnow.aeinfinitycell.storage.CellCount;
import cn.dancingsnow.aeinfinitycell.storage.EssentiaStackKey;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import thaumicenergistics.common.storage.AEEssentiaStack;
import thaumicenergistics.common.storage.AEEssentiaStackType;

public final class InfinityEssentiaInventoryHandler extends AbstractInfinityInventoryHandler<AEEssentiaStack> {

    public InfinityEssentiaInventoryHandler(ItemStack cellStack, ISaveProvider saveProvider) {
        super(cellStack, saveProvider, AEEssentiaStackType.ESSENTIA_STACK_TYPE, null, ICellCacheRegistry.TYPE.ESSENTIA);
    }

    @Override
    protected void add(InfinityCellRecord record, AEEssentiaStack input, long amount) {
        record.addEssentia(EssentiaStackKey.from(input), amount);
    }

    @Override
    protected long extract(InfinityCellRecord record, AEEssentiaStack request, long amount, boolean modulate) {
        return record.extractEssentia(EssentiaStackKey.from(request), amount, modulate);
    }

    @Override
    protected long amount(InfinityCellRecord record, AEEssentiaStack request) {
        return record.getEssentiaAmount(EssentiaStackKey.from(request));
    }

    @Override
    protected void addAvailable(InfinityCellRecord record, IItemList<AEEssentiaStack> out) {
        for (Map.Entry<EssentiaStackKey, CellCount> entry : record.getEssentiaView()
            .entrySet()) {
            AEEssentiaStack stack = entry.getKey()
                .toStack(
                    entry.getValue()
                        .longValue());
            if (stack != null) {
                out.addStorage(stack);
            }
        }
    }

    @Override
    protected long usedTypes(InfinityCellRecord record) {
        return record.getUsedEssentiaTypes();
    }
}
