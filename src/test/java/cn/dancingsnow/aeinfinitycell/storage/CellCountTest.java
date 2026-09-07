package cn.dancingsnow.aeinfinitycell.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

public class CellCountTest {

    @Test
    public void smallAmountsStayInLongRange() {
        CellCount count = new CellCount();
        assertTrue(count.isZero());
        assertFalse(count.isPositive());

        count.add(1000L);
        count.add(500L);

        assertEquals(1500L, count.longValue());
        assertEquals("1500", count.toString());
        assertEquals(BigInteger.valueOf(1500L), count.toBigInteger());
    }

    @Test
    public void nonPositiveAddsAreIgnored() {
        CellCount count = new CellCount();
        count.add(10L);
        count.add(0L);
        count.add(-5L);

        assertEquals(10L, count.longValue());
    }

    @Test
    public void overflowBeyondLongMaxSpillsToBigInteger() {
        CellCount count = new CellCount();
        count.add(Long.MAX_VALUE);
        count.add(1L);

        BigInteger expected = BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.ONE);
        assertEquals(Long.MAX_VALUE, count.longValue());
        assertEquals(expected, count.toBigInteger());
        assertEquals(expected.toString(), count.toString());
    }

    @Test
    public void addBigAccumulatesAcrossRepresentations() {
        CellCount count = new CellCount();
        count.add(Long.MAX_VALUE);

        CellCount other = new CellCount();
        other.add(Long.MAX_VALUE);
        other.add(7L);
        count.add(other);

        assertEquals(
            BigInteger.valueOf(Long.MAX_VALUE)
                .multiply(BigInteger.valueOf(2L))
                .add(BigInteger.valueOf(7L)),
            count.toBigInteger());
    }

    @Test
    public void extractCapsAtRequestedAndAvailable() {
        CellCount count = new CellCount();
        count.add(100L);

        assertEquals(0L, count.extract(0L));
        assertEquals(0L, count.extract(-1L));
        assertEquals(40L, count.extract(40L));
        assertEquals(60L, count.extract(1000L));
        assertTrue(count.isZero());
        assertEquals(0L, count.extract(10L));
    }

    @Test
    public void extractFromBigCollapsesBackToLong() {
        CellCount count = new CellCount();
        count.add(Long.MAX_VALUE);
        count.add(50L);

        assertEquals(Long.MAX_VALUE, count.extract(Long.MAX_VALUE));

        assertEquals(50L, count.longValue());
        assertEquals("50", count.toString());
    }

    @Test
    public void parseRoundTripsDecimalStrings() {
        assertEquals(
            12345L,
            CellCount.parse("12345")
                .longValue());

        BigInteger huge = BigInteger.valueOf(Long.MAX_VALUE)
            .add(BigInteger.valueOf(999L));
        assertEquals(
            huge,
            CellCount.parse(huge.toString())
                .toBigInteger());
        assertEquals(
            huge.toString(),
            CellCount.parse(huge.toString())
                .toString());
    }

    @Test
    public void parseRejectsMalformedAndNonPositiveText() {
        assertTrue(
            CellCount.parse("not-a-number")
                .isZero());
        assertTrue(
            CellCount.parse("-5")
                .isZero());
        assertTrue(
            CellCount.parse("0")
                .isZero());
        assertTrue(
            CellCount.parse("")
                .isZero());
        assertTrue(
            CellCount.parse(null)
                .isZero());
    }

    @Test
    public void compareToOrdersAcrossRepresentations() {
        CellCount small = new CellCount();
        small.add(10L);
        CellCount large = new CellCount();
        large.add(Long.MAX_VALUE);
        large.add(1L);

        assertTrue(small.compareTo(large) < 0);
        assertTrue(large.compareTo(small) > 0);
        assertEquals(0, large.compareTo(large.copy()));
        assertEquals(0, small.compareTo(small.copy()));
    }

    @Test
    public void copyIsIndependentSnapshot() {
        CellCount count = new CellCount();
        count.add(100L);
        CellCount snapshot = count.copy();

        count.add(50L);

        assertEquals(100L, snapshot.longValue());
        assertEquals(150L, count.longValue());
        assertEquals(snapshot, CellCount.parse("100"));
    }

    @Test
    public void clearResetsToZero() {
        CellCount count = new CellCount();
        count.add(Long.MAX_VALUE);
        count.add(1L);

        count.clear();

        assertTrue(count.isZero());
        assertEquals(0L, count.longValue());
    }
}
