package cn.dancingsnow.aeinfinitycell.nei;

import static net.minecraft.util.EnumChatFormatting.GRAY;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import appeng.api.config.TerminalFontSize;
import appeng.client.render.StackSizeRenderer;
import appeng.core.localization.GuiText;
import cn.dancingsnow.aeinfinitycell.Config;
import cn.dancingsnow.aeinfinitycell.item.ItemInfinityStorageCell;
import cn.dancingsnow.aeinfinitycell.nei.InfinityCellViewPreview.Channel;
import cn.dancingsnow.aeinfinitycell.nei.InfinityCellViewPreview.Entry;
import cn.dancingsnow.aeinfinitycell.nei.InfinityCellViewPreview.Page;
import cn.dancingsnow.aeinfinitycell.storage.EssentiaStackKey;
import cn.dancingsnow.aeinfinitycell.storage.FluidStackKey;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellDataAccess;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import cn.dancingsnow.aeinfinitycell.storage.ItemStackKey;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IUsageHandler;
import gregtech.api.util.GTUtility;
import gregtech.nei.FluidDisplayStackMode;
import thaumicenergistics.common.storage.AEEssentiaStack;

public final class InfinityCellViewHandler implements IUsageHandler {

    private static final ResourceLocation SLOT_TEXTURE_LOCATION = new ResourceLocation("nei", "textures/slot.png");
    private static final int OFFSET_X = 2;
    private static final int INFO_OFFSET_Y = 4;
    private static final int ITEMS_OFFSET_Y = 28;
    private static final int ROW_ITEM_NUM = 9;
    private static final String HIDE_FLUID_DISPLAY_STACK_SIZE_TAG = "mHideStackSize";

    private final List<ViewPage> pages = new ArrayList<ViewPage>();

    @Override
    public IUsageHandler getUsageHandler(String inputId, Object... ingredients) {
        if (ingredients.length == 0 || !(ingredients[0] instanceof ItemStack)) {
            return null;
        }

        ItemStack ingredient = (ItemStack) ingredients[0];
        if (!(ingredient.getItem() instanceof ItemInfinityStorageCell)) {
            return null;
        }

        UUID id = ItemInfinityStorageCell.getStorageId(ingredient);
        if (id == null) {
            return null;
        }

        InfinityCellRecord record = InfinityCellDataAccess.getOrCreate(id, null);
        if (record == null) {
            return null;
        }

        InfinityCellViewHandler handler = new InfinityCellViewHandler();
        for (Page page : InfinityCellViewPreview.pages(record, Config.neiPreviewEntriesPerChannel)) {
            handler.addPage(page);
        }
        return handler.pages.isEmpty() ? null : handler;
    }

    private void addPage(Page page) {
        List<ViewItemStack> stacks = new ArrayList<ViewItemStack>();
        for (Entry<?> entry : page.getEntries()) {
            ViewItemStack viewStack = createViewStack(page.getChannel(), entry, stacks.size());
            if (viewStack != null) {
                stacks.add(viewStack);
            }
        }
        if (!stacks.isEmpty()) {
            pages.add(new ViewPage(page.getChannel(), stacks, page.getTotalTypes()));
        }
    }

    private ViewItemStack createViewStack(Channel channel, Entry<?> entry, int index) {
        ItemStack displayStack = createDisplayStack(channel, entry);
        if (displayStack == null) {
            return null;
        }
        displayStack.stackSize = 1;
        PositionedStack positionedStack = new PositionedStack(
            displayStack,
            OFFSET_X + index % ROW_ITEM_NUM * 18 + 1,
            ITEMS_OFFSET_Y + index / ROW_ITEM_NUM * 18 + 1,
            false);
        return new ViewItemStack(
            positionedStack,
            entry.getAmount(),
            getDrawnStackSize(channel, entry),
            getFluidIcon(channel, entry),
            getFluidColor(channel, entry));
    }

    static ItemStack createDisplayStack(Channel channel, Entry<?> entry) {
        if (channel == Channel.ITEMS) {
            return ((ItemStackKey) entry.getKey()).toStack(1L);
        }
        if (channel == Channel.FLUIDS) {
            FluidStack fluidStack = ((FluidStackKey) entry.getKey()).toStack(entry.getStackSize());
            return createFluidDisplayStack(fluidStack);
        }
        if (channel == Channel.ESSENTIA) {
            AEEssentiaStack essentiaStack = ((EssentiaStackKey) entry.getKey()).toStack(1L);
            return essentiaStack == null ? null : essentiaStack.getItemStackForNEI();
        }
        return null;
    }

    static ItemStack createFluidDisplayStack(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return null;
        }
        return prepareFluidDisplayStack(GTUtility.getFluidDisplayStack(fluidStack, getFluidDisplayStackMode()));
    }

    static FluidDisplayStackMode getFluidDisplayStackMode() {
        return FluidDisplayStackMode.SHOWN;
    }

    static ItemStack prepareFluidDisplayStack(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        stack.stackSize = 1;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound()
            .setBoolean(HIDE_FLUID_DISPLAY_STACK_SIZE_TAG, true);
        return stack;
    }

    static long getDisplayStackSize(Channel channel, Entry<?> entry) {
        return 1L;
    }

    static long getDrawnStackSize(Channel channel, Entry<?> entry) {
        return entry.getStackSize();
    }

    private static IIcon getFluidIcon(Channel channel, Entry<?> entry) {
        FluidStack fluidStack = channel == Channel.FLUIDS
            ? ((FluidStackKey) entry.getKey()).toStack(entry.getStackSize())
            : null;
        return fluidStack == null || fluidStack.getFluid() == null ? null
            : fluidStack.getFluid()
                .getIcon();
    }

    private static int getFluidColor(Channel channel, Entry<?> entry) {
        FluidStack fluidStack = channel == Channel.FLUIDS
            ? ((FluidStackKey) entry.getKey()).toStack(entry.getStackSize())
            : null;
        return fluidStack == null || fluidStack.getFluid() == null ? 0xFFFFFF
            : fluidStack.getFluid()
                .getColor();
    }

    private static int saturatedInt(long amount) {
        if (amount > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (amount < 0L) {
            return 0;
        }
        return (int) amount;
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.aeinfinitycell.infinity_cell_view");
    }

    @Override
    public int numRecipes() {
        return pages.size();
    }

    @Override
    public int getRecipeHeight(int recipe) {
        ViewPage page = page(recipe);
        if (page == null) {
            return 0;
        }
        int rows = (page.stacks.size() + ROW_ITEM_NUM - 1) / ROW_ITEM_NUM;
        return ITEMS_OFFSET_Y + rows * 18 + 2;
    }

    @Override
    public void drawBackground(int recipe) {
        ViewPage page = page(recipe);
        if (page == null) {
            return;
        }

        if (!drawSlotsInBackground(page.channel)) {
            return;
        }

        drawSlots(page);
    }

    private void drawSlots(ViewPage page) {
        GL11.glColor3f(1F, 1F, 1F);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(SLOT_TEXTURE_LOCATION);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        for (int i = 0; i < page.stacks.size(); i++) {
            int line = i % ROW_ITEM_NUM;
            int row = i / ROW_ITEM_NUM;
            tessellator.addVertexWithUV(OFFSET_X + 18 * (line + 1), ITEMS_OFFSET_Y + 18 * row, 0, 1, 0);
            tessellator.addVertexWithUV(OFFSET_X + 18 * line, ITEMS_OFFSET_Y + 18 * row, 0, 0, 0);
            tessellator.addVertexWithUV(OFFSET_X + 18 * line, ITEMS_OFFSET_Y + 18 * (row + 1), 0, 0, 1);
            tessellator.addVertexWithUV(OFFSET_X + 18 * (line + 1), ITEMS_OFFSET_Y + 18 * (row + 1), 0, 1, 1);
        }
        tessellator.draw();
    }

    static boolean drawSlotsInBackground(Channel channel) {
        return channel != Channel.FLUIDS;
    }

    @Override
    public void drawForeground(int recipe) {
        ViewPage page = page(recipe);
        if (page == null) {
            return;
        }

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer
            .drawString(StatCollector.translateToLocal(page.channel.getTranslationKey()), OFFSET_X, INFO_OFFSET_Y, 0);
        fontRenderer.drawString(
            formatTypeCountLine(page.totalTypes, GuiText.Types.getLocal()),
            OFFSET_X,
            INFO_OFFSET_Y + fontRenderer.FONT_HEIGHT + 2,
            0);

        if (page.channel == Channel.FLUIDS) {
            drawSlots(page);
            drawFluidIcons(page);
        }
        for (ViewItemStack viewStack : page.stacks) {
            if (viewStack.stackSize <= 0L) {
                continue;
            }
            StackSizeRenderer.drawStackSize(
                viewStack.stack.relx,
                viewStack.stack.rely,
                viewStack.stackSize,
                fontRenderer,
                TerminalFontSize.SMALL);
        }
    }

    static String formatTypeCountLine(long totalTypes, String typesText) {
        return NumberFormat.getNumberInstance()
            .format(totalTypes) + ' '
            + typesText;
    }

    private void drawFluidIcons(ViewPage page) {
        Tessellator tessellator = Tessellator.instance;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        tessellator.startDrawingQuads();
        for (ViewItemStack viewStack : page.stacks) {
            if (viewStack.icon == null) {
                continue;
            }

            int x = viewStack.stack.relx;
            int y = viewStack.stack.rely;
            IIcon icon = viewStack.icon;
            int color = viewStack.color;
            tessellator.setColorRGBA(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 0xFF);
            tessellator.addVertexWithUV(x + 16, y, 0, icon.getMaxU(), icon.getMinV());
            tessellator.addVertexWithUV(x, y, 0, icon.getMinU(), icon.getMinV());
            tessellator.addVertexWithUV(x, y + 16, 0, icon.getMinU(), icon.getMaxV());
            tessellator.addVertexWithUV(x + 16, y + 16, 0, icon.getMaxU(), icon.getMaxV());
        }
        tessellator.draw();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public List<PositionedStack> getIngredientStacks(int recipe) {
        return new ArrayList<>();
    }

    @Override
    public List<PositionedStack> getOtherStacks(int recipe) {
        ViewPage page = page(recipe);
        if (page == null) {
            return new ArrayList<PositionedStack>();
        }
        return page.stacks.stream()
            .map(stack -> stack.stack)
            .collect(Collectors.toList());
    }

    @Override
    public PositionedStack getResultStack(int recipe) {
        return null;
    }

    @Override
    public void onUpdate() {}

    @Override
    public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
        return false;
    }

    @Override
    public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe) {
        return null;
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> currenttip, int recipe) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack, List<String> currenttip, int recipe) {
        ViewPage page = page(recipe);
        if (page == null || stack == null) {
            return currenttip;
        }

        for (ViewItemStack viewStack : page.stacks) {
            if (viewStack.stack.containsWithNBT(stack)) {
                currenttip.add(
                    Math.min(1, currenttip.size()),
                    GRAY + GuiText.Stored.getLocal()
                        + ": "
                        + NumberFormat.getNumberInstance()
                            .format(viewStack.amount));
                break;
            }
        }
        return currenttip;
    }

    @Override
    public boolean keyTyped(GuiRecipe<?> gui, char keyChar, int keyCode, int recipe) {
        return false;
    }

    @Override
    public boolean mouseClicked(GuiRecipe<?> gui, int button, int recipe) {
        return false;
    }

    @Override
    public int recipiesPerPage() {
        return 1;
    }

    private ViewPage page(int recipe) {
        return recipe < 0 || recipe >= pages.size() ? null : pages.get(recipe);
    }

    private static final class ViewPage {

        private final Channel channel;
        private final List<ViewItemStack> stacks;
        private final long totalTypes;

        private ViewPage(Channel channel, List<ViewItemStack> stacks, long totalTypes) {
            this.channel = channel;
            this.stacks = stacks;
            this.totalTypes = totalTypes;
        }
    }

    private static final class ViewItemStack {

        private final PositionedStack stack;
        private final BigInteger amount;
        private final long stackSize;
        private final IIcon icon;
        private final int color;

        private ViewItemStack(PositionedStack stack, BigInteger amount, long stackSize, IIcon icon, int color) {
            this.stack = stack;
            this.amount = amount;
            this.stackSize = stackSize;
            this.icon = icon;
            this.color = color;
        }
    }
}
