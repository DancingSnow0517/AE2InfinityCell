package cn.dancingsnow.aeinfinitycell;

import net.minecraftforge.client.MinecraftForgeClient;

import cn.dancingsnow.aeinfinitycell.item.ModItems;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import fox.spiteful.avaritia.render.FancyHaloRenderer;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        FancyHaloRenderer shiny = new FancyHaloRenderer();

        MinecraftForgeClient.registerItemRenderer(ModItems.INFINITY_STORAGE_CELL, shiny);
    }
}
