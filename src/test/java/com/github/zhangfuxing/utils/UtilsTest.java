package com.github.zhangfuxing.utils;

import junit.framework.TestCase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link Utils} 日期相关方法测试
 *
 * @author 张福兴
 * @date 2026/11/9
 */
public class UtilsTest extends TestCase {

	private static final LocalDateTime SAMPLE = LocalDateTime.of(2026, 11, 9, 14, 5, 3, 7_000_000);
	private static final String SAMPLE_TEXT = "2026-11-09 14:05:03.007";

	public void testFormatDatetimeLocalDateTime() {
		assertEquals(SAMPLE_TEXT, Utils.formatDatetime(SAMPLE));
	}

	public void testFormatDatetimeDate() {
		Date date = new Date(Utils.toMillisecond(SAMPLE));
		assertEquals(SAMPLE_TEXT, Utils.formatDatetime(date));
	}

	public void testFormatDatetimeLocalDate() {
		assertEquals("2026-11-09 00:00:00.000", Utils.formatDatetime(LocalDate.of(2026, 11, 9)));
	}

	public void testFormatDatetimeInstant() {
		// 2026-11-09T06:05:03.007Z 是东八区的 14:05:03.007
		assertEquals(SAMPLE_TEXT, Utils.formatDatetime(Instant.parse("2026-11-09T06:05:03.007Z")));
	}

	public void testFormatDatetimeNull() {
		assertNull(Utils.formatDatetime((Date) null));
		assertNull(Utils.formatDatetime((TemporalAccessor) null));
	}

	public void testToMillisecondNull() {
		assertEquals(0L, Utils.toMillisecond((Date) null));
		assertEquals(0L, Utils.toMillisecond((TemporalAccessor) null));
	}

	public void testFormatDataSize() {
		assertEquals("0 B", Utils.formatDataSize(0));
		assertEquals("1.5 KB", Utils.formatDataSize(1536));
		assertEquals("2 KB", Utils.formatDataSize(1536, 0));
	}

	public void testParseDataSize() {
		assertEquals(314572800L, Utils.parseDataSize("300MB"));
		assertEquals(1610612736L, Utils.parseDataSize("1.5 GB"));
		assertEquals(1024L, Utils.parseDataSize("1024"));
	}

	public void testConvertDataSize() {
		assertEquals("0.29 GB", Utils.convertDataSize("300MB", "GB"));
		assertEquals("1 KB", Utils.convertDataSize("1024", "kb"));
		assertEquals("314572800 B", Utils.convertDataSize("300MB", "字节"));
	}

	/**
	 * 并发格式化：SimpleDateFormat 在此场景下会错乱，DateTimeFormatter 应稳定输出
	 */
	public void testFormatDatetimeConcurrently() throws Exception {
		int threads = 8;
		int times = 100;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			List<Future<String>> futures = new ArrayList<>();
			for (int i = 0; i < threads * times; i++) {
				futures.add(pool.submit(() -> Utils.formatDatetime(SAMPLE)));
			}
			for (Future<String> future : futures) {
				assertEquals(SAMPLE_TEXT, future.get(30, TimeUnit.SECONDS));
			}
		} finally {
			pool.shutdownNow();
		}
	}
}
