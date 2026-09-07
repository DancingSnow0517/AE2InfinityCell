package cn.dancingsnow.aeinfinitycell.storage;

import java.math.BigInteger;
import java.util.Objects;

import cn.dancingsnow.aeinfinitycell.AEInfinityCell;

/**
 * 无限存储单元中单个条目的数量计数。
 * 数量不超过 {@link Long#MAX_VALUE} 时直接用 long 存放，避免存取热路径上反复分配 BigInteger；
 * 累加溢出后切换为 BigInteger 计数，回落到 long 范围内时再切换回来，数量本身不设上限。
 */
public final class CellCount implements Comparable<CellCount> {

    private static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private long value;
    private BigInteger big;

    public CellCount() {}

    private CellCount(long value, BigInteger big) {
        this.value = value;
        this.big = big;
    }

    /**
     * 从 NBT 中的十进制字符串解析数量。格式非法或数量不为正时返回 0 并记录警告。
     */
    public static CellCount parse(String text) {
        if (text == null || text.isEmpty()) {
            return new CellCount();
        }
        try {
            long parsed = Long.parseLong(text);
            CellCount count = new CellCount();
            if (parsed > 0L) {
                count.value = parsed;
            }
            return count;
        } catch (NumberFormatException notALong) {
            try {
                BigInteger parsed = new BigInteger(text);
                CellCount count = new CellCount();
                if (parsed.signum() > 0) {
                    if (parsed.compareTo(BIG_LONG_MAX) > 0) {
                        count.big = parsed;
                    } else {
                        count.value = parsed.longValue();
                    }
                }
                return count;
            } catch (NumberFormatException malformed) {
                AEInfinityCell.LOG.warn("Ignoring malformed cell amount: {}", text);
                return new CellCount();
            }
        }
    }

    public void add(long amount) {
        if (amount <= 0L) {
            return;
        }
        if (big != null) {
            big = big.add(BigInteger.valueOf(amount));
            return;
        }
        if (value <= Long.MAX_VALUE - amount) {
            value += amount;
        } else {
            big = BigInteger.valueOf(value)
                .add(BigInteger.valueOf(amount));
            value = 0L;
        }
    }

    public void add(CellCount other) {
        if (other.big != null) {
            addBig(other.big);
        } else {
            add(other.value);
        }
    }

    /**
     * 取出至多 requested 的数量，返回实际取出的数量。
     */
    public long extract(long requested) {
        if (requested <= 0L || isZero()) {
            return 0L;
        }
        if (big == null) {
            long removed = Math.min(value, requested);
            value -= removed;
            return removed;
        }
        BigInteger removed = big.min(BigInteger.valueOf(requested));
        big = big.subtract(removed);
        collapseToLong();
        return removed.longValue();
    }

    /**
     * 返回数量值，超出 long 范围时钳制到 {@link Long#MAX_VALUE}。
     */
    public long longValue() {
        return big != null ? Long.MAX_VALUE : value;
    }

    public void clear() {
        value = 0L;
        big = null;
    }

    public boolean isZero() {
        return big == null && value == 0L;
    }

    public boolean isPositive() {
        return big != null || value > 0L;
    }

    public BigInteger toBigInteger() {
        return big != null ? big : BigInteger.valueOf(value);
    }

    public CellCount copy() {
        return new CellCount(value, big);
    }

    @Override
    public int compareTo(CellCount other) {
        if (big != null) {
            return other.big != null ? big.compareTo(other.big) : 1;
        }
        if (other.big != null) {
            return -1;
        }
        return Long.compare(value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CellCount)) {
            return false;
        }
        CellCount that = (CellCount) o;
        return value == that.value && Objects.equals(big, that.big);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, big);
    }

    @Override
    public String toString() {
        return big != null ? big.toString() : Long.toString(value);
    }

    private void addBig(BigInteger amount) {
        if (big != null) {
            big = big.add(amount);
            return;
        }
        BigInteger total = BigInteger.valueOf(value)
            .add(amount);
        if (total.compareTo(BIG_LONG_MAX) > 0) {
            big = total;
            value = 0L;
        } else {
            value = total.longValue();
        }
    }

    private void collapseToLong() {
        if (big != null && big.compareTo(BIG_LONG_MAX) <= 0) {
            value = big.longValue();
            big = null;
        }
    }
}
