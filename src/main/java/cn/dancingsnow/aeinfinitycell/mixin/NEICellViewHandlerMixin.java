package cn.dancingsnow.aeinfinitycell.mixin;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.integration.modules.NEIHelpers.NEICellViewHandler;
import cn.dancingsnow.aeinfinitycell.item.ItemInfinityStorageCell;
import codechicken.nei.recipe.IUsageHandler;

/**
 * 无限存储元件自带 NEI 视图（InfinityCellViewHandler），支持滚动分页和全部通道。
 * AE2 内置的 NEICellViewHandler 只按 9 列画固定槽位网格，且把 getTotalTypes()
 * 当循环上限用，无限语义会撑爆 Tessellator 导致 OOM；这里让它对无限元件直接返回 null。
 */
@Mixin(value = NEICellViewHandler.class, remap = false)
public abstract class NEICellViewHandlerMixin {

    @Inject(method = "getUsageHandler", at = @At("HEAD"), cancellable = true)
    private void aeinfinitycell$skipBuiltinCellView(String inputId, Object[] ingredients,
        CallbackInfoReturnable<IUsageHandler> cir) {
        if (ingredients != null && ingredients.length > 0
            && ingredients[0] instanceof ItemStack stack
            && stack.getItem() instanceof ItemInfinityStorageCell) {
            cir.setReturnValue(null);
        }
    }
}
