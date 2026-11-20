package com.github.zhangfuxing.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link StringFormat} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/9
 */
public class StringFormatTest {

	private final StringFormat stringFormat = new StringFormat();

	// ---------------------------------------------------------------- format

	@Test
	public void testFormat() {
		assertEquals("hello, world!", stringFormat.format("hello, {}!", "world"));
		assertEquals("a=1, b=2", stringFormat.format("a={}, b={}", 1, 2));
		assertEquals("", stringFormat.format(""));
	}

	@Test
	public void testFormatWithoutPlaceholder() {
		assertEquals("hello, world!", stringFormat.format("hello, world!", "ignored"));
		assertEquals("hello, world!", stringFormat.format("hello, world!"));
		assertEquals("hello, world!", stringFormat.format("hello, world!", (Object[]) null));
	}

	@Test
	public void testFormatMorePlaceholdersThanArgs() {
		assertEquals("a=1, b={}", stringFormat.format("a={}, b={}", 1));
		assertEquals("{}", stringFormat.format("{}"));
	}

	@Test
	public void testFormatMoreArgsThanPlaceholders() {
		assertEquals("a=1", stringFormat.format("a={}", 1, 2, 3));
	}

	@Test
	public void testFormatNonEmptyBraceIsLiteral() {
		assertEquals("a={0}, b=x", stringFormat.format("a={0}, b={}", "x"));
		assertEquals("a={ }, b=x", stringFormat.format("a={ }, b={}", "x"));
	}

	@Test
	public void testFormatEscape() {
		assertEquals("hello, {}!", stringFormat.format("hello, \\{}!", "world"));
		assertEquals("{}{}", stringFormat.format("\\{}\\{}", "a", "b"));
	}

	@Test
	public void testFormatEscapedBackslash() {
		// \\ 输出一个 \，后面的 {} 仍然参与替换
		assertEquals("a\\1b", stringFormat.format("a\\\\{}b", 1));
	}

	@Test
	public void testFormatWindowsPathKeepsBackslash() {
		assertEquals("C:\\Users\\admin", stringFormat.format("C:\\Users\\admin"));
		assertEquals("a\\b1", stringFormat.format("a\\b{}", 1));
		// 路径后紧跟占位符时 \{ 会被当成转义，需要写成 \\{ 才能替换
		assertEquals("C:\\Users\\zhang", stringFormat.formatByNamed("C:\\Users\\\\{name}", "name", "zhang"));
	}

	@Test
	public void testFormatUnclosedBrace() {
		assertEquals("a { b", stringFormat.format("a { b", "x"));
		assertEquals("a {}b", stringFormat.format("a {}b"));
	}

	@Test
	public void testFormatNullArgument() {
		assertEquals("hello, null!", stringFormat.format("hello, {}!", (Object) null));
		assertEquals("hello, !", new StringFormat("").format("hello, {}!", (Object) null));
		assertEquals("hello, N/A!", new StringFormat("N/A").format("hello, {}!", (Object) null));
		assertEquals("hello, !", stringFormat.setNullValue(null).format("hello, {}!", (Object) null));
	}

	@Test
	public void testFormatNullFormat() {
		assertNull(stringFormat.format(null, "world"));
	}

	// ---------------------------------------------------------- formatByIndex

	@Test
	public void testFormatByIndex() {
		assertEquals("hello, world!", stringFormat.formatByIndex("hello, {0}!", "world"));
		assertEquals("b=2, a=1", stringFormat.formatByIndex("b={1}, a={0}", 1, 2));
		assertEquals("x-x-x", stringFormat.formatByIndex("{0}-{0}-{0}", "x"));
	}

	@Test
	public void testFormatByIndexOutOfRange() {
		assertEquals("a=1, b={1}", stringFormat.formatByIndex("a={0}, b={1}", 1));
		assertEquals("a=1, b={-1}", stringFormat.formatByIndex("a={0}, b={-1}", 1));
	}

	@Test
	public void testFormatByIndexNonPlaceholder() {
		assertEquals("a={}, b={name}, c={01}", stringFormat.formatByIndex("a={}, b={name}, c={01}", "x"));
	}

	@Test
	public void testFormatByIndexEscape() {
		assertEquals("hello, {0}!", stringFormat.formatByIndex("hello, \\{0}!", "world"));
	}

	// ---------------------------------------------------------- formatByNamed

	@Test
	public void testFormatByNamed() {
		assertEquals("hello, world!", stringFormat.formatByNamed("hello, {name}!", "name", "world"));
		assertEquals("zhang 18", stringFormat.formatByNamed("{name} {age}", "name", "zhang", "age", 18));
		assertEquals("zhang-zhang", stringFormat.formatByNamed("{name}-{name}", "name", "zhang"));
	}

	@Test
	public void testFormatByNamedMissingKey() {
		assertEquals("hello, {name}!", stringFormat.formatByNamed("hello, {name}!"));
		assertEquals("hello, {name}! zhang", stringFormat.formatByNamed("hello, {name}! {other}", "other", "zhang"));
	}

	@Test
	public void testFormatByNamedNonNamePlaceholder() {
		assertEquals("a={}, b={na me}", stringFormat.formatByNamed("a={}, b={na me}", "na", "x"));
		assertEquals("x", stringFormat.formatByNamed("{0}", "0", "x"));
	}

	@Test
	public void testFormatByNamedEscape() {
		assertEquals("hello, {name}!", stringFormat.formatByNamed("hello, \\{name}!", "name", "world"));
	}

	@Test
	public void testFormatByNamedNullValue() {
		assertEquals("hello, null!", stringFormat.formatByNamed("hello, {name}!", "name", null));
		assertEquals("hello, !", new StringFormat("").formatByNamed("hello, {name}!", "name", null));
	}

	@Test
	public void testFormatByNamedOddArgs() {
		assertThrows(IllegalArgumentException.class,
				() -> stringFormat.formatByNamed("hello, {name}!", "name"));
	}
}
