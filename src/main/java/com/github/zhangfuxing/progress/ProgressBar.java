package com.github.zhangfuxing.progress;

import java.io.PrintStream;

/**
 * 控制台文本进度条
 * <p>
 * 默认使用 {@link ProgressBarStyle#BLOCK} 样式、宽度 30、显示百分比：
 * <pre>{@code
 * ProgressBar bar = ProgressBar.of(100);
 * bar.onChange(event -> log.info("{}", event.getText()));
 * for (int i = 0; i < 100; i++) {
 *     // 处理任务
 *     bar.advance().print();
 * }
 * bar.finish();
 * }</pre>
 * {@link #print()} 使用 {@code \r} 覆盖当前行，不换行；{@link #println()} 输出完整一行并以换行结束。
 * <p>
 * 样式可通过 {@link #setStyle(ProgressBarStyle)} 切换，或直接调用
 * {@link #setCompleteChar(String)}、{@link #setIncompleteChar(String)}、{@link #setHeadChar(String)}、
 * {@link #setLeftBracket(String)}、{@link #setRightBracket(String)}、{@link #setWidth(int)} 组合自定义。
 * <p>
 * 更新与渲染方法均为同步方法，同一个实例可在多线程中共享；但输出顺序取决于调用顺序。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public class ProgressBar {

	/**
	 * 默认条形宽度
	 */
	public static final int DEFAULT_WIDTH = 30;

	/**
	 * 进度总量，固定不变
	 */
	private final long total;

	/**
	 * 当前进度值
	 */
	private long current;

	/**
	 * 条形宽度（字符数，不含边框、百分比等附加内容）
	 */
	private int width = DEFAULT_WIDTH;

	private String completeChar;

	private String incompleteChar;

	private String headChar;

	private String leftBracket;

	private String rightBracket;

	/**
	 * 是否显示百分比
	 */
	private boolean showPercent = true;

	/**
	 * 是否显示当前值/总量，如 "(5/10)"
	 */
	private boolean showCount;

	/**
	 * 附加在末尾的自定义文本
	 */
	private String suffix = "";

	/**
	 * 进度变化钩子
	 */
	private ProgressListener listener;

	/**
	 * 输出目标，默认 {@link System#out}
	 */
	private PrintStream out = System.out;

	/**
	 * @param total 进度总量，不能为负数
	 */
	public ProgressBar(long total) {
		this(total, ProgressBarStyle.BLOCK);
	}

	/**
	 * @param total 进度总量，不能为负数
	 * @param style 内置样式
	 */
	public ProgressBar(long total, ProgressBarStyle style) {
		this(total, style, System.out);
	}

	/**
	 * @param total 进度总量，不能为负数
	 * @param style 内置样式
	 * @param out   输出目标
	 */
	public ProgressBar(long total, ProgressBarStyle style, PrintStream out) {
		if (total < 0) {
			throw new IllegalArgumentException("total must be >= 0, but was " + total);
		}
		if (style == null) {
			throw new IllegalArgumentException("style must not be null");
		}
		if (out == null) {
			throw new IllegalArgumentException("out must not be null");
		}
		this.total = total;
		this.out = out;
		applyStyle(style);
	}

	/**
	 * 创建进度条，默认使用 {@link ProgressBarStyle#BLOCK} 样式
	 *
	 * @param total 进度总量
	 *
	 * @return 进度条
	 */
	public static ProgressBar of(long total) {
		return new ProgressBar(total);
	}

	/**
	 * 创建指定样式的进度条
	 *
	 * @param total 进度总量
	 * @param style 内置样式
	 *
	 * @return 进度条
	 */
	public static ProgressBar of(long total, ProgressBarStyle style) {
		return new ProgressBar(total, style);
	}

	/**
	 * 创建指定样式和输出目标的进度条
	 *
	 * @param total 进度总量
	 * @param style 内置样式
	 * @param out   输出目标
	 *
	 * @return 进度条
	 */
	public static ProgressBar of(long total, ProgressBarStyle style, PrintStream out) {
		return new ProgressBar(total, style, out);
	}

	// -------------------------------------------------------------------- 进度

	/**
	 * 进度加 1
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar advance() {
		return advance(1);
	}

	/**
	 * 进度增加指定值，超过总量时按总量处理
	 *
	 * @param delta 增加量，不能为负数
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar advance(long delta) {
		if (delta < 0) {
			throw new IllegalArgumentException("delta must be >= 0, but was " + delta);
		}
		long target = delta > total - current ? total : current + delta;
		return setCurrent(target);
	}

	/**
	 * 设置当前进度，小于 0 抛异常，超过总量时按总量处理
	 *
	 * @param current 当前进度值
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setCurrent(long current) {
		if (current < 0) {
			throw new IllegalArgumentException("current must be >= 0, but was " + current);
		}
		long target = Math.min(current, total);
		if (target == this.current) {
			return this;
		}
		this.current = target;
		notifyListener();
		return this;
	}

	/**
	 * 重置进度到 0
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar reset() {
		if (current == 0) {
			return this;
		}
		current = 0;
		notifyListener();
		return this;
	}

	/**
	 * 标记完成：进度置为总量，渲染完成后换行
	 */
	public synchronized void finish() {
		setCurrent(total);
		println();
	}

	/**
	 * @return 进度总量
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return 当前进度值
	 */
	public synchronized long getCurrent() {
		return current;
	}

	/**
	 * @return 剩余进度值，即 total - current
	 */
	public synchronized long getRemaining() {
		return total - current;
	}

	/**
	 * @return 百分比，范围 0 ~ 100；总量为 0 时视为已完成
	 */
	public synchronized int getPercent() {
		return percentValue();
	}

	/**
	 * @return 进度比例，范围 0.0 ~ 1.0
	 */
	public synchronized double getRatio() {
		if (total <= 0) {
			return 1.0;
		}
		return Math.min(1.0, (double) current / total);
	}

	/**
	 * @return 是否已完成，即 current 达到 total
	 */
	public synchronized boolean isComplete() {
		return current >= total;
	}

	// -------------------------------------------------------------------- 样式

	/**
	 * 切换内置样式，会覆盖当前的字符与边框配置
	 *
	 * @param style 内置样式
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setStyle(ProgressBarStyle style) {
		if (style == null) {
			throw new IllegalArgumentException("style must not be null");
		}
		applyStyle(style);
		return this;
	}

	/**
	 * @param width 条形宽度（字符数），必须大于 0
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setWidth(int width) {
		if (width < 1) {
			throw new IllegalArgumentException("width must be >= 1, but was " + width);
		}
		this.width = width;
		return this;
	}

	/**
	 * @return 条形宽度
	 */
	public synchronized int getWidth() {
		return width;
	}

	/**
	 * @param completeChar 已完成部分使用的字符，null 视为空字符串
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setCompleteChar(String completeChar) {
		this.completeChar = nullToEmpty(completeChar);
		return this;
	}

	/**
	 * @param incompleteChar 未完成部分使用的字符，null 视为空字符串
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setIncompleteChar(String incompleteChar) {
		this.incompleteChar = nullToEmpty(incompleteChar);
		return this;
	}

	/**
	 * @param headChar 已完成部分末端使用的字符，null 或空字符串表示不使用头部字符
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setHeadChar(String headChar) {
		this.headChar = nullToEmpty(headChar);
		return this;
	}

	/**
	 * @param leftBracket 左侧边框，null 视为空字符串
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setLeftBracket(String leftBracket) {
		this.leftBracket = nullToEmpty(leftBracket);
		return this;
	}

	/**
	 * @param rightBracket 右侧边框，null 视为空字符串
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setRightBracket(String rightBracket) {
		this.rightBracket = nullToEmpty(rightBracket);
		return this;
	}

	/**
	 * @param showPercent 是否显示百分比
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setShowPercent(boolean showPercent) {
		this.showPercent = showPercent;
		return this;
	}

	/**
	 * @param showCount 是否显示当前值/总量，如 "(5/10)"
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setShowCount(boolean showCount) {
		this.showCount = showCount;
		return this;
	}

	/**
	 * @param suffix 附加在末尾的自定义文本，null 视为空字符串
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setSuffix(String suffix) {
		this.suffix = nullToEmpty(suffix);
		return this;
	}

	/**
	 * @param out 输出目标，不能为 null
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar setOut(PrintStream out) {
		if (out == null) {
			throw new IllegalArgumentException("out must not be null");
		}
		this.out = out;
		return this;
	}

	// -------------------------------------------------------------------- 钩子

	/**
	 * 注册进度变化钩子，传入 null 表示取消
	 *
	 * @param listener 钩子
	 *
	 * @return 当前进度条，便于链式调用
	 */
	public synchronized ProgressBar onChange(ProgressListener listener) {
		this.listener = listener;
		return this;
	}

	// -------------------------------------------------------------------- 渲染

	/**
	 * 渲染当前进度条文本，不输出
	 *
	 * @return 进度条文本，如 "[█████████░░░░░░░░░░░░░░░░░░░░░] 33%"
	 */
	public synchronized String render() {
		return renderText();
	}

	/**
	 * 原地刷新：输出 {@code \r} + 进度条文本，不换行
	 */
	public synchronized void print() {
		write(false);
	}

	/**
	 * 输出完整进度条文本并换行
	 */
	public synchronized void println() {
		write(true);
	}

	@Override
	public String toString() {
		return render();
	}

	// -------------------------------------------------------------------- 内部

	/**
	 * 应用内置样式的字符与边框；仅百分比的样式会强制打开百分比显示
	 */
	private void applyStyle(ProgressBarStyle style) {
		this.completeChar = style.getCompleteChar();
		this.incompleteChar = style.getIncompleteChar();
		this.headChar = style.getHeadChar();
		this.leftBracket = style.getLeftBracket();
		this.rightBracket = style.getRightBracket();
		if (style.isPercentOnly()) {
			this.showPercent = true;
		}
	}

	/**
	 * 进度变化时创建快照并回调
	 */
	private void notifyListener() {
		if (listener == null) {
			return;
		}
		listener.onProgress(new ProgressEvent(current, total, percentValue(), renderText(), current >= total));
	}

	/**
	 * 渲染实现，调用方需持有锁
	 */
	private String renderText() {
		int percent = percentValue();
		StringBuilder sb = new StringBuilder(16);
		appendPart(sb, barText(percent));
		if (showPercent) {
			appendPart(sb, percent + "%");
		}
		if (showCount) {
			appendPart(sb, "(" + current + "/" + total + ")");
		}
		appendPart(sb, suffix);
		return sb.toString();
	}

	/**
	 * 百分比计算，调用方需持有锁
	 */
	private int percentValue() {
		if (total <= 0 || current >= total) {
			return 100;
		}
		// current < total 时最多显示 99%，避免大数值下 double 精度导致提前显示 100%
		return Math.min((int) (current * 100.0 / total), 99);
	}

	/**
	 * 拼装条形部分；已完成部分末端有头部字符时，头部字符占用一格
	 */
	private String barText(int percent) {
		if (completeChar.isEmpty() && incompleteChar.isEmpty()) {
			return "";
		}
		int cells = (int) ((long) width * percent / 100);
		boolean hasHead = !headChar.isEmpty() && cells > 0 && cells < width;
		int completeCells = hasHead ? cells - 1 : cells;
		StringBuilder sb = new StringBuilder(leftBracket.length() + width + rightBracket.length());
		sb.append(leftBracket);
		repeat(sb, completeChar, completeCells);
		if (hasHead) {
			sb.append(headChar);
		}
		repeat(sb, incompleteChar, width - cells);
		return sb.append(rightBracket).toString();
	}

	/**
	 * 输出到目标流；print 不换行，println 换行
	 */
	private void write(boolean newline) {
		out.print('\r');
		out.print(renderText());
		if (newline) {
			out.println();
		}
		out.flush();
	}

	private static void repeat(StringBuilder sb, String value, int count) {
		for (int i = 0; i < count; i++) {
			sb.append(value);
		}
	}

	private static void appendPart(StringBuilder sb, String part) {
		if (part == null || part.isEmpty()) {
			return;
		}
		if (sb.length() > 0) {
			sb.append(' ');
		}
		sb.append(part);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
