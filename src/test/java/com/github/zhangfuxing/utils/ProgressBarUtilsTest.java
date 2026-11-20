package com.github.zhangfuxing.utils;

import com.github.zhangfuxing.progress.ProgressBar;
import com.github.zhangfuxing.progress.ProgressBarStyle;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ProgressBarUtils} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/20
 */
public class ProgressBarUtilsTest {

	@Test
	public void testTrack() {
		ProgressBar bar = ProgressBarUtils.track(10, ProgressBarStyle.DOT);
		assertEquals(10, bar.getTotal());
		assertEquals(0, bar.getCurrent());
		bar.advance(5);
		assertEquals(50, bar.getPercent());
		assertEquals(10, ProgressBarUtils.track(10).getTotal());
	}

	@Test
	public void testRender() {
		assertEquals(ProgressBar.of(10).setCurrent(5).render(), ProgressBarUtils.render(5, 10));
		assertEquals("[███████████████░░░░░░░░░░░░░░░] 50%", ProgressBarUtils.render(5, 10));
		assertEquals("[###############---------------] 50%", ProgressBarUtils.render(5, 10, ProgressBarStyle.ASCII));
		assertEquals("[==============>---------------] 50%", ProgressBarUtils.render(5, 10, ProgressBarStyle.ARROW));
		assertEquals("50%", ProgressBarUtils.render(5, 10, ProgressBarStyle.PERCENT));
	}

	@Test
	public void testRenderClamps() {
		assertEquals(100, ProgressBar.of(10).setCurrent(20).getPercent());
		assertEquals("[##########] 100%", ProgressBar.of(10, ProgressBarStyle.ASCII).setWidth(10)
				.setCurrent(20).render());
	}

	@Test
	public void testPrintln() {
		PrintStream original = System.out;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(buffer));
		try {
			ProgressBarUtils.println(5, 10, ProgressBarStyle.ASCII);
		} finally {
			System.setOut(original);
		}
		assertEquals("\r[###############---------------] 50%" + System.lineSeparator(), buffer.toString());
	}

	@Test
	public void testPrintInPlace() {
		PrintStream original = System.out;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(buffer));
		try {
			ProgressBarUtils.print(5, 10, ProgressBarStyle.PERCENT);
		} finally {
			System.setOut(original);
		}
		assertEquals("\r50%", buffer.toString());
	}
}
