package cn.dancingsnow.aeinfinitycell.recipe;

import appeng.api.AEApi;
import cn.dancingsnow.aeinfinitycell.item.ModItems;
import com.glodblock.github.loader.ItemAndBlockHolder;
import fox.spiteful.avaritia.items.LudicrousItems;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.material.MaterialsAlloy;
import net.minecraft.item.ItemStack;
import singulariteam.eternalsingularity.item.EternalSingularityItem;
import thaumicenergistics.api.ThEApi;

import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;
import static gregtech.api.util.GTRecipeConstants.AssemblyLine;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;

public class ModRecipeLoader {

    public static void loadRecipes() {
        ItemStack itemSingularity = AEApi.instance().definitions()
            .items()
            .cellSingularity()
            .maybeStack(1)
            .orNull();

        if (itemSingularity == null) {
            return;
        }

        ItemStack fluidSingularity = new ItemStack(ItemAndBlockHolder.SINGULARITY_CELL, 1);

        ThEApi thEApi = ThEApi.instance();
        if (thEApi == null) {
            return;
        }
        ItemStack essentiaSingularity = thEApi.items().EssentiaCell_Singularity.getStacks(1);


        GTRecipeBuilder.builder()
            .metadata(RESEARCH_ITEM, itemSingularity)
            .metadata(SCANNING, new Scanning(3 * MINUTES, TierEU.RECIPE_UV))
            .itemInputs(
                new ItemStack(EternalSingularityItem.instance, 1),
                itemSingularity,
                fluidSingularity,
                essentiaSingularity,
                new ItemStack(LudicrousItems.resource, 8, 5),
                GTOreDictUnificator.get(OrePrefixes.bolt, Materials.Infinity, 32),
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Infinity, 32),
                ItemList.Field_Generator_UV.get(16),
                new Object[]{OrePrefixes.circuit.get(Materials.UV), 16})
            .fluidInputs(
                Materials.Infinity.getMolten(16 * INGOTS),
                MaterialsAlloy.INDALLOY_140.getFluidStack(32 * INGOTS),
                Materials.SolderingAlloy.getMolten(4 * INGOTS))
            .itemOutputs(new ItemStack(ModItems.INFINITY_STORAGE_CELL, 1))
            .eut(TierEU.RECIPE_UHV)
            .duration(90 * SECONDS)
            .addTo(AssemblyLine);
    }
}
