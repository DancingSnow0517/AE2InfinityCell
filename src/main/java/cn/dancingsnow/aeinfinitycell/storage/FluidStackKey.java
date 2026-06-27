package cn.dancingsnow.aeinfinitycell.storage;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public final class FluidStackKey {

    private static final String KEY_FLUID_ID = "id";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_TAG = "tag";

    private final String fluidName;
    private final NbtKey tag;

    private FluidStackKey(String fluidName, NbtKey tag) {
        this.fluidName = fluidName;
        this.tag = tag;
    }

    public static FluidStackKey from(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) {
            throw new IllegalArgumentException("stack");
        }
        return new FluidStackKey(FluidRegistry.getFluidName(stack.getFluid()), NbtKey.of(stack.tag));
    }

    public static FluidStackKey readFromNBT(NBTTagCompound serialized) {
        String fluidName = serialized.getString(KEY_FLUID_ID);
        NbtKey tag = serialized.hasKey(KEY_TAG) ? NbtKey.of(serialized.getCompoundTag(KEY_TAG)) : NbtKey.NONE;
        return new FluidStackKey(fluidName, tag);
    }

    public NBTTagCompound writeToNBT(long amount) {
        NBTTagCompound serialized = new NBTTagCompound();
        serialized.setString(KEY_FLUID_ID, fluidName);
        serialized.setLong(KEY_AMOUNT, amount);
        if (!tag.isEmpty()) {
            serialized.setTag(KEY_TAG, tag.copyTag());
        }
        return serialized;
    }

    public FluidStack toStack(long amount) {
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid == null) {
            return null;
        }
        NBTTagCompound copiedTag = tag.isEmpty() ? null : tag.copyTag();
        return new FluidStack(fluid, saturatedInt(amount), copiedTag);
    }

    public String getFluidName() {
        return fluidName;
    }

    public NbtKey getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FluidStackKey)) {
            return false;
        }
        FluidStackKey that = (FluidStackKey) o;
        return Objects.equals(fluidName, that.fluidName) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluidName, tag);
    }

    @Override
    public String toString() {
        return "FluidStackKey{" + fluidName + ", tag=" + tag + '}';
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
}
