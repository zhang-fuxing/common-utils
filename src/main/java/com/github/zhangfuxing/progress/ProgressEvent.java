package com.github.zhangfuxing.progress;

/**
 * 进度快照，由 {@link ProgressBar} 在进度变化时创建，并交给 {@link ProgressListener}
 * <p>
 * 实例不可变，可安全地传递到其他线程或缓存。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public final class ProgressEvent {

	private final long current;

	private final long total;

	private final int percent;

	private final String text;

	private final boolean complete;

	ProgressEvent(long current, long total, int percent, String text, boolean complete) {
		this.current = current;
		this.total = total;
		this.percent = percent;
		this.text = text;
		this.complete = complete;
	}

	/**
	 * @return 当前进度值
	 */
	public long getCurrent() {
		return current;
	}

	/**
	 * @return 进度总量
	 */
	public long getTotal() {
		return total;
	}

	/**
	 * @return 剩余进度值，即 total - current
	 */
	public long getRemaining() {
		return total - current;
	}

	/**
	 * @return 百分比，范围 0 ~ 100
	 */
	public int getPercent() {
		return percent;
	}

	/**
	 * @return 触发本次回调时的进度条文本
	 */
	public String getText() {
		return text;
	}

	/**
	 * 是否已完成，即 current 达到 total
	 *
	 * @return 是否已完成
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @return 进度条文本，便于直接打印日志
	 */
	@Override
	public String toString() {
		return text;
	}
}
