package com.github.zhangfuxing.progress;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ProgressBar} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/20
 */
public class ProgressBarTest {

	@Test
	public void testDefaultRender() {
		ProgressBar bar = ProgressBar.of(10);
		assertEquals("[" + repeat("░", 30) + "] 0%", bar.render());
		bar.setCurrent(5);
		assertEquals("[" + repeat("█", 15) + repeat("░", 15) + "] 50%", bar.render());
		bar.setCurrent(10);
		assertEquals("[" + repeat("█", 30) + "] 100%", bar.render());
		assertTrue(bar.isComplete());
	}

	@Test
	public void testBuiltInStyles() {
		assertEquals("[#####-----] 50%", bar(ProgressBarStyle.ASCII).render());
		assertEquals("[====>-----] 50%", bar(ProgressBarStyle.ARROW).render());
		assertEquals("[■■■■■□□□□□] 50%", bar(ProgressBarStyle.SQUARE).render());
		assertEquals("[●●●●●○○○○○] 50%", bar(ProgressBarStyle.DOT).render());
		assertEquals("[★★★★★☆☆☆☆☆] 50%", bar(ProgressBarStyle.STAR).render());
		assertEquals("[▓▓▓▓▓▒▒▒▒▒] 50%", bar(ProgressBarStyle.SHADE).render());
		assertEquals("━━━━━───── 50%", bar(ProgressBarStyle.LINE).render());
		assertEquals("50%", bar(ProgressBarStyle.PERCENT).render());
	}

	@Test
	public void testArrowHeadAtBoundaries() {
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.ARROW).setWidth(10);
		assertEquals("[----------] 0%", bar.render());
		bar.setCurrent(10);
		assertEquals("[==========] 100%", bar.render());
	}

	@Test
	public void testSwitchStyle() {
		ProgressBar bar = ProgressBar.of(10).setWidth(10).setCurrent(5);
		bar.setStyle(ProgressBarStyle.ARROW);
		assertEquals("[====>-----] 50%", bar.render());
	}

	@Test
	public void testCustomStyle() {
		ProgressBar bar = ProgressBar.of(100).setWidth(10).setCurrent(50)
				.setCompleteChar("★")
				.setIncompleteChar("☆")
				.setHeadChar(">")
				.setLeftBracket("|")
				.setRightBracket("|")
				.setShowPercent(false);
		assertEquals("|★★★★>☆☆☆☆☆|", bar.render());
	}

	@Test
	public void testCountAndSuffix() {
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.ASCII).setWidth(10).setCurrent(5)
				.setShowCount(true)
				.setSuffix("done");
		assertEquals("[#####-----] 50% (5/10) done", bar.render());
	}

	@Test
	public void testNullCharsTreatedAsEmpty() {
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.ASCII).setWidth(10).setCurrent(5)
				.setHeadChar(null)
				.setLeftBracket(null)
				.setRightBracket(null)
				.setSuffix(null);
		assertEquals("#####----- 50%", bar.render());
	}

	@Test
	public void testZeroTotal() {
		ProgressBar bar = ProgressBar.of(0);
		assertEquals(100, bar.getPercent());
		assertTrue(bar.isComplete());
		assertEquals(0, bar.getRemaining());
		assertEquals(1.0, bar.getRatio(), 0.0);
	}

	@Test
	public void testPercentAndRatio() {
		ProgressBar bar = ProgressBar.of(3);
		bar.setCurrent(1);
		assertEquals(33, bar.getPercent());
		assertEquals(1.0 / 3, bar.getRatio(), 1e-9);
		bar.setCurrent(2);
		assertEquals(66, bar.getPercent());
		assertEquals(2.0 / 3, bar.getRatio(), 1e-9);
		bar.setCurrent(20);
		assertEquals(3, bar.getCurrent());
		assertEquals(100, bar.getPercent());
		assertEquals(1.0, bar.getRatio(), 0.0);
	}

	@Test
	public void testPercentNeverHundredBeforeComplete() {
		ProgressBar bar = ProgressBar.of(Long.MAX_VALUE);
		bar.setCurrent(Long.MAX_VALUE - 1);
		assertFalse(bar.isComplete());
		assertEquals(99, bar.getPercent());
	}

	@Test
	public void testAdvance() {
		ProgressBar bar = ProgressBar.of(10);
		assertEquals(1, bar.advance().getCurrent());
		assertEquals(5, bar.advance(4).getCurrent());
		assertEquals(10, bar.advance(100).getCurrent());
		assertEquals(0, bar.advance().getRemaining());
	}

	@Test
	public void testReset() {
		ProgressBar bar = ProgressBar.of(10).setCurrent(5);
		assertEquals(0, bar.reset().getCurrent());
		assertEquals(0, bar.getPercent());
	}

	@Test
	public void testListener() {
		List<ProgressEvent> events = new ArrayList<>();
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.ASCII).setWidth(10);
		bar.onChange(events::add);

		bar.advance();
		assertEquals(1, events.size());
		ProgressEvent event = events.get(0);
		assertEquals(1, event.getCurrent());
		assertEquals(10, event.getTotal());
		assertEquals(9, event.getRemaining());
		assertEquals(10, event.getPercent());
		assertFalse(event.isComplete());
		assertEquals("[#---------] 10%", event.getText());
		assertEquals(event.getText(), event.toString());

		bar.setCurrent(10);
		assertEquals(2, events.size());
		assertTrue(events.get(1).isComplete());
		assertEquals(100, events.get(1).getPercent());

		// 进度未变化时不重复回调
		bar.setCurrent(10);
		assertEquals(2, events.size());

		// 取消钩子后不再回调
		bar.onChange(null);
		bar.reset();
		assertEquals(0, bar.getCurrent());
		assertEquals(2, events.size());
	}

	@Test
	public void testListenerGetsRenderedText() {
		List<ProgressEvent> events = new ArrayList<>();
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.PERCENT);
		bar.onChange(events::add);
		bar.setCurrent(5);
		assertEquals("50%", events.get(0).getText());
	}

	@Test
	public void testPrintAndFinish() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.ASCII, new PrintStream(buffer)).setWidth(10);

		bar.setCurrent(5).print();
		assertEquals("\r[#####-----] 50%", buffer.toString());

		buffer.reset();
		bar.finish();
		assertTrue(buffer.toString().startsWith("\r[##########] 100%"));
		assertTrue(buffer.toString().endsWith(System.lineSeparator()));
		assertEquals(10, bar.getCurrent());
	}

	@Test
	public void testSetOut() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ProgressBar bar = ProgressBar.of(10, ProgressBarStyle.PERCENT);
		bar.setOut(new PrintStream(buffer));
		bar.setCurrent(5).print();
		assertEquals("\r50%", buffer.toString());
	}

	@Test
	public void testInvalidArguments() {
		assertIllegalArgument(() -> ProgressBar.of(-1));
		assertIllegalArgument(() -> ProgressBar.of(10, null));
		assertIllegalArgument(() -> ProgressBar.of(10).setWidth(0));
		assertIllegalArgument(() -> ProgressBar.of(10).setStyle(null));
		assertIllegalArgument(() -> ProgressBar.of(10).setOut(null));

		ProgressBar bar = ProgressBar.of(10);
		assertIllegalArgument(() -> bar.setCurrent(-1));
		assertIllegalArgument(() -> bar.advance(-1));
	}

	private ProgressBar bar(ProgressBarStyle style) {
		return ProgressBar.of(10, style).setWidth(10).setCurrent(5);
	}

	private void assertIllegalArgument(Runnable action) {
		assertThrows(IllegalArgumentException.class, action::run);
	}

	private static String repeat(String value, int count) {
		StringBuilder sb = new StringBuilder(value.length() * count);
		for (int i = 0; i < count; i++) {
			sb.append(value);
		}
		return sb.toString();
	}
}
