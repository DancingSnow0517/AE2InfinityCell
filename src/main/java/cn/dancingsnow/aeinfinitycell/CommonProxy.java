package cn.dancingsnow.aeinfinitycell;

import cn.dancingsnow.aeinfinitycell.recipe.ModRecipeLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import appeng.api.AEApi;
import cn.dancingsnow.aeinfinitycell.ae.InfinityCellHandler;
import cn.dancingsnow.aeinfinitycell.item.ModItems;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellDataAccess;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellStorage;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        ModItems.register();

        AEInfinityCell.LOG.info(Config.greeting);
        AEInfinityCell.LOG.info("AE2 Infinity Cell version " + Tags.VERSION);
    }

    public void init(FMLInitializationEvent event) {
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(new InfinityCellHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void postInit(FMLPostInitializationEvent event) {
        ModRecipeLoader.loadRecipes();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        ServerWorldAccess.setServer(event.getServer());
        InfinityCellDataAccess.migrateLegacy(null);
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        InfinityCellStorage.getInstance()
            .saveAll();
        InfinityCellStorage.getInstance()
            .clear();
        ServerWorldAccess.clear();
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (event.world.provider.dimensionId == 0) {
            InfinityCellStorage.getInstance()
                .saveAll();
        }
    }
}
