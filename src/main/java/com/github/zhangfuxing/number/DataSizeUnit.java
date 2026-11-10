package com.github.zhangfuxing.number;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * 数据大小单位，从字节开始逐级放大
 * <p>
 * 单位只表示「第几级」，具体倍数由进制决定：{@code KB} 在 1024 进制下是 1024 字节，在 1000 进制下是 1000 字节。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/10
 * @email zhangfuxing1010@163.com
 */
public enum DataSizeUnit {

	/**
	 * 字节
	 */
	B("B"),

	/**
	 * 千字节
	 */
	KB("KB"),

	/**
	 * 兆字节
	 */
	MB("MB"),

	/**
	 * 吉字节
	 */
	GB("GB"),

	/**
	 * 太字节
	 */
	TB("TB"),

	/**
	 * 拍字节
	 */
	PB("PB"),

	/**
	 * 艾字节
	 */
	EB("EB");

	private final String name;

	DataSizeUnit(String name) {
		this.name = name;
	}

	/**
	 * @return 单位名称，如 "MB"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return 单位级别，B 为 0，KB 为 1，依此类推
	 */
	public int getIndex() {
		return ordinal();
	}

	/**
	 * 计算该单位在指定进制下的字节倍数，如 MB 在 1024 进制下为 1048576
	 *
	 * @param base 进制，如 1024、1000
	 *
	 * @return 字节倍数
	 */
	public BigDecimal factor(long base) {
		return BigDecimal.valueOf(base).pow(ordinal());
	}

	/**
	 * 解析单位文本，忽略大小写，支持常见别名
	 * <p>
	 * 例："MB"、"mb"、"M"、"MiB" 均解析为 {@link #MB}；"字节"、"B" 解析为 {@link #B}；空文本默认 {@link #B}
	 *
	 * @param text 单位文本
	 *
	 * @return 数据大小单位
	 *
	 * @throws IllegalArgumentException 无法识别的单位
	 */
	public static DataSizeUnit of(String text) {
		if (text == null || text.trim().isEmpty()) {
			return B;
		}
		switch (text.trim().toUpperCase(Locale.ROOT)) {
			case "B":
			case "BYTE":
			case "BYTES":
			case "字节":
				return B;
			case "K":
			case "KB":
			case "KIB":
			case "千字节":
				return KB;
			case "M":
			case "MB":
			case "MIB":
			case "兆字节":
				return MB;
			case "G":
			case "GB":
			case "GIB":
			case "吉字节":
				return GB;
			case "T":
			case "TB":
			case "TIB":
			case "太字节":
				return TB;
			case "P":
			case "PB":
			case "PIB":
			case "拍字节":
				return PB;
			case "E":
			case "EB":
			case "EIB":
			case "艾字节":
				return EB;
			default:
				throw new IllegalArgumentException("未知的数据大小单位: " + text);
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
