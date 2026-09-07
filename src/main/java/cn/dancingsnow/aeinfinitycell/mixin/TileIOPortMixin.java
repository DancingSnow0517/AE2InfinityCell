package cn.dancingsnow.aeinfinitycell.mixin;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.api.AEApi;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.Settings;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.AEStackTypeRegistry;
import appeng.api.storage.data.IAEStackType;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.storage.TileIOPort;
import appeng.util.ConfigManager;
import cn.dancingsnow.aeinfinitycell.ae.AbstractInfinityInventoryHandler;
import cn.dancingsnow.aeinfinitycell.item.ItemInfinityStorageCell;

/**
 * AE2 的 IO 端口每 tick 只取存储元件第一个可用通道的库存（见 TileIOPort#getInv），
 * 多通道的无限存储单元因此只会传输物品，流体等通道永远轮不到。
 * 这里让 IO 端口在单元的各通道间逐 tick 轮询，并把"搬空/抽空"的完成判定扩展到全部通道。
 */
@Mixin(value = TileIOPort.class, remap = false)
public abstract class TileIOPortMixin {

    @Shadow
    private ItemStack currentCell;

    @Shadow
    private IMEInventory<?> cachedInventory;

    @Shadow
    @Final
    private ConfigManager manager;

    @Unique
    private int aeinfinitycell$channelRotation;

    @Inject(method = "getInv", at = @At("HEAD"), cancellable = true)
    private void aeinfinitycell$rotateChannels(ItemStack is, CallbackInfoReturnable<IMEInventory<?>> cir) {
        if (is == null || !(is.getItem() instanceof ItemInfinityStorageCell)) {
            return;
        }
        if (this.currentCell != is) {
            this.currentCell = is;
            this.aeinfinitycell$channelRotation = 0;
        }

        List<IAEStackType<?>> types = new ArrayList<>(AEStackTypeRegistry.getAllTypes());
        int typeCount = types.size();
        if (typeCount == 0) {
            return;
        }

        boolean emptying = (OperationMode) this.manager.getSetting(Settings.OPERATION_MODE) == OperationMode.EMPTY;
        for (int i = 0; i < typeCount; i++) {
            int index = (this.aeinfinitycell$channelRotation + i) % typeCount;
            IMEInventory<?> inv = aeinfinitycell$channelInventory(is, types.get(index));
            if (!(inv instanceof AbstractInfinityInventoryHandler<?>handler)) {
                continue;
            }
            // 搬空模式下跳过已无内容的通道，避免 tick 浪费在空通道上
            if (emptying && handler.getUsedTypes() == 0L) {
                continue;
            }
            this.aeinfinitycell$channelRotation = (index + 1) % typeCount;
            this.cachedInventory = inv;
            cir.setReturnValue(inv);
            return;
        }

        // 搬空模式下全部通道都已为空：交给第一个通道，让 matches 判定后把元件弹到输出槽
        IMEInventory<?> fallback = aeinfinitycell$channelInventory(is, types.get(0));
        this.cachedInventory = fallback;
        cir.setReturnValue(fallback);
    }

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void aeinfinitycell$matchesAllChannels(FullnessMode fm, OperationMode om, IMEInventory<?> src,
        boolean didWork, CallbackInfoReturnable<Boolean> cir) {
        if (!(src instanceof AbstractInfinityInventoryHandler<?>handler)) {
            return;
        }
        if (fm == FullnessMode.EMPTY && om == OperationMode.EMPTY) {
            // 所有通道都搬空后才算"已空"，而不是只看当前轮到的通道
            cir.setReturnValue(aeinfinitycell$cellDrained(handler.getCellStack()));
        }
    }

    @Inject(method = "shouldMove", at = @At("HEAD"), cancellable = true)
    private void aeinfinitycell$shouldMoveAllChannels(IMEInventory<?> inventory, boolean sourceEmptyAfterTransfer,
        boolean destinationFull, boolean didWork, boolean moveOnEmptyWhileFilling, OperationMode om, FullnessMode fm,
        CallbackInfoReturnable<Boolean> cir) throws GridAccessException {
        if (!(inventory instanceof AbstractInfinityInventoryHandler<?>)) {
            return;
        }
        if (moveOnEmptyWhileFilling && didWork) {
            // 填充模式下所有通道的网络侧都抽空后才弹出元件
            cir.setReturnValue(destinationFull || aeinfinitycell$networkDrained());
        }
    }

    @Unique
    private boolean aeinfinitycell$cellDrained(ItemStack cellStack) {
        for (IAEStackType<?> type : AEStackTypeRegistry.getAllTypes()) {
            IMEInventory<?> inv = aeinfinitycell$channelInventory(cellStack, type);
            if (inv instanceof AbstractInfinityInventoryHandler<?>handler && handler.getUsedTypes() > 0L) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private boolean aeinfinitycell$networkDrained() throws GridAccessException {
        AENetworkInvTile self = (AENetworkInvTile) (Object) this;
        for (IAEStackType<?> type : AEStackTypeRegistry.getAllTypes()) {
            IMEMonitor<?> monitor = self.getProxy()
                .getStorage()
                .getMEMonitor(type);
            if (monitor != null && !monitor.getStorageList()
                .isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private IMEInventory<?> aeinfinitycell$channelInventory(ItemStack cell, IAEStackType<?> type) {
        return AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(cell, null, type);
    }
}
