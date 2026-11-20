package com.github.zhangfuxing.number;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link NumberParse} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/20
 */
public class NumberParseTest {

	@Test
	public void testParseFromString() {
		assertEquals(Byte.valueOf((byte) 12), NumberParse.parseByte("12"));
		assertEquals(Short.valueOf((short) 1234), NumberParse.parseShort("1234"));
		assertEquals(Integer.valueOf(123), NumberParse.parseInt("123"));
		assertEquals(Long.valueOf(1234567890123L), NumberParse.parseLong("1234567890123"));
		assertEquals(Float.valueOf(1.5f), NumberParse.parseFloat("1.5"));
		assertEquals(Double.valueOf(1.5d), NumberParse.parseDouble("1.5"));
		assertEquals(new BigInteger("12345678901234567890"), NumberParse.parseBigInteger("12345678901234567890"));
		assertEquals(new BigDecimal("123.45"), NumberParse.parseBigDecimal("123.45"));
	}

	@Test
	public void testTrimAndSign() {
		assertEquals(Integer.valueOf(42), NumberParse.parseInt("  +42  "));
		assertEquals(Integer.valueOf(-42), NumberParse.parseInt("\t-42\n"));
		assertEquals(new BigDecimal("-1.5"), NumberParse.parseBigDecimal(" -1.5 "));
	}

	@Test
	public void testParseFromNumber() {
		assertEquals(Byte.valueOf((byte) 12), NumberParse.parseByte(12));
		assertEquals(Short.valueOf((short) 1234), NumberParse.parseShort(1234L));
		assertEquals(Integer.valueOf(123), NumberParse.parseInt(123L));
		assertEquals(Integer.valueOf(120), NumberParse.parseInt(new BigInteger("120")));
		assertEquals(Integer.valueOf(123), NumberParse.parseInt(new BigDecimal("123.00")));
		assertEquals(Integer.valueOf(1), NumberParse.parseInt(1.0d));
		assertEquals(Integer.valueOf(-5), NumberParse.parseInt(-5.0f));
		assertEquals(Long.valueOf(1234567890123L), NumberParse.parseLong(new BigInteger("1234567890123")));
		assertEquals(Float.valueOf(1.5f), NumberParse.parseFloat(new BigDecimal("1.5")));
		assertEquals(Double.valueOf(1.5d), NumberParse.parseDouble(new BigDecimal("1.5")));
		assertEquals(new BigInteger("1000"), NumberParse.parseBigInteger(1000L));
		assertEquals(new BigInteger("1000"), NumberParse.parseBigInteger(new BigDecimal("1000.00")));
		assertEquals(new BigDecimal("1000"), NumberParse.parseBigDecimal(1000L));
		assertEquals(new BigDecimal("1000"), NumberParse.parseBigDecimal(new BigInteger("1000")));
		assertEquals(new BigDecimal("1.5"), NumberParse.parseBigDecimal(1.5d));
		assertEquals(new BigDecimal("1.5"), NumberParse.parseBigDecimal(1.5f));
		assertEquals(new BigDecimal("0.1"), NumberParse.parseBigDecimal(0.1d));
		assertEquals(new BigDecimal("0.1"), NumberParse.parseBigDecimal(0.1f));
	}

	@Test
	public void testFractionalNumberFailsForIntegralTypes() {
		assertThrows(NumberFormatException.class, () -> NumberParse.parseByte(1.5d));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseShort(1.5d));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt(1.5d));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseLong(1.5f));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseBigInteger(1.5d));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseBigInteger(new BigDecimal("1.5")));
	}

	@Test
	public void testFractionalStringFailsForIntegralTypes() {
		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt("1.5"));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseLong("1.0"));
	}

	@Test
	public void testByteRange() {
		assertEquals(Byte.valueOf(Byte.MIN_VALUE), NumberParse.parseByte(Byte.MIN_VALUE));
		assertEquals(Byte.valueOf(Byte.MAX_VALUE), NumberParse.parseByte("127"));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseByte(128));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseByte(-129));
	}

	@Test
	public void testShortRange() {
		assertEquals(Short.valueOf(Short.MIN_VALUE), NumberParse.parseShort(Short.MIN_VALUE));
		assertEquals(Short.valueOf(Short.MAX_VALUE), NumberParse.parseShort("32767"));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseShort(32768));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseShort(-32769));
	}

	@Test
	public void testIntegerAndLongRange() {
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), NumberParse.parseInt(Integer.MIN_VALUE));
		assertEquals(Integer.valueOf(Integer.MAX_VALUE), NumberParse.parseInt("2147483647"));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt(2147483648L));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt("2147483648"));

		assertEquals(Long.valueOf(Long.MIN_VALUE), NumberParse.parseLong(Long.MIN_VALUE));
		assertEquals(Long.valueOf(Long.MAX_VALUE), NumberParse.parseLong(Long.MAX_VALUE));
		assertThrows(NumberFormatException.class,
				() -> NumberParse.parseLong(new BigInteger("9223372036854775808")));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseLong(1e20));
	}

	@Test
	public void testNullBehavior() {
		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt(null));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseDouble(null));

		assertEquals(Integer.valueOf(7), NumberParse.parseInt(null, 7));
		assertEquals(Byte.valueOf((byte) 1), NumberParse.parseByte(null, (byte) 1));
		assertNull(NumberParse.parseIntOrNull(null));
		assertNull(NumberParse.parseBigDecimalOrNull(null));
	}

	@Test
	public void testDefaultValueOnInvalidInput() {
		assertEquals(Byte.valueOf((byte) 1), NumberParse.parseByte("x", (byte) 1));
		assertEquals(Short.valueOf((short) 2), NumberParse.parseShort("x", (short) 2));
		assertEquals(Integer.valueOf(3), NumberParse.parseInt("x", 3));
		assertEquals(Long.valueOf(4L), NumberParse.parseLong("x", 4L));
		assertEquals(Float.valueOf(5.5f), NumberParse.parseFloat("x", 5.5f));
		assertEquals(Double.valueOf(6.6d), NumberParse.parseDouble("x", 6.6d));
		assertEquals(BigInteger.TEN, NumberParse.parseBigInteger("x", BigInteger.TEN));
		assertEquals(new BigDecimal("7.7"), NumberParse.parseBigDecimal("x", new BigDecimal("7.7")));
	}

	@Test
	public void testOrNullOnInvalidInput() {
		assertNull(NumberParse.parseByteOrNull("x"));
		assertNull(NumberParse.parseShortOrNull("x"));
		assertNull(NumberParse.parseIntOrNull("x"));
		assertNull(NumberParse.parseLongOrNull("x"));
		assertNull(NumberParse.parseFloatOrNull("x"));
		assertNull(NumberParse.parseDoubleOrNull("x"));
		assertNull(NumberParse.parseBigIntegerOrNull("x"));
		assertNull(NumberParse.parseBigDecimalOrNull("x"));
	}

	@Test
	public void testInvalidText() {
		NumberFormatException exception = assertThrows(NumberFormatException.class,
				() -> NumberParse.parseInt("abc"));
		assertTrue(exception.getMessage().contains("Integer"));
		assertTrue(exception.getMessage().contains("abc"));

		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt(""));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseInt("   "));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseBigInteger("1.5"));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseBigDecimal("abc"));
	}

	@Test
	public void testArbitraryObject() {
		Object integerText = new Object() {
			@Override
			public String toString() {
				return " 42 ";
			}
		};
		Object decimalText = new Object() {
			@Override
			public String toString() {
				return "3.14";
			}
		};
		assertEquals(Integer.valueOf(42), NumberParse.parseInt(integerText));
		assertEquals(new BigDecimal("3.14"), NumberParse.parseBigDecimal(decimalText));
		assertEquals(Integer.valueOf(0), NumberParse.parseInt(decimalText, 0));
	}

	@Test
	public void testNumberSubclass() {
		assertEquals(Integer.valueOf(7), NumberParse.parseInt(new AtomicInteger(7)));
		assertEquals(Long.valueOf(Long.MAX_VALUE), NumberParse.parseLong(new AtomicLong(Long.MAX_VALUE)));
	}

	@Test
	public void testFloatAndDoubleSpecialValues() {
		assertTrue(Float.isNaN(NumberParse.parseFloat("NaN")));
		assertTrue(Double.isNaN(NumberParse.parseDouble("NaN")));
		assertTrue(Float.isInfinite(NumberParse.parseFloat("Infinity")));
		assertTrue(Double.isInfinite(NumberParse.parseDouble("-Infinity")));

		assertThrows(NumberFormatException.class, () -> NumberParse.parseBigDecimal(Double.NaN));
		assertThrows(NumberFormatException.class, () -> NumberParse.parseBigDecimal(Float.POSITIVE_INFINITY));
	}

	@Test
	public void testBigNumberPrecision() {
		BigInteger bigInteger = new BigInteger("123456789012345678901234567890");
		assertEquals(bigInteger, NumberParse.parseBigInteger(bigInteger));
		assertEquals(bigInteger, NumberParse.parseBigInteger(bigInteger.toString()));

		BigDecimal bigDecimal = new BigDecimal("12345678901234567890.123");
		assertEquals(bigDecimal, NumberParse.parseBigDecimal(bigDecimal));
		assertEquals(bigDecimal, NumberParse.parseBigDecimal(bigDecimal.toString()));

		assertEquals(new BigInteger("100000000000000000000"), NumberParse.parseBigInteger(new BigDecimal("1E+20")));
	}

	@Test
	public void testLenientIntegralConversion() {
		assertEquals(Byte.valueOf((byte) 1), NumberParse.toByte(1.5d));
		assertEquals(Short.valueOf((short) -1), NumberParse.toShort(-1.5d));
		assertEquals(Integer.valueOf(1), NumberParse.toInt(1.5d));
		assertEquals(Integer.valueOf(-1), NumberParse.toInt(-1.5d));
		assertEquals(Long.valueOf(1L), NumberParse.toLong(1.5f));
		assertEquals(Long.valueOf(0L), NumberParse.toLong(Double.NaN));

		assertEquals(Byte.valueOf((byte) 44), NumberParse.toByte(300));
		assertEquals(Short.valueOf((short) -32768), NumberParse.toShort(32768));
		assertEquals(Integer.valueOf(Integer.MIN_VALUE), NumberParse.toInt(2147483648L));
		assertEquals(Long.valueOf(Long.MIN_VALUE), NumberParse.toLong(new BigInteger("9223372036854775808")));
	}

	@Test
	public void testLenientStringAndNull() {
		assertEquals(Byte.valueOf((byte) 1), NumberParse.toByte("1.5"));
		assertEquals(Integer.valueOf(1), NumberParse.toInt("1.5"));
		assertEquals(Integer.valueOf(1000), NumberParse.toInt("1e3"));
		assertEquals(Byte.valueOf((byte) 44), NumberParse.toByte("300"));
		assertEquals(Long.valueOf(Long.MIN_VALUE), NumberParse.toLong("9223372036854775808"));

		assertNull(NumberParse.toByte(null));
		assertNull(NumberParse.toShort(""));
		assertNull(NumberParse.toInt("   "));
		assertNull(NumberParse.toLong("abc"));
		assertNull(NumberParse.toFloat("abc"));
		assertNull(NumberParse.toDouble(null));

		Object text = new Object() {
			@Override
			public String toString() {
				return " 42 ";
			}
		};
		assertEquals(Integer.valueOf(42), NumberParse.toInt(text));
	}

	@Test
	public void testLenientFloatDoubleAndBigNumber() {
		assertEquals(Float.valueOf(1.5f), NumberParse.toFloat("1.5"));
		assertEquals(Double.valueOf(1.5d), NumberParse.toDouble("1.5"));
		assertTrue(Float.isNaN(NumberParse.toFloat("NaN")));
		assertTrue(Float.isNaN(NumberParse.toFloat(Double.NaN)));
		assertTrue(Double.isInfinite(NumberParse.toDouble("Infinity")));
		assertTrue(Double.isInfinite(NumberParse.toDouble(Float.POSITIVE_INFINITY)));

		assertEquals(new BigInteger("1"), NumberParse.toBigInteger(1.5d));
		assertEquals(new BigInteger("-1"), NumberParse.toBigInteger(-1.5d));
		assertEquals(new BigInteger("123"), NumberParse.toBigInteger(new BigDecimal("123.9")));
		assertEquals(new BigInteger("9223372036854775808"),
				NumberParse.toBigInteger(new BigInteger("9223372036854775808")));

		assertEquals(new BigDecimal("0.1"), NumberParse.toBigDecimal(0.1d));
		assertEquals(new BigDecimal("0.1"), NumberParse.toBigDecimal(0.1f));
		assertEquals(new BigDecimal("123.45"), NumberParse.toBigDecimal("123.45"));
		assertEquals(new BigDecimal("9223372036854775807"),
				NumberParse.toBigDecimal(new AtomicLong(Long.MAX_VALUE)));

		assertNull(NumberParse.toBigInteger(Double.NaN));
		assertNull(NumberParse.toBigDecimal(Double.POSITIVE_INFINITY));
		assertNull(NumberParse.toBigDecimal("abc"));
	}
}
