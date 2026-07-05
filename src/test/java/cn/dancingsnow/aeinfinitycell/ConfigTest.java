package cn.dancingsnow.aeinfinitycell;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigTest {

    @Test
    public void neiPreviewEntriesPerChannelIsClampedToVisiblePageRange() {
        assertEquals(1, Config.clampNeiPreviewEntriesPerChannel(0));
        assertEquals(42, Config.clampNeiPreviewEntriesPerChannel(42));
        assertEquals(63, Config.clampNeiPreviewEntriesPerChannel(128));
    }
}
