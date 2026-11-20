package com.github.zhangfuxing.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * 数字解析工具，把任意对象解析为指定数字类型
 * <p>
 * 提供两套 API：
 * <ul>
 *     <li>{@code parseXxx}：严格解析，要求值精确且在目标类型的取值范围内，失败抛 {@link NumberFormatException}</li>
 *     <li>{@code toXxx}：宽松转换，{@link Number} 直接使用 {@code byteValue()} / {@code intValue()} 等窄化语义，
 *     允许小数截断和定长整型溢出回绕；无法转换时返回 {@code null}</li>
 * </ul>
 * <p>
 * 严格解析规则：
 * <ul>
 *     <li>字符串：先去除首尾空白，再交给目标类型的标准解析方法（如 {@link Integer#parseInt(String)}）</li>
 *     <li>{@link Number}：按数值转换；整型目标要求值必须是整数且在目标类型的取值范围内，否则抛异常</li>
 *     <li>其他对象：先转为字符串，再按字符串规则解析</li>
 *     <li>{@code null}：解析失败，抛 {@link NumberFormatException}；带默认值或 OrNull 的方法返回默认值 / null</li>
 * </ul>
 * <p>
 * 例：
 * <pre>{@code
 * NumberParse.parseInt("123");                        // 123
 * NumberParse.parseInt(123L);                         // 123
 * NumberParse.parseInt(new BigDecimal("123.00"));     // 123
 * NumberParse.parseInt("abc", 0);                     // 0
 * NumberParse.parseIntOrNull("abc");                  // null
 * NumberParse.parseBigInteger("12345678901234567890"); // 12345678901234567890
 *
 * NumberParse.toByte(1.5d);                           // 1
 * NumberParse.toByte(300);                            // 44
 * NumberParse.toInt("1.5");                           // 1
 * NumberParse.toInt("abc");                           // null
 * }</pre>
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public class NumberParse {

	private NumberParse() {
	}

	// -------------------------------------------------------------------- Byte

	/**
	 * 解析为 Byte
	 *
	 * @param input 任意对象
	 *
	 * @return Byte 值
	 *
	 * @throws NumberFormatException 输入为 null、格式非法、不是整数或超出 Byte 范围
	 */
	public static Byte parseByte(Object input) {
		if (input instanceof Number) {
			return (byte) numberToLong((Number) input, Byte.MIN_VALUE, Byte.MAX_VALUE, "Byte");
		}
		return parseText(input, "Byte", Byte::parseByte);
	}

	/**
	 * 解析为 Byte，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return Byte 值或默认值
	 */
	public static Byte parseByte(Object input, Byte defaultValue) {
		return parseOrDefault(input, NumberParse::parseByte, defaultValue);
	}

	/**
	 * 解析为 Byte，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return Byte 值或 null
	 */
	public static Byte parseByteOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseByte, null);
	}

	// ------------------------------------------------------------------- Short

	/**
	 * 解析为 Short
	 *
	 * @param input 任意对象
	 *
	 * @return Short 值
	 *
	 * @throws NumberFormatException 输入为 null、格式非法、不是整数或超出 Short 范围
	 */
	public static Short parseShort(Object input) {
		if (input instanceof Number) {
			return (short) numberToLong((Number) input, Short.MIN_VALUE, Short.MAX_VALUE, "Short");
		}
		return parseText(input, "Short", Short::parseShort);
	}

	/**
	 * 解析为 Short，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return Short 值或默认值
	 */
	public static Short parseShort(Object input, Short defaultValue) {
		return parseOrDefault(input, NumberParse::parseShort, defaultValue);
	}

	/**
	 * 解析为 Short，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return Short 值或 null
	 */
	public static Short parseShortOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseShort, null);
	}

	// ------------------------------------------------------------------- Integer

	/**
	 * 解析为 Integer
	 *
	 * @param input 任意对象
	 *
	 * @return Integer 值
	 *
	 * @throws NumberFormatException 输入为 null、格式非法、不是整数或超出 Integer 范围
	 */
	public static Integer parseInt(Object input) {
		if (input instanceof Number) {
			return (int) numberToLong((Number) input, Integer.MIN_VALUE, Integer.MAX_VALUE, "Integer");
		}
		return parseText(input, "Integer", Integer::parseInt);
	}

	/**
	 * 解析为 Integer，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return Integer 值或默认值
	 */
	public static Integer parseInt(Object input, Integer defaultValue) {
		return parseOrDefault(input, NumberParse::parseInt, defaultValue);
	}

	/**
	 * 解析为 Integer，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return Integer 值或 null
	 */
	public static Integer parseIntOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseInt, null);
	}

	// ---------------------------------------------------------------------- Long

	/**
	 * 解析为 Long
	 *
	 * @param input 任意对象
	 *
	 * @return Long 值
	 *
	 * @throws NumberFormatException 输入为 null、格式非法、不是整数或超出 Long 范围
	 */
	public static Long parseLong(Object input) {
		if (input instanceof Number) {
			return numberToLong((Number) input, Long.MIN_VALUE, Long.MAX_VALUE, "Long");
		}
		return parseText(input, "Long", Long::parseLong);
	}

	/**
	 * 解析为 Long，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return Long 值或默认值
	 */
	public static Long parseLong(Object input, Long defaultValue) {
		return parseOrDefault(input, NumberParse::parseLong, defaultValue);
	}

	/**
	 * 解析为 Long，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return Long 值或 null
	 */
	public static Long parseLongOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseLong, null);
	}

	// --------------------------------------------------------------------- Float

	/**
	 * 解析为 Float
	 *
	 * @param input 任意对象
	 *
	 * @return Float 值
	 *
	 * @throws NumberFormatException 输入为 null 或格式非法
	 */
	public static Float parseFloat(Object input) {
		if (input instanceof Number) {
			return ((Number) input).floatValue();
		}
		return parseText(input, "Float", Float::parseFloat);
	}

	/**
	 * 解析为 Float，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return Float 值或默认值
	 */
	public static Float parseFloat(Object input, Float defaultValue) {
		return parseOrDefault(input, NumberParse::parseFloat, defaultValue);
	}

	/**
	 * 解析为 Float，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return Float 值或 null
	 */
	public static Float parseFloatOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseFloat, null);
	}

	// -------------------------------------------------------------------- Double

	/**
	 * 解析为 Double
	 *
	 * @param input 任意对象
	 *
	 * @return Double 值
	 *
	 * @throws NumberFormatException 输入为 null 或格式非法
	 */
	public static Double parseDouble(Object input) {
		if (input instanceof Number) {
			return ((Number) input).doubleValue();
		}
		return parseText(input, "Double", Double::parseDouble);
	}

	/**
	 * 解析为 Double，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return Double 值或默认值
	 */
	public static Double parseDouble(Object input, Double defaultValue) {
		return parseOrDefault(input, NumberParse::parseDouble, defaultValue);
	}

	/**
	 * 解析为 Double，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return Double 值或 null
	 */
	public static Double parseDoubleOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseDouble, null);
	}

	// ---------------------------------------------------------------- BigInteger

	/**
	 * 解析为 BigInteger
	 *
	 * @param input 任意对象
	 *
	 * @return BigInteger 值
	 *
	 * @throws NumberFormatException 输入为 null、格式非法或不是整数
	 */
	public static BigInteger parseBigInteger(Object input) {
		if (input instanceof Number) {
			return numberToBigInteger((Number) input, "BigInteger");
		}
		return parseText(input, "BigInteger", BigInteger::new);
	}

	/**
	 * 解析为 BigInteger，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return BigInteger 值或默认值
	 */
	public static BigInteger parseBigInteger(Object input, BigInteger defaultValue) {
		return parseOrDefault(input, NumberParse::parseBigInteger, defaultValue);
	}

	/**
	 * 解析为 BigInteger，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return BigInteger 值或 null
	 */
	public static BigInteger parseBigIntegerOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseBigInteger, null);
	}

	// --------------------------------------------------------------- BigDecimal

	/**
	 * 解析为 BigDecimal
	 *
	 * @param input 任意对象
	 *
	 * @return BigDecimal 值
	 *
	 * @throws NumberFormatException 输入为 null、格式非法，或输入为 NaN / Infinity
	 */
	public static BigDecimal parseBigDecimal(Object input) {
		if (input instanceof Number) {
			return numberToBigDecimal((Number) input, "BigDecimal");
		}
		return parseText(input, "BigDecimal", BigDecimal::new);
	}

	/**
	 * 解析为 BigDecimal，失败或输入为 null 时返回默认值
	 *
	 * @param input        任意对象
	 * @param defaultValue 默认值
	 *
	 * @return BigDecimal 值或默认值
	 */
	public static BigDecimal parseBigDecimal(Object input, BigDecimal defaultValue) {
		return parseOrDefault(input, NumberParse::parseBigDecimal, defaultValue);
	}

	/**
	 * 解析为 BigDecimal，失败或输入为 null 时返回 null
	 *
	 * @param input 任意对象
	 *
	 * @return BigDecimal 值或 null
	 */
	public static BigDecimal parseBigDecimalOrNull(Object input) {
		return parseOrDefault(input, NumberParse::parseBigDecimal, null);
	}

	// ---------------------------------------------------------------- 宽松转换

	/**
	 * 宽松转换为 Byte，无法转换时返回 null
	 * <p>
	 * 输入为 {@link Number} 时等价于 {@code ((Number) input).byteValue()}，允许小数截断和溢出回绕；
	 * 其他对象按字符串解析为数字后转换。
	 *
	 * @param input 任意对象
	 *
	 * @return Byte 值，无法转换时返回 null
	 */
	public static Byte toByte(Object input) {
		Number number = toNumber(input);
		return number == null ? null : number.byteValue();
	}

	/**
	 * 宽松转换为 Short，无法转换时返回 null
	 * <p>
	 * 输入为 {@link Number} 时等价于 {@code ((Number) input).shortValue()}，允许小数截断和溢出回绕。
	 *
	 * @param input 任意对象
	 *
	 * @return Short 值，无法转换时返回 null
	 */
	public static Short toShort(Object input) {
		Number number = toNumber(input);
		return number == null ? null : number.shortValue();
	}

	/**
	 * 宽松转换为 Integer，无法转换时返回 null
	 * <p>
	 * 输入为 {@link Number} 时等价于 {@code ((Number) input).intValue()}，允许小数截断和溢出回绕。
	 *
	 * @param input 任意对象
	 *
	 * @return Integer 值，无法转换时返回 null
	 */
	public static Integer toInt(Object input) {
		Number number = toNumber(input);
		return number == null ? null : number.intValue();
	}

	/**
	 * 宽松转换为 Long，无法转换时返回 null
	 * <p>
	 * 输入为 {@link Number} 时等价于 {@code ((Number) input).longValue()}，允许小数截断和溢出回绕。
	 *
	 * @param input 任意对象
	 *
	 * @return Long 值，无法转换时返回 null
	 */
	public static Long toLong(Object input) {
		Number number = toNumber(input);
		return number == null ? null : number.longValue();
	}

	/**
	 * 宽松转换为 Float，无法转换时返回 null
	 * <p>
	 * 输入为 {@link Number} 时使用 {@link Number#floatValue()}；
	 * 其他对象按 {@link Float#parseFloat(String)} 规则解析，支持 NaN、Infinity 和十六进制浮点。
	 *
	 * @param input 任意对象
	 *
	 * @return Float 值，无法转换时返回 null
	 */
	public static Float toFloat(Object input) {
		if (input instanceof Number) {
			return ((Number) input).floatValue();
		}
		String text = lenientText(input);
		if (text == null) {
			return null;
		}
		try {
			return Float.valueOf(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 宽松转换为 Double，无法转换时返回 null
	 * <p>
	 * 输入为 {@link Number} 时使用 {@link Number#doubleValue()}；
	 * 其他对象按 {@link Double#parseDouble(String)} 规则解析，支持 NaN、Infinity 和十六进制浮点。
	 *
	 * @param input 任意对象
	 *
	 * @return Double 值，无法转换时返回 null
	 */
	public static Double toDouble(Object input) {
		if (input instanceof Number) {
			return ((Number) input).doubleValue();
		}
		String text = lenientText(input);
		if (text == null) {
			return null;
		}
		try {
			return Double.valueOf(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 宽松转换为 BigInteger，小数向零截断，无法转换时返回 null
	 * <p>
	 * 例如 {@code toBigInteger(1.5)} 返回 1，{@code toBigInteger(-1.5)} 返回 -1。
	 *
	 * @param input 任意对象
	 *
	 * @return BigInteger 值，无法转换时返回 null
	 */
	public static BigInteger toBigInteger(Object input) {
		BigDecimal value = toBigDecimal(input);
		return value == null ? null : value.toBigInteger();
	}

	/**
	 * 宽松转换为 BigDecimal，无法转换或输入为 NaN / Infinity 时返回 null
	 * <p>
	 * Float / Double 使用十进制字符串构造，避免二进制浮点误差；例如 {@code toBigDecimal(0.1f)}
	 * 返回 {@code 0.1}。
	 *
	 * @param input 任意对象
	 *
	 * @return BigDecimal 值，无法转换时返回 null
	 */
	public static BigDecimal toBigDecimal(Object input) {
		Number number = toNumber(input);
		if (number == null) {
			return null;
		}
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		}
		if (number instanceof BigInteger) {
			return new BigDecimal((BigInteger) number);
		}
		if (number instanceof Byte || number instanceof Short
				|| number instanceof Integer || number instanceof Long) {
			return BigDecimal.valueOf(number.longValue());
		}
		if (number instanceof Float) {
			float value = number.floatValue();
			return Float.isFinite(value) ? new BigDecimal(Float.toString(value)) : null;
		}
		if (number instanceof Double) {
			double value = number.doubleValue();
			return Double.isFinite(value) ? BigDecimal.valueOf(value) : null;
		}
		try {
			return new BigDecimal(number.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// -------------------------------------------------------------------- 内部

	/**
	 * 宽松转换：任意对象转为 Number，无法转换时返回 null
	 */
	private static Number toNumber(Object input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Number) {
			return (Number) input;
		}
		String text = lenientText(input);
		if (text == null) {
			return null;
		}
		try {
			return new BigDecimal(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 宽松取文本：null、空串或空白串返回 null
	 */
	private static String lenientText(Object input) {
		if (input == null) {
			return null;
		}
		String text = input.toString().trim();
		return text.isEmpty() ? null : text;
	}

	/**
	 * 字符串解析：去空白、空串校验，并把底层异常包装成带输入信息的 NumberFormatException
	 */
	private static <T> T parseText(Object input, String target, Function<String, T> parser) {
		String text = text(input, target);
		try {
			return parser.apply(text);
		} catch (NumberFormatException e) {
			throw formatException(input, target, e);
		}
	}

	/**
	 * 把任意输入转为待解析的字符串
	 */
	private static String text(Object input, String target) {
		if (input == null) {
			throw formatException(null, target, null);
		}
		String text = input.toString().trim();
		if (text.isEmpty()) {
			throw formatException(input, target, null);
		}
		return text;
	}

	/**
	 * Number 转 long：要求数值为整数并落在 [min, max] 内
	 */
	private static long numberToLong(Number input, long min, long max, String target) {
		BigInteger value = numberToBigInteger(input, target);
		if (value.compareTo(BigInteger.valueOf(min)) < 0 || value.compareTo(BigInteger.valueOf(max)) > 0) {
			throw formatException(input, target, null);
		}
		return value.longValue();
	}

	/**
	 * Number 转 BigInteger：小数（如 1.5）视为解析失败
	 */
	private static BigInteger numberToBigInteger(Number input, String target) {
		if (input instanceof BigInteger) {
			return (BigInteger) input;
		}
		if (input instanceof BigDecimal) {
			try {
				return ((BigDecimal) input).toBigIntegerExact();
			} catch (ArithmeticException e) {
				throw formatException(input, target, e);
			}
		}
		if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long) {
			return BigInteger.valueOf(input.longValue());
		}
		try {
			return new BigDecimal(input.toString()).toBigIntegerExact();
		} catch (NumberFormatException | ArithmeticException e) {
			throw formatException(input, target, e);
		}
	}

	/**
	 * Number 转 BigDecimal，保留浮点数的十进制表示
	 */
	private static BigDecimal numberToBigDecimal(Number input, String target) {
		if (input instanceof BigDecimal) {
			return (BigDecimal) input;
		}
		if (input instanceof BigInteger) {
			return new BigDecimal((BigInteger) input);
		}
		if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long) {
			return BigDecimal.valueOf(input.longValue());
		}
		if (input instanceof Float) {
			float value = (Float) input;
			if (!Float.isFinite(value)) {
				throw formatException(input, target, null);
			}
			return new BigDecimal(Float.toString(value));
		}
		if (input instanceof Double) {
			double value = (Double) input;
			if (!Double.isFinite(value)) {
				throw formatException(input, target, null);
			}
			return BigDecimal.valueOf(value);
		}
		try {
			return new BigDecimal(input.toString());
		} catch (NumberFormatException e) {
			throw formatException(input, target, e);
		}
	}

	/**
	 * 解析成功返回结果，失败（NumberFormatException）返回默认值
	 */
	private static <T> T parseOrDefault(Object input, Function<Object, T> parser, T defaultValue) {
		try {
			return parser.apply(input);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 构造带输入与目标类型信息的数字格式化异常
	 */
	private static NumberFormatException formatException(Object input, String target, Throwable cause) {
		NumberFormatException exception = new NumberFormatException("无法将 " + input + " 解析为 " + target);
		if (cause != null) {
			exception.initCause(cause);
		}
		return exception;
	}
}
