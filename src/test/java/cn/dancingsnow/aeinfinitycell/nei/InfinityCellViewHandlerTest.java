package cn.dancingsnow.aeinfinitycell.nei;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.Test;

import cn.dancingsnow.aeinfinitycell.AEInfinityCell;
import cn.dancingsnow.aeinfinitycell.nei.InfinityCellViewPreview.Channel;
import cn.dancingsnow.aeinfinitycell.nei.InfinityCellViewPreview.Entry;
import cn.dancingsnow.aeinfinitycell.storage.FluidStackKey;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import gregtech.nei.FluidDisplayStackMode;

public class InfinityCellViewHandlerTest {

    @Test
    public void handlerIdUsesClassNameLikeAe2NeiHandlers() {
        assertEquals(NEIHandlerInfoRegistration.HANDLER, new InfinityCellViewHandler().getHandlerId());
    }

    @Test
    public void handlerInfoImcUsesInfinityCellIconAndHasNoOverlayButton() {
        NBTTagCompound tag = NEIHandlerInfoRegistration.createCellViewHandlerInfo();

        assertEquals(NEIHandlerInfoRegistration.HANDLER, tag.getString("handler"));
        assertEquals("AE2 Infinity Cell", tag.getString("modName"));
        assertEquals(AEInfinityCell.MODID, tag.getString("modId"));
        assertEquals(NEIHandlerInfoRegistration.ITEM_NAME, tag.getString("itemName"));
        assertEquals(160, tag.getInteger("handlerHeight"));
        assertFalse(tag.getBoolean("multipleWidgetsAllowed"));
        assertFalse(tag.getBoolean("showFavoritesButton"));
        assertFalse(tag.getBoolean("showOverlayButton"));
        assertFalse(tag.getBoolean("showBadge"));
    }

    @Test
    public void fluidCarrierStackUsesOneItemAndDrawnAmountUsesStoredAmount() {
        InfinityCellRecord record = new InfinityCellRecord();
        record.addFluid(fluidKey("water"), 64000L);
        List<Entry<FluidStackKey>> fluids = InfinityCellViewPreview.fluids(record, 63);

        assertEquals(1L, InfinityCellViewHandler.getDisplayStackSize(Channel.FLUIDS, fluids.get(0)));
        assertEquals(64000L, InfinityCellViewHandler.getDrawnStackSize(Channel.FLUIDS, fluids.get(0)));
    }

    @Test
    public void typeCountLineShowsTotalTypesOnly() {
        assertEquals("1,234 Types", InfinityCellViewHandler.formatTypeCountLine(1234L, "Types"));
    }

    @Test
    public void fluidCarrierStackUsesShownGregTechDisplayAndHidesRendererStackSize() {
        assertEquals(FluidDisplayStackMode.SHOWN, InfinityCellViewHandler.getFluidDisplayStackMode());
        ItemStack displayStack = new ItemStack(new Item(), 64);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("mFluidDisplayAmount", 64000L);
        displayStack.setTagCompound(tag);

        ItemStack stack = InfinityCellViewHandler.prepareFluidDisplayStack(displayStack);

        assertEquals(displayStack, stack);
        assertEquals(1, stack.stackSize);
        assertTrue(stack.hasTagCompound());
        assertEquals(
            64000L,
            stack.getTagCompound()
                .getLong("mFluidDisplayAmount"));
        assertTrue(
            stack.getTagCompound()
                .getBoolean("mHideStackSize"));
    }

    @Test
    public void fluidSlotsAreDrawnInForegroundToCoverNeiCarrierStack() {
        assertFalse(InfinityCellViewHandler.drawSlotsInBackground(Channel.FLUIDS));
    }

    private static FluidStackKey fluidKey(String id) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", id);
        return FluidStackKey.readFromNBT(tag);
    }
}
