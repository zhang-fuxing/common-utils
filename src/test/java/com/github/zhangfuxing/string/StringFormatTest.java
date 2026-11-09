package com.github.zhangfuxing.string;

import junit.framework.TestCase;

/**
 * {@link StringFormat} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/9
 */
public class StringFormatTest extends TestCase {

	private final StringFormat stringFormat = new StringFormat();

	// ---------------------------------------------------------------- format

	public void testFormat() {
		assertEquals("hello, world!", stringFormat.format("hello, {}!", "world"));
		assertEquals("a=1, b=2", stringFormat.format("a={}, b={}", 1, 2));
		assertEquals("", stringFormat.format(""));
	}

	public void testFormatWithoutPlaceholder() {
		assertEquals("hello, world!", stringFormat.format("hello, world!", "ignored"));
		assertEquals("hello, world!", stringFormat.format("hello, world!"));
		assertEquals("hello, world!", stringFormat.format("hello, world!", (Object[]) null));
	}

	public void testFormatMorePlaceholdersThanArgs() {
		assertEquals("a=1, b={}", stringFormat.format("a={}, b={}", 1));
		assertEquals("{}", stringFormat.format("{}"));
	}

	public void testFormatMoreArgsThanPlaceholders() {
		assertEquals("a=1", stringFormat.format("a={}", 1, 2, 3));
	}

	public void testFormatNonEmptyBraceIsLiteral() {
		assertEquals("a={0}, b=x", stringFormat.format("a={0}, b={}", "x"));
		assertEquals("a={ }, b=x", stringFormat.format("a={ }, b={}", "x"));
	}

	public void testFormatEscape() {
		assertEquals("hello, {}!", stringFormat.format("hello, \\{}!", "world"));
		assertEquals("{}{}", stringFormat.format("\\{}\\{}", "a", "b"));
	}

	public void testFormatEscapedBackslash() {
		// \\ 输出一个 \，后面的 {} 仍然参与替换
		assertEquals("a\\1b", stringFormat.format("a\\\\{}b", 1));
	}

	public void testFormatWindowsPathKeepsBackslash() {
		assertEquals("C:\\Users\\admin", stringFormat.format("C:\\Users\\admin"));
		assertEquals("a\\b1", stringFormat.format("a\\b{}", 1));
		// 路径后紧跟占位符时 \{ 会被当成转义，需要写成 \\{ 才能替换
		assertEquals("C:\\Users\\zhang", stringFormat.formatByNamed("C:\\Users\\\\{name}", "name", "zhang"));
	}

	public void testFormatUnclosedBrace() {
		assertEquals("a { b", stringFormat.format("a { b", "x"));
		assertEquals("a {}b", stringFormat.format("a {}b"));
	}

	public void testFormatNullArgument() {
		assertEquals("hello, null!", stringFormat.format("hello, {}!", (Object) null));
		assertEquals("hello, !", new StringFormat("").format("hello, {}!", (Object) null));
		assertEquals("hello, N/A!", new StringFormat("N/A").format("hello, {}!", (Object) null));
		assertEquals("hello, !", stringFormat.setNullValue(null).format("hello, {}!", (Object) null));
	}

	public void testFormatNullFormat() {
		assertNull(stringFormat.format(null, "world"));
	}

	// ---------------------------------------------------------- formatByIndex

	public void testFormatByIndex() {
		assertEquals("hello, world!", stringFormat.formatByIndex("hello, {0}!", "world"));
		assertEquals("b=2, a=1", stringFormat.formatByIndex("b={1}, a={0}", 1, 2));
		assertEquals("x-x-x", stringFormat.formatByIndex("{0}-{0}-{0}", "x"));
	}

	public void testFormatByIndexOutOfRange() {
		assertEquals("a=1, b={1}", stringFormat.formatByIndex("a={0}, b={1}", 1));
		assertEquals("a=1, b={-1}", stringFormat.formatByIndex("a={0}, b={-1}", 1));
	}

	public void testFormatByIndexNonPlaceholder() {
		assertEquals("a={}, b={name}, c={01}", stringFormat.formatByIndex("a={}, b={name}, c={01}", "x"));
	}

	public void testFormatByIndexEscape() {
		assertEquals("hello, {0}!", stringFormat.formatByIndex("hello, \\{0}!", "world"));
	}

	// ---------------------------------------------------------- formatByNamed

	public void testFormatByNamed() {
		assertEquals("hello, world!", stringFormat.formatByNamed("hello, {name}!", "name", "world"));
		assertEquals("zhang 18", stringFormat.formatByNamed("{name} {age}", "name", "zhang", "age", 18));
		assertEquals("zhang-zhang", stringFormat.formatByNamed("{name}-{name}", "name", "zhang"));
	}

	public void testFormatByNamedMissingKey() {
		assertEquals("hello, {name}!", stringFormat.formatByNamed("hello, {name}!"));
		assertEquals("hello, {name}! zhang", stringFormat.formatByNamed("hello, {name}! {other}", "other", "zhang"));
	}

	public void testFormatByNamedNonNamePlaceholder() {
		assertEquals("a={}, b={na me}", stringFormat.formatByNamed("a={}, b={na me}", "na", "x"));
		assertEquals("x", stringFormat.formatByNamed("{0}", "0", "x"));
	}

	public void testFormatByNamedEscape() {
		assertEquals("hello, {name}!", stringFormat.formatByNamed("hello, \\{name}!", "name", "world"));
	}

	public void testFormatByNamedNullValue() {
		assertEquals("hello, null!", stringFormat.formatByNamed("hello, {name}!", "name", null));
		assertEquals("hello, !", new StringFormat("").formatByNamed("hello, {name}!", "name", null));
	}

	public void testFormatByNamedOddArgs() {
		try {
			stringFormat.formatByNamed("hello, {name}!", "name");
			fail("args 个数为奇数时应抛出 IllegalArgumentException");
		} catch (IllegalArgumentException expected) {
			// expected
		}
	}
}
