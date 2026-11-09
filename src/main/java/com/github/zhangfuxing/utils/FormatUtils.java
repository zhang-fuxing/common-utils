package com.github.zhangfuxing.utils;

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
}
