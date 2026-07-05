package cn.dancingsnow.aeinfinitycell.nei;

import cn.dancingsnow.aeinfinitycell.Tags;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

@SuppressWarnings("unused")
public final class NEIAEInfinityCellConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerUsageHandler(new InfinityCellViewHandler());
    }

    @Override
    public String getName() {
        return "AE2 Infinity Cell NEI Plugin";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
