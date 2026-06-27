package cn.dancingsnow.aeinfinitycell.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Requires Minecraft/Forge registries; verify in a launched client.")
public class FluidStackKeyTest {

    @Test
    public void fluidKeyRoundTripsByFluidNameAndAmount() {
        FluidStack water = new FluidStack(FluidRegistry.WATER, 2500);

        FluidStackKey key = FluidStackKey.from(water);
        NBTTagCompound serialized = key.writeToNBT(2500L);
        FluidStackKey loaded = FluidStackKey.readFromNBT(serialized);

        assertEquals(key, loaded);
        assertEquals(2500L, serialized.getLong("amount"));
        assertEquals("water", serialized.getString("id"));
        assertEquals(2500, loaded.toStack(2500L).amount);
    }

    @Test
    public void fluidKeyPreservesTagIdentity() {
        FluidStack first = new FluidStack(FluidRegistry.WATER, 1000);
        first.tag = new NBTTagCompound();
        first.tag.setString("temperature", "cold");

        FluidStack second = new FluidStack(FluidRegistry.WATER, 1000);
        second.tag = new NBTTagCompound();
        second.tag.setString("temperature", "hot");

        assertNotEquals(FluidStackKey.from(first), FluidStackKey.from(second));
    }

    @Test
    public void unknownFluidDoesNotReconstruct() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", "missing_fluid");
        tag.setLong("amount", 1000L);

        FluidStackKey key = FluidStackKey.readFromNBT(tag);

        assertNull(key.toStack(1000L));
    }
}
