package cn.dancingsnow.aeinfinitycell.ae;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.tile.inventory.AppEngNullInventory;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;

/**
 * {@link ICellInventory} view of an infinity storage cell. Content access is delegated to the record hooks of the
 * owning {@link AbstractInfinityInventoryHandler}, so the cell behaves like a regular AE2 storage cell for network
 * scans, crafting simulation and NEI queries.
 */
public final class InfinityCellInventory<T extends IAEStack<T>> implements ICellInventory<T> {

    private final ItemStack cellStack;
    private final IAEStackType<T> stackType;
    private final StorageChannel channel;
    private final IInventory emptyInventory = new AppEngNullInventory();

    private AbstractInfinityInventoryHandler<T> handler;

    InfinityCellInventory(ItemStack cellStack, IAEStackType<T> stackType, StorageChannel channel) {
        this.cellStack = cellStack;
        this.stackType = stackType;
        this.channel = channel;
    }

    void setHandler(AbstractInfinityInventoryHandler<T> handler) {
        this.handler = handler;
    }

    @Override
    public T injectItems(T input, Actionable type, BaseActionSource src) {
        if (input == null || input.getStackSize() <= 0L) {
            return null;
        }

        InfinityCellRecord record = record();
        if (record == null) {
            return input;
        }

        if (type == Actionable.MODULATE) {
            handler.add(record, input, input.getStackSize());
            handler.markChanged();
        }
        return null;
    }

    @Override
    public T extractItems(T request, Actionable mode, BaseActionSource src) {
        if (request == null || request.getStackSize() <= 0L) {
            return null;
        }

        InfinityCellRecord record = record();
        if (record == null) {
            return null;
        }

        long extracted = handler.extract(record, request, request.getStackSize(), mode == Actionable.MODULATE);
        if (extracted <= 0L) {
            return null;
        }

        if (mode == Actionable.MODULATE) {
            handler.markChanged();
        }
        return handler.copyWithSize(request, extracted);
    }

    @Override
    public IItemList<T> getAvailableItems(IItemList<T> out, int iteration) {
        InfinityCellRecord record = record();
        if (record != null) {
            handler.addAvailable(record, out);
        }
        return out;
    }

    @Override
    public T getAvailableItem(@Nonnull T request, int iteration) {
        if (request == null) {
            return null;
        }

        InfinityCellRecord record = record();
        if (record == null) {
            return null;
        }

        long amount = handler.amount(record, request);
        return amount <= 0L ? null : handler.copyWithSize(request, amount);
    }

    @Override
    public StorageChannel getChannel() {
        return channel;
    }

    @Override
    public IAEStackType<?> getStackType() {
        return stackType;
    }

    @Override
    public ItemStack getItemStack() {
        return cellStack;
    }

    @Override
    public double getIdleDrain() {
        return 0.5D;
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return FuzzyMode.IGNORE_ALL;
    }

    @Deprecated
    @Override
    public IInventory getConfigInventory() {
        return emptyInventory;
    }

    @Override
    public IInventory getUpgradesInventory() {
        return emptyInventory;
    }

    @Override
    public int getBytesPerType() {
        return 0;
    }

    @Override
    public boolean canHoldNewItem() {
        return true;
    }

    @Override
    public long getTotalBytes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getFreeBytes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getUsedBytes() {
        return 0L;
    }

    @Override
    public long getTotalItemTypes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getStoredItemCount() {
        InfinityCellRecord record = record();
        return record == null ? 0L : handler.storedUnits(record);
    }

    @Override
    public long getStoredItemTypes() {
        InfinityCellRecord record = record();
        return record == null ? 0L : handler.usedTypes(record);
    }

    @Override
    public long getRemainingItemTypes() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getRemainingItemCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getRemainingItemsCountDist(T stack) {
        return Long.MAX_VALUE;
    }

    @Override
    public int getUnusedItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getStatusForCell() {
        return getStoredItemTypes() == 0L ? 1 : 2;
    }

    @Override
    public String getOreFilter() {
        return "";
    }

    private InfinityCellRecord record() {
        return handler == null ? null : handler.record();
    }
}
