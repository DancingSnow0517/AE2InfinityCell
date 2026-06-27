package cn.dancingsnow.aeinfinitycell;

import appeng.api.AEApi;
import cn.dancingsnow.aeinfinitycell.ae.InfinityCellHandler;
import cn.dancingsnow.aeinfinitycell.item.ModItems;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        ModItems.register();

        AEInfinityCell.LOG.info(Config.greeting);
        AEInfinityCell.LOG.info("AE2 Infinity Cell version " + Tags.VERSION);
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(new InfinityCellHandler());
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        ServerWorldAccess.setServer(event.getServer());
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        ServerWorldAccess.clear();
    }
}
