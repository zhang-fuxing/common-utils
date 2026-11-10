package com.github.zhangfuxing.utils;

import junit.framework.TestCase;

/**
 * {@link FormatUtils} 门面方法测试
 *
 * @author 张福兴
 * @date 2026/11/10
 */
public class FormatUtilsTest extends TestCase {

	public void testFormatString() {
		assertEquals("hello, world!", FormatUtils.formatString("hello, {}!", "world"));
		assertEquals("hello, world!", FormatUtils.formatStringByIndex("hello, {0}!", "world"));
		assertEquals("hello, world!", FormatUtils.formatStringByNamed("hello, {name}!", "name", "world"));
	}

	public void testFormatDataSize() {
		assertEquals("0 B", FormatUtils.formatDataSize(0));
		assertEquals("1.5 KB", FormatUtils.formatDataSize(1536));
		assertEquals("2 KB", FormatUtils.formatDataSize(1536, 0));
		assertEquals("1.18 MB", FormatUtils.formatDataSize(1234567));
	}

	public void testParseDataSize() {
		assertEquals(314572800L, FormatUtils.parseDataSize("300MB"));
		assertEquals(1610612736L, FormatUtils.parseDataSize("1.5 GB"));
		assertEquals(1024L, FormatUtils.parseDataSize("1024"));
	}

	public void testConvertDataSize() {
		assertEquals("0.29 GB", FormatUtils.convertDataSize("300MB", "GB"));
		assertEquals("1 KB", FormatUtils.convertDataSize("1024", "kb"));
		assertEquals("314572800 B", FormatUtils.convertDataSize("300MB", "字节"));
	}
}
