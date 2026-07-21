package cn.dancingsnow.aeinfinitycell;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigTest {

    @Test
    public void neiPreviewEntriesPerChannelHasMinimumButNoHardMaximum() {
        assertEquals(1, Config.clampNeiPreviewEntriesPerChannel(0));
        assertEquals(42, Config.clampNeiPreviewEntriesPerChannel(42));
        assertEquals(128, Config.clampNeiPreviewEntriesPerChannel(128));
        assertEquals(Integer.MAX_VALUE, Config.clampNeiPreviewEntriesPerChannel(Integer.MAX_VALUE));
    }
}
