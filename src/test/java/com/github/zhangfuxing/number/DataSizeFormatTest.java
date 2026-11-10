package com.github.zhangfuxing.number;

import junit.framework.TestCase;

import java.math.BigDecimal;

/**
 * {@link DataSizeFormat} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/9
 */
public class DataSizeFormatTest extends TestCase {

	private final DataSizeFormat dataSizeFormat = new DataSizeFormat();

	public void testFormatZeroAndBytes() {
		assertEquals("0 B", dataSizeFormat.format(0));
		assertEquals("1 B", dataSizeFormat.format(1));
		assertEquals("1023 B", dataSizeFormat.format(1023));
	}

	public void testFormatBinaryUnits() {
		assertEquals("1 KB", dataSizeFormat.format(1024));
		assertEquals("1.5 KB", dataSizeFormat.format(1536));
		assertEquals("1 MB", dataSizeFormat.format(1024L * 1024));
		assertEquals("1.18 MB", dataSizeFormat.format(1234567));
		assertEquals("1 GB", dataSizeFormat.format(1024L * 1024 * 1024));
		assertEquals("1 TB", dataSizeFormat.format(1024L * 1024 * 1024 * 1024));
		assertEquals("1 PB", dataSizeFormat.format(1024L * 1024 * 1024 * 1024 * 1024));
	}

	public void testFormatMaxLong() {
		assertEquals("8 EB", dataSizeFormat.format(Long.MAX_VALUE));
	}

	public void testFormatNegative() {
		assertEquals("-1.5 KB", dataSizeFormat.format(-1536));
		assertEquals("-1 MB", dataSizeFormat.format(-1024L * 1024));
	}

	public void testFormatWithScale() {
		assertEquals("2 KB", new DataSizeFormat().setScale(0).format(1536));
		assertEquals("1 KB", new DataSizeFormat().setScale(0).format(1024));
		assertEquals("1.5 KB", new DataSizeFormat().setScale(3).format(1536));
		assertEquals("1.000 KB", new DataSizeFormat(DataSizeFormat.BINARY_BASE, 3)
				.setStripTrailingZeros(false)
				.format(1024));
	}

	public void testFormatDecimalBase() {
		DataSizeFormat decimalFormat = new DataSizeFormat(DataSizeFormat.DECIMAL_BASE, 2);
		assertEquals("1 KB", decimalFormat.format(1000));
		assertEquals("1.5 KB", decimalFormat.format(1500));
		assertEquals("1 MB", decimalFormat.format(1000L * 1000));
		// 999.999 KB 四舍五入后进位成 1 MB，不能输出 1000 KB
		assertEquals("1 MB", decimalFormat.format(999999));
	}

	public void testFormatWithoutSpace() {
		assertEquals("1.5KB", new DataSizeFormat().setSpace(false).format(1536));
	}

	public void testKeepTrailingZeros() {
		assertEquals("1.00 KB", new DataSizeFormat().setStripTrailingZeros(false).format(1024));
	}

	public void testInvalidConfig() {
		try {
			new DataSizeFormat().setScale(-1);
			fail("scale 为负数时应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			new DataSizeFormat().setScale(11);
			fail("scale 超过 10 时应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
		try {
			new DataSizeFormat().setBase(1);
			fail("base 小于 2 时应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	// ---------------------------------------------------------------- 解析

	public void testParse() {
		assertEquals(314572800L, dataSizeFormat.parse("300MB"));
		assertEquals(314572800L, dataSizeFormat.parse("300 mb"));
		assertEquals(314572800L, dataSizeFormat.parse("300M"));
		assertEquals(314572800L, dataSizeFormat.parse("+300MB"));
		assertEquals(1610612736L, dataSizeFormat.parse("1.5 GB"));
		assertEquals(1536L, dataSizeFormat.parse("1.5KB"));
		assertEquals(512L, dataSizeFormat.parse("0.5 KB"));
		assertEquals(1024L, dataSizeFormat.parse("1KiB"));
		assertEquals(1L, dataSizeFormat.parse("1B"));
		assertEquals(512L, dataSizeFormat.parse("512字节"));
	}

	public void testParseWithoutUnit() {
		assertEquals(1024L, dataSizeFormat.parse("1024"));
		assertEquals(0L, dataSizeFormat.parse("0"));
	}

	public void testParseNegative() {
		assertEquals(-1536L, dataSizeFormat.parse("-1.5kb"));
		assertEquals(-314572800L, dataSizeFormat.parse("-300MB"));
	}

	public void testParseRoundTrip() {
		for (long bytes : new long[]{0L, 1L, 1024L, 1536L, 1048576L}) {
			assertEquals(bytes, dataSizeFormat.parse(dataSizeFormat.format(bytes)));
		}
	}

	public void testParseDecimalBase() {
		DataSizeFormat decimalFormat = new DataSizeFormat(DataSizeFormat.DECIMAL_BASE, 2);
		assertEquals(1000L, decimalFormat.parse("1KB"));
		assertEquals(1500L, decimalFormat.parse("1.5KB"));
	}

	public void testParseInvalid() {
		assertParseFailed(null);
		assertParseFailed("");
		assertParseFailed("   ");
		assertParseFailed("abc");
		assertParseFailed("MB");
		assertParseFailed("1.2.3MB");
		assertParseFailed("1XB");
	}

	public void testParseOverflow() {
		assertParseFailed("99999999999EB");
	}

	// ------------------------------------------------------------ 单位换算

	public void testFormatWithUnit() {
		assertEquals("300 MB", dataSizeFormat.format(314572800L, DataSizeUnit.MB));
		assertEquals("0.29 GB", dataSizeFormat.format(314572800L, DataSizeUnit.GB));
		assertEquals("-0.29 GB", dataSizeFormat.format(-314572800L, DataSizeUnit.GB));
		assertEquals("307200 KB", dataSizeFormat.format(314572800L, DataSizeUnit.KB));
	}

	public void testToUnit() {
		assertEquals(0, new BigDecimal("0.29296875").compareTo(dataSizeFormat.toUnit(314572800L, DataSizeUnit.GB)));
		assertEquals(0, new BigDecimal("300").compareTo(dataSizeFormat.toUnit(314572800L, DataSizeUnit.MB)));
		assertEquals(0, BigDecimal.ZERO.compareTo(dataSizeFormat.toUnit(0L, DataSizeUnit.GB)));
		assertEquals(0, new BigDecimal("-0.29296875").compareTo(dataSizeFormat.toUnit(-314572800L, DataSizeUnit.GB)));
	}

	public void testConvert() {
		assertEquals("0.29 GB", dataSizeFormat.convert("300MB", DataSizeUnit.GB));
		assertEquals("314572800 B", dataSizeFormat.convert("300MB", DataSizeUnit.B));
		assertEquals("1024 KB", dataSizeFormat.convert("1MB", DataSizeUnit.KB));
	}

	public void testConvertTo() {
		assertEquals(0, new BigDecimal("0.29296875").compareTo(dataSizeFormat.convertTo("300MB", DataSizeUnit.GB)));
		assertEquals(0, new BigDecimal("1024").compareTo(dataSizeFormat.convertTo("1MB", DataSizeUnit.KB)));
	}

	public void testDataSizeUnitOf() {
		assertEquals(DataSizeUnit.B, DataSizeUnit.of(null));
		assertEquals(DataSizeUnit.B, DataSizeUnit.of(""));
		assertEquals(DataSizeUnit.B, DataSizeUnit.of("字节"));
		assertEquals(DataSizeUnit.MB, DataSizeUnit.of("mb"));
		assertEquals(DataSizeUnit.MB, DataSizeUnit.of("MiB"));
		assertEquals(DataSizeUnit.GB, DataSizeUnit.of("G"));
		assertEquals(6, DataSizeUnit.EB.getIndex());
		assertEquals(0, new BigDecimal("1073741824").compareTo(DataSizeUnit.GB.factor(DataSizeFormat.BINARY_BASE)));
		try {
			DataSizeUnit.of("XB");
			fail("未知单位时应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}

	private void assertParseFailed(String text) {
		try {
			dataSizeFormat.parse(text);
			fail("非法输入应抛出 IllegalArgumentException: " + text);
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}
}
