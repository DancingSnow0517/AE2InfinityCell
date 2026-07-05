package cn.dancingsnow.aeinfinitycell;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static final int MIN_NEI_PREVIEW_ENTRIES_PER_CHANNEL = 1;
    public static final int MAX_NEI_PREVIEW_ENTRIES_PER_CHANNEL = 63;

    public static String greeting = "Hello World";
    public static int neiPreviewEntriesPerChannel = MAX_NEI_PREVIEW_ENTRIES_PER_CHANNEL;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        neiPreviewEntriesPerChannel = clampNeiPreviewEntriesPerChannel(
            configuration.getInt(
                "neiPreviewEntriesPerChannel",
                Configuration.CATEGORY_GENERAL,
                neiPreviewEntriesPerChannel,
                MIN_NEI_PREVIEW_ENTRIES_PER_CHANNEL,
                MAX_NEI_PREVIEW_ENTRIES_PER_CHANNEL,
                "Maximum entries shown per item, fluid, or essentia page in the infinity cell NEI view."));

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static int clampNeiPreviewEntriesPerChannel(int value) {
        if (value < MIN_NEI_PREVIEW_ENTRIES_PER_CHANNEL) {
            return MIN_NEI_PREVIEW_ENTRIES_PER_CHANNEL;
        }
        if (value > MAX_NEI_PREVIEW_ENTRIES_PER_CHANNEL) {
            return MAX_NEI_PREVIEW_ENTRIES_PER_CHANNEL;
        }
        return value;
    }
}
