package cn.dancingsnow.aeinfinitycell.nei;

import net.minecraft.nbt.NBTTagCompound;

import cn.dancingsnow.aeinfinitycell.AEInfinityCell;
import cpw.mods.fml.common.event.FMLInterModComms;

public final class NEIHandlerInfoRegistration {

    static final String HANDLER = "cn.dancingsnow.aeinfinitycell.nei.InfinityCellViewHandler";
    static final String ITEM_NAME = AEInfinityCell.MODID + ":infinity_storage_cell";

    private NEIHandlerInfoRegistration() {}

    public static void sendCellViewHandlerInfo() {
        FMLInterModComms.sendMessage("NotEnoughItems", "registerHandlerInfo", createCellViewHandlerInfo());
    }

    static NBTTagCompound createCellViewHandlerInfo() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("handler", HANDLER);
        tag.setString("modName", "AE2 Infinity Cell");
        tag.setString("modId", AEInfinityCell.MODID);
        tag.setBoolean("modRequired", true);
        tag.setString("itemName", ITEM_NAME);
        tag.setInteger("yShift", 0);
        tag.setInteger("handlerHeight", 160);
        tag.setBoolean("multipleWidgetsAllowed", false);
        tag.setBoolean("showFavoritesButton", false);
        tag.setBoolean("showOverlayButton", false);
        tag.setBoolean("showBadge", false);
        return tag;
    }
}
