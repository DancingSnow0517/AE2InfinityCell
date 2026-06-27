package cn.dancingsnow.aeinfinitycell.ae;

import java.util.Map;

import net.minecraft.item.ItemStack;

import appeng.api.storage.ICellCacheRegistry;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEItemStackType;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import cn.dancingsnow.aeinfinitycell.storage.ItemStackKey;

public final class InfinityItemInventoryHandler extends AbstractInfinityInventoryHandler<IAEItemStack> {

    public InfinityItemInventoryHandler(ItemStack cellStack, ISaveProvider saveProvider) {
        super(
            cellStack,
            saveProvider,
            AEItemStackType.ITEM_STACK_TYPE,
            StorageChannel.ITEMS,
            ICellCacheRegistry.TYPE.ITEM);
    }

    @Override
    protected void add(InfinityCellRecord record, IAEItemStack input, long amount) {
        ItemStackKey key = key(input);
        if (key != null) {
            record.addItem(key, amount);
        }
    }

    @Override
    protected long extract(InfinityCellRecord record, IAEItemStack request, long amount, boolean modulate) {
        ItemStackKey key = key(request);
        if (key == null) {
            return 0L;
        }
        long available = record.getItemAmount(key);
        long extracted = Math.min(available, amount);
        if (modulate && extracted > 0L) {
            record.removeItem(key, extracted);
        }
        return extracted;
    }

    @Override
    protected long amount(InfinityCellRecord record, IAEItemStack request) {
        ItemStackKey key = key(request);
        return key == null ? 0L : record.getItemAmount(key);
    }

    @Override
    protected void addAvailable(InfinityCellRecord record, IItemList<IAEItemStack> out) {
        for (Map.Entry<ItemStackKey, Long> entry : record.getItemsView()
            .entrySet()) {
            ItemStack stack = entry.getKey()
                .toStack(
                    entry.getValue()
                        .longValue());
            if (stack == null) {
                continue;
            }
            IAEItemStack aeStack = AEItemStack.create(stack);
            if (aeStack != null) {
                aeStack.setStackSize(
                    entry.getValue()
                        .longValue());
                out.addStorage(aeStack);
            }
        }
    }

    @Override
    protected long usedTypes(InfinityCellRecord record) {
        return record.getUsedItemTypes();
    }

    private static ItemStackKey key(IAEItemStack stack) {
        try {
            return ItemStackKey.from(stack.getItemStack());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
