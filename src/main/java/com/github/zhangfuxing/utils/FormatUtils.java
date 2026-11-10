package com.github.zhangfuxing.utils;

import com.github.zhangfuxing.number.DataSizeFormat;
import com.github.zhangfuxing.number.DataSizeUnit;
import com.github.zhangfuxing.string.StringFormat;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/9
 * @email zhangfuxing1010@163.com
 */
public class FormatUtils {

	/**
	 * 默认使用 "null" 作为参数为 null 时的输出，线程安全（不做任何修改时只读）
	 */
	private static final StringFormat STRING_FORMAT = new StringFormat();

	/**
	 * 默认的数据大小格式化：1024 进制、2 位小数、去掉末尾多余的 0
	 */
	private static final DataSizeFormat DATA_SIZE_FORMAT = new DataSizeFormat();

	private FormatUtils() {
	}

	/**
	 * 顺序占位符格式化，例：formatString("hello, {}!", "world") 返回 "hello, world!"
	 *
	 * @see StringFormat#format(String, Object...)
	 */
	public static String formatString(String format, Object... args) {
		return STRING_FORMAT.format(format, args);
	}

	/**
	 * 索引占位符格式化，例：formatStringByIndex("hello, {0}!", "world") 返回 "hello, world!"
	 *
	 * @see StringFormat#formatByIndex(String, Object...)
	 */
	public static String formatStringByIndex(String format, Object... args) {
		return STRING_FORMAT.formatByIndex(format, args);
	}

	/**
	 * 命名占位符格式化，例：formatStringByNamed("hello, {name}!", "name", "world") 返回 "hello, world!"
	 *
	 * @see StringFormat#formatByNamed(String, Object...)
	 */
	public static String formatStringByNamed(String format, Object... args) {
		return STRING_FORMAT.formatByNamed(format, args);
	}

	/**
	 * 格式化数据大小，例：formatDataSize(1536) 返回 "1.5 KB"
	 *
	 * @see DataSizeFormat#format(long)
	 */
	public static String formatDataSize(long bytes) {
		return DATA_SIZE_FORMAT.format(bytes);
	}

	/**
	 * 格式化数据大小并指定小数位，例：formatDataSize(1536, 0) 返回 "2 KB"
	 *
	 * @see DataSizeFormat#setScale(int)
	 */
	public static String formatDataSize(long bytes, int scale) {
		return new DataSizeFormat().setScale(scale).format(bytes);
	}

	/**
	 * 解析数据大小文本为字节数，例：parseDataSize("300MB") 返回 314572800
	 *
	 * @see DataSizeFormat#parse(String)
	 */
	public static long parseDataSize(String text) {
		return DATA_SIZE_FORMAT.parse(text);
	}

	/**
	 * 数据大小单位换算，例：convertDataSize("300MB", "GB") 返回 "0.29 GB"
	 *
	 * @see DataSizeFormat#convert(String, DataSizeUnit)
	 */
	public static String convertDataSize(String text, String unit) {
		return DATA_SIZE_FORMAT.convert(text, DataSizeUnit.of(unit));
	}
}
