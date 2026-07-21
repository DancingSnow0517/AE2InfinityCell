package cn.dancingsnow.aeinfinitycell.nei;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.dancingsnow.aeinfinitycell.storage.EssentiaStackKey;
import cn.dancingsnow.aeinfinitycell.storage.FluidStackKey;
import cn.dancingsnow.aeinfinitycell.storage.InfinityCellRecord;
import cn.dancingsnow.aeinfinitycell.storage.ItemStackKey;

public final class InfinityCellViewPreview {

    private static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private InfinityCellViewPreview() {}

    public static List<Entry<ItemStackKey>> items(InfinityCellRecord record, int limit) {
        return record == null ? Collections.emptyList() : select(record.getItemsView(), limit);
    }

    public static List<Entry<FluidStackKey>> fluids(InfinityCellRecord record, int limit) {
        return record == null ? Collections.emptyList() : select(record.getFluidsView(), limit);
    }

    public static List<Entry<EssentiaStackKey>> essentia(InfinityCellRecord record, int limit) {
        return record == null ? Collections.emptyList() : select(record.getEssentiaView(), limit);
    }

    public static List<Entry<Void>> eu(InfinityCellRecord record, int limit) {
        if (record == null || limit <= 0
            || record.getEUAmountExact()
                .signum() <= 0) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new Entry<>(null, record.getEUAmountExact()));
    }

    public static List<Page> pages(InfinityCellRecord record, int limit) {
        if (record == null) {
            return Collections.emptyList();
        }

        List<Page> pages = new ArrayList<>();
        List<Entry<ItemStackKey>> itemEntries = items(record, limit);
        if (!itemEntries.isEmpty()) {
            pages.add(new Page(Channel.ITEMS, itemEntries, record.getUsedItemTypes()));
        }

        List<Entry<FluidStackKey>> fluidEntries = fluids(record, limit);
        if (!fluidEntries.isEmpty()) {
            pages.add(new Page(Channel.FLUIDS, fluidEntries, record.getUsedFluidTypes()));
        }

        List<Entry<EssentiaStackKey>> essentiaEntries = essentia(record, limit);
        if (!essentiaEntries.isEmpty()) {
            pages.add(new Page(Channel.ESSENTIA, essentiaEntries, record.getUsedEssentiaTypes()));
        }

        List<Entry<Void>> euEntries = eu(record, limit);
        if (!euEntries.isEmpty()) {
            pages.add(new Page(Channel.EU, euEntries, record.getUsedEUTypes()));
        }
        return pages;
    }

    private static <K> List<Entry<K>> select(Map<K, BigInteger> source, int limit) {
        if (limit <= 0 || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<Entry<K>> entries = new ArrayList<>();
        for (Map.Entry<K, BigInteger> sourceEntry : source.entrySet()) {
            BigInteger amount = sourceEntry.getValue();
            if (amount != null && amount.signum() > 0) {
                entries.add(new Entry<>(sourceEntry.getKey(), amount));
            }
        }

        entries.sort((left, right) -> right.amount.compareTo(left.amount));

        if (entries.size() > limit) {
            return new ArrayList<>(entries.subList(0, limit));
        }
        return entries;
    }

    public enum Channel {

        ITEMS("nei.aeinfinitycell.channel.items"),
        FLUIDS("nei.aeinfinitycell.channel.fluids"),
        ESSENTIA("nei.aeinfinitycell.channel.essentia"),
        EU("nei.aeinfinitycell.channel.eu");

        private final String translationKey;

        Channel(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    public static final class Page {

        private final Channel channel;
        private final List<? extends Entry<?>> entries;
        private final long totalTypes;

        private Page(Channel channel, List<? extends Entry<?>> entries, long totalTypes) {
            this.channel = channel;
            this.entries = entries;
            this.totalTypes = totalTypes;
        }

        public Channel getChannel() {
            return channel;
        }

        public List<? extends Entry<?>> getEntries() {
            return entries;
        }

        public long getTotalTypes() {
            return totalTypes;
        }

    }

    public static final class Entry<K> {

        private final K key;
        private final BigInteger amount;

        private Entry(K key, BigInteger amount) {
            this.key = key;
            this.amount = amount;
        }

        public K getKey() {
            return key;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public long getStackSize() {
            if (amount.compareTo(BIG_LONG_MAX) > 0) {
                return Long.MAX_VALUE;
            }
            return amount.longValue();
        }
    }
}
