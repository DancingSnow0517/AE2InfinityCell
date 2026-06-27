package cn.dancingsnow.aeinfinitycell;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
@SuppressWarnings("unused")
public final class AEInfinityCellLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.aeinfinitycell.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Arrays.asList("TileDriveMixin");
    }
}
