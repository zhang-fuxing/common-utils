package com.github.zhangfuxing.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据大小格式化与解析，在字节数与可读文本之间互转
 * <p>
 * 格式化：{@code format(1536)} 返回 {@code "1.5 KB"}
 * <p>
 * 解析：{@code parse("300MB")} 返回 {@code 314572800}
 * <p>
 * 单位换算：{@code convert("300MB", DataSizeUnit.GB)} 返回 {@code "0.29 GB"}
 * <p>
 * 默认按 1024 进制（B / KB / MB / GB / TB / PB / EB），保留 2 位小数并去掉多余的 0；
 * 可通过 {@link #setBase(long)} 切换为 1000 进制，通过 {@link #setScale(int)} 调整小数位数。
 * 进制同时作用于格式化和解析，保证 {@code parse(format(bytes)) == bytes}。
 * <p>
 * 实例配置完成后是不可变的，可安全地在多线程间共享。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/9
 * @email zhangfuxing1010@163.com
 */
public class DataSizeFormat {

	/**
	 * 二进制进制：1024
	 */
	public static final long BINARY_BASE = 1024L;

	/**
	 * 十进制进制：1000
	 */
	public static final long DECIMAL_BASE = 1000L;

	/**
	 * 默认保留的小数位数
	 */
	public static final int DEFAULT_SCALE = 2;

	/**
	 * 允许的最大小数位数
	 */
	private static final int MAX_SCALE = 10;

	/**
	 * 解析用的数字 + 单位，如 "300MB"、"-1.5GB"、"1024"
	 */
	private static final Pattern SIZE_PATTERN = Pattern.compile("^([+-]?)(\\d+(?:\\.\\d+)?)([A-Za-z\\u4e00-\\u9fa5]*)$");

	/**
	 * 需要从输入中剔除的空白字符（含全角空格、不间断空格）
	 */
	private static final char[] BLANKS = {' ', '\t', '\r', '\n', '\u00A0', '\u3000'};

	/**
	 * 进制，默认 1024
	 */
	private long base = BINARY_BASE;

	/**
	 * 小数位数，默认 2
	 */
	private int scale = DEFAULT_SCALE;

	/**
	 * 是否去掉小数末尾多余的 0，默认 true（1.50 KB -> 1.5 KB，1.00 KB -> 1 KB）
	 */
	private boolean stripTrailingZeros = true;

	/**
	 * 数值与单位之间是否加空格，默认 true（1.5 KB）；false 时输出 1.5KB
	 */
	private boolean space = true;

	public DataSizeFormat() {
	}

	/**
	 * @param base  进制，如 {@link #BINARY_BASE}、{@link #DECIMAL_BASE}
	 * @param scale 小数位数
	 */
	public DataSizeFormat(long base, int scale) {
		setBase(base);
		setScale(scale);
	}

	// ------------------------------------------------------------------ 格式化

	/**
	 * 格式化数据大小，自动选择合适的单位
	 * <p>
	 * 例：0 -> "0 B"，1023 -> "1023 B"，1536 -> "1.5 KB"，-1536 -> "-1.5 KB"
	 *
	 * @param bytes 字节数，负数表示负的大小（输出带负号）
	 *
	 * @return 格式化后的文本
	 */
	public String format(long bytes) {
		BigDecimal size = BigDecimal.valueOf(bytes).abs();
		BigDecimal baseValue = BigDecimal.valueOf(base);

		// 四舍五入后仍然超过进制，才进位到下一个单位，避免出现 "1000.00 KB" 这种结果
		int index = 0;
		BigDecimal display = size.setScale(scale, RoundingMode.HALF_UP);
		while (index < DataSizeUnit.values().length - 1 && display.compareTo(baseValue) >= 0) {
			size = size.divide(baseValue, scale + 5, RoundingMode.HALF_UP);
			index++;
			display = size.setScale(scale, RoundingMode.HALF_UP);
		}
		return text(bytes < 0, display, DataSizeUnit.values()[index]);
	}

	/**
	 * 格式化数据大小，强制使用指定单位
	 * <p>
	 * 例：format(314572800, DataSizeUnit.GB) 返回 "0.29 GB"
	 *
	 * @param bytes 字节数
	 * @param unit  目标单位
	 *
	 * @return 格式化后的文本
	 */
	public String format(long bytes, DataSizeUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit must not be null");
		}
		return text(bytes < 0, toUnitValue(bytes, unit), unit);
	}

	/**
	 * 单位换算：把字节数换算成指定单位的数值
	 * <p>
	 * 例：toUnit(314572800, DataSizeUnit.GB) 返回 0.29296875
	 *
	 * @param bytes 字节数
	 * @param unit  目标单位
	 *
	 * @return 该单位下的数值（精确值，已去掉末尾多余的 0；格式化请用 {@link #format(long, DataSizeUnit)}）
	 */
	public BigDecimal toUnit(long bytes, DataSizeUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit must not be null");
		}
		BigDecimal value = exactDivide(BigDecimal.valueOf(bytes).abs(), unit);
		return bytes < 0 ? value.negate() : value;
	}

	// -------------------------------------------------------------------- 解析

	/**
	 * 解析数据大小文本为字节数
	 * <p>
	 * 例："300MB" -> 314572800，"1.5 GB" -> 1610612736，"1024" -> 1024（无单位按字节处理），
	 * "-1.5kb" -> -1536，"512字节" -> 512
	 * <p>
	 * 单位忽略大小写，支持单字母（K/M/G）、双字母（KB/MB）、IEC（KiB/MiB）与中文别名；
	 * 进制使用当前实例配置，因此 {@code parse(format(bytes)) == bytes}。
	 *
	 * @param text 数据大小文本，允许数字与单位之间有空白
	 *
	 * @return 字节数
	 *
	 * @throws IllegalArgumentException 文本为空、格式非法、单位无法识别或结果超出 long 范围
	 */
	public long parse(String text) {
		BigDecimal bytes = parseValue(text).setScale(0, RoundingMode.HALF_UP);
		try {
			return bytes.longValueExact();
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException("数据大小超出 long 范围: " + text, e);
		}
	}

	/**
	 * 解析并换算到指定单位，返回格式化后的文本
	 * <p>
	 * 例：convert("300MB", DataSizeUnit.GB) 返回 "0.29 GB"
	 *
	 * @param text 数据大小文本
	 * @param unit 目标单位
	 *
	 * @return 目标单位下的格式化文本
	 */
	public String convert(String text, DataSizeUnit unit) {
		return format(parse(text), unit);
	}

	/**
	 * 解析并换算到指定单位，返回数值
	 * <p>
	 * 例：convertTo("300MB", DataSizeUnit.GB) 返回 0.29296875
	 *
	 * @param text 数据大小文本
	 * @param unit 目标单位
	 *
	 * @return 目标单位下的数值（精确值，已去掉末尾多余的 0）
	 */
	public BigDecimal convertTo(String text, DataSizeUnit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit must not be null");
		}
		return exactDivide(parseValue(text), unit);
	}

	// -------------------------------------------------------------------- 配置

	/**
	 * @return 进制
	 */
	public long getBase() {
		return base;
	}

	/**
	 * @param base 进制，不能小于 2
	 */
	public DataSizeFormat setBase(long base) {
		if (base < 2) {
			throw new IllegalArgumentException("base must be >= 2, but was " + base);
		}
		this.base = base;
		return this;
	}

	/**
	 * @return 小数位数
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * @param scale 小数位数，范围 0 ~ 10
	 */
	public DataSizeFormat setScale(int scale) {
		if (scale < 0 || scale > MAX_SCALE) {
			throw new IllegalArgumentException("scale must be between 0 and " + MAX_SCALE + ", but was " + scale);
		}
		this.scale = scale;
		return this;
	}

	/**
	 * @return 是否去掉小数末尾多余的 0
	 */
	public boolean isStripTrailingZeros() {
		return stripTrailingZeros;
	}

	/**
	 * @param stripTrailingZeros 是否去掉小数末尾多余的 0
	 */
	public DataSizeFormat setStripTrailingZeros(boolean stripTrailingZeros) {
		this.stripTrailingZeros = stripTrailingZeros;
		return this;
	}

	/**
	 * @return 数值与单位之间是否加空格
	 */
	public boolean isSpace() {
		return space;
	}

	/**
	 * @param space 数值与单位之间是否加空格
	 */
	public DataSizeFormat setSpace(boolean space) {
		this.space = space;
		return this;
	}

	/**
	 * 精确换算到指定单位；进制导致除不尽时（如自定义 base = 1023）退化为保留 10 位小数
	 */
	private BigDecimal exactDivide(BigDecimal bytes, DataSizeUnit unit) {
		BigDecimal factor = unit.factor(base);
		BigDecimal value;
		try {
			value = bytes.divide(factor);
		} catch (ArithmeticException e) {
			value = bytes.divide(factor, MAX_SCALE, RoundingMode.HALF_UP);
		}
		BigDecimal stripped = value.stripTrailingZeros();
		return stripped.signum() == 0 ? BigDecimal.ZERO : stripped;
	}

	/**
	 * 按当前小数位配置把字节数换算成指定单位的数值（恒为非负）
	 */
	private BigDecimal toUnitValue(long bytes, DataSizeUnit unit) {
		BigDecimal size = BigDecimal.valueOf(bytes).abs();
		return size.divide(unit.factor(base), scale + 5, RoundingMode.HALF_UP).setScale(scale, RoundingMode.HALF_UP);
	}

	/**
	 * 解析文本为字节数（BigDecimal，未做取整与范围校验）
	 */
	private BigDecimal parseValue(String text) {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null");
		}
		String normalized = normalize(text);
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("text must not be blank");
		}
		Matcher matcher = SIZE_PATTERN.matcher(normalized);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("无法解析的数据大小: " + text);
		}
		BigDecimal value = new BigDecimal(matcher.group(1) + matcher.group(2));
		DataSizeUnit unit = DataSizeUnit.of(matcher.group(3));
		return value.multiply(unit.factor(base));
	}

	/**
	 * 去掉所有空白字符，便于解析 "1.5 GB" 这类输入
	 */
	private static String normalize(String text) {
		String trimmed = text.trim();
		StringBuilder sb = new StringBuilder(trimmed.length());
		for (int i = 0; i < trimmed.length(); i++) {
			char c = trimmed.charAt(i);
			if (!isBlank(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static boolean isBlank(char c) {
		for (char blank : BLANKS) {
			if (c == blank) {
				return true;
			}
		}
		return Character.isWhitespace(c);
	}

	/**
	 * 拼装最终文本
	 */
	private String text(boolean negative, BigDecimal display, DataSizeUnit unit) {
		StringBuilder sb = new StringBuilder(16);
		if (negative) {
			sb.append('-');
		}
		sb.append(numberText(display));
		if (space) {
			sb.append(' ');
		}
		return sb.append(unit.getName()).toString();
	}

	/**
	 * 数字文本，按配置去掉末尾多余的 0
	 */
	private String numberText(BigDecimal value) {
		String text = value.toPlainString();
		if (!stripTrailingZeros || text.indexOf('.') < 0) {
			return text;
		}
		int end = text.length();
		while (end > 0 && text.charAt(end - 1) == '0') {
			end--;
		}
		if (end > 0 && text.charAt(end - 1) == '.') {
			end--;
		}
		return text.substring(0, end);
	}
}
