package cn.dancingsnow.aeinfinitycell.ae;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.CellInventoryHandler;
import cn.dancingsnow.aeinfinitycell.ServerWorldAccess;
import cn.dancingsnow.aeinfinitycell.item.ItemInfinityStorageCell;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;

/**
 * Base handler for infinity storage cells. It is a real {@link CellInventoryHandler} backed by an
 * {@link InfinityCellInventory}, so network scans, crafting simulation, drive mounting and NEI queries all go through
 * AE2's standard cell pipeline.
 */
public abstract class AbstractInfinityInventoryHandler<T extends IAEStack<T>> extends CellInventoryHandler<T> {

    private final ItemStack cellStack;
    private final ISaveProvider saveProvider;
    private final IAEStackType<T> stackType;
    private final StorageChannel channel;
    private final TYPE cellType;

    protected AbstractInfinityInventoryHandler(ItemStack cellStack, ISaveProvider saveProvider,
        IAEStackType<T> stackType, StorageChannel channel, TYPE cellType) {
        this(
            new InfinityCellInventory<>(cellStack, stackType, channel),
            cellStack,
            saveProvider,
            stackType,
            channel,
            cellType);
    }

    private AbstractInfinityInventoryHandler(InfinityCellInventory<T> cellInventory, ItemStack cellStack,
        ISaveProvider saveProvider, IAEStackType<T> stackType, StorageChannel channel, TYPE cellType) {
        super(cellInventory, stackType);
        cellInventory.setHandler(this);
        this.cellStack = cellStack;
        this.saveProvider = saveProvider;
        this.stackType = stackType;
        this.channel = channel;
        this.cellType = cellType;
    }

    @Override
    public final StorageChannel getChannel() {
        return channel;
    }

    @Override
    public final IAEStackType<?> getStackType() {
        return stackType;
    }

    @Override
    public final StorageChannel getStorageChannel() {
        return channel;
    }

    @Override
    public final TYPE getCellType() {
        return cellType;
    }

    protected final T copyWithSize(T stack, long amount) {
        T copy = stack.copy();
        copy.setStackSize(amount);
        return copy;
    }

    public final boolean isForCellStack(ItemStack stack) {
        return this.cellStack == stack;
    }

    public final ItemStack getCellStack() {
        return cellStack;
    }

    protected final InfinityCellRecord record() {
        World world = ServerWorldAccess.getServerWorld();
        return ItemInfinityStorageCell.getRecord(cellStack, world);
    }

    protected final void markChanged() {
        World world = ServerWorldAccess.getServerWorld();
        ItemInfinityStorageCell.markDirty(cellStack, world);
        if (saveProvider != null) {
            saveProvider.saveChanges(this);
        }
    }

    protected abstract void add(InfinityCellRecord record, T input, long amount);

    protected abstract long extract(InfinityCellRecord record, T request, long amount, boolean modulate);

    protected abstract long amount(InfinityCellRecord record, T request);

    protected abstract void addAvailable(InfinityCellRecord record, IItemList<T> out);

    protected abstract long usedTypes(InfinityCellRecord record);

    protected abstract long storedUnits(InfinityCellRecord record);
}
