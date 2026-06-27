package cn.dancingsnow.aeinfinitycell.storage;

import java.util.Objects;

import net.minecraft.nbt.NBTTagCompound;

import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

public final class EssentiaStackKey {

    private static final String KEY_ASPECT = "aspect";
    private static final String KEY_AMOUNT = "amount";

    private final String aspectTag;

    private EssentiaStackKey(String aspectTag) {
        this.aspectTag = aspectTag;
    }

    public static EssentiaStackKey from(AEEssentiaStack stack) {
        if (stack == null || stack.getAspect() == null) {
            throw new IllegalArgumentException("stack");
        }
        return new EssentiaStackKey(
            stack.getAspect()
                .getTag());
    }

    public static EssentiaStackKey readFromNBT(NBTTagCompound serialized) {
        return new EssentiaStackKey(serialized.getString(KEY_ASPECT));
    }

    public NBTTagCompound writeToNBT(long amount) {
        NBTTagCompound serialized = new NBTTagCompound();
        serialized.setString(KEY_ASPECT, aspectTag);
        serialized.setString(KEY_AMOUNT, Long.toString(amount));
        return serialized;
    }

    public AEEssentiaStack toStack(long amount) {
        Aspect aspect = Aspect.getAspect(aspectTag);
        if (aspect == null) {
            return null;
        }
        return new AEEssentiaStack(aspect, amount);
    }

    public String getAspectTag() {
        return aspectTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EssentiaStackKey)) {
            return false;
        }
        EssentiaStackKey that = (EssentiaStackKey) o;
        return Objects.equals(aspectTag, that.aspectTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspectTag);
    }

    @Override
    public String toString() {
        return "EssentiaStackKey{" + aspectTag + '}';
    }
}
