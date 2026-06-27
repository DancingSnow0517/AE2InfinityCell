package cn.dancingsnow.aeinfinitycell.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStackType;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.storage.TileDrive;
import appeng.util.item.AEFluidStackType;
import appeng.util.item.AEItemStackType;
import cn.dancingsnow.aeinfinitycell.ae.AbstractInfinityInventoryHandler;
import cn.dancingsnow.aeinfinitycell.item.ItemInfinityStorageCell;
import thaumicenergistics.common.storage.AEEssentiaStackType;

@Mixin(value = TileDrive.class, remap = false)
public abstract class TileDriveMixin {

    @Shadow
    @Final
    private AppEngInternalInventory inv;

    @Shadow
    @Final
    private ICellHandler[] handlersBySlot;

    @Shadow
    @Final
    @SuppressWarnings("rawtypes")
    private Map<IAEStackType<?>, List<IMEInventoryHandler>> cellsMap;

    @Shadow
    private int priority;

    @Inject(method = "updateState", at = @At("RETURN"))
    private void aeinfinitycell$mountDualChannelInventories(CallbackInfo ci) {
        for (int slot = 0; slot < this.inv.getSizeInventory(); slot++) {
            ItemStack stack = this.inv.getStackInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof ItemInfinityStorageCell)) {
                continue;
            }

            ICellHandler cellHandler = this.handlersBySlot[slot];
            if (cellHandler == null) {
                continue;
            }

            this.aeinfinitycell$mountMissingHandler(stack, cellHandler, AEItemStackType.ITEM_STACK_TYPE);
            this.aeinfinitycell$mountMissingHandler(stack, cellHandler, AEFluidStackType.FLUID_STACK_TYPE);
            this.aeinfinitycell$mountMissingHandler(stack, cellHandler, AEEssentiaStackType.ESSENTIA_STACK_TYPE);
        }
    }

    private void aeinfinitycell$mountMissingHandler(ItemStack stack, ICellHandler cellHandler, IAEStackType<?> type) {
        List<IMEInventoryHandler> handlers = this.cellsMap.get(type);
        if (handlers == null) {
            handlers = new ArrayList<>();
            this.cellsMap.put(type, handlers);
        }
        if (this.aeinfinitycell$hasMountedHandler(handlers, stack)) {
            return;
        }

        IMEInventoryHandler cell = cellHandler.getCellInventory(stack, (ISaveProvider) (Object) this, type);
        if (cell == null) {
            return;
        }

        MEInventoryHandler mounted = new MEInventoryHandler(cell, cell.getStackType());
        mounted.setPriority(this.priority);
        handlers.add(mounted);
    }

    private boolean aeinfinitycell$hasMountedHandler(List<IMEInventoryHandler> handlers, ItemStack stack) {
        for (IMEInventoryHandler handler : handlers) {
            if (this.aeinfinitycell$isHandlerForStack(handler, stack)) {
                return true;
            }
            if (handler instanceof MEInventoryHandler) {
                IMEInventory internal = ((MEInventoryHandler) handler).getInternal();
                if (internal instanceof IMEInventoryHandler
                    && this.aeinfinitycell$isHandlerForStack((IMEInventoryHandler) internal, stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean aeinfinitycell$isHandlerForStack(IMEInventoryHandler handler, ItemStack stack) {
        if (handler instanceof AbstractInfinityInventoryHandler) {
            return ((AbstractInfinityInventoryHandler) handler).isForCellStack(stack);
        }
        return false;
    }
}
