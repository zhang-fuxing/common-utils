package com.github.zhangfuxing.progress;

/**
 * 进度变化钩子
 * <p>
 * 每次进度值发生变化时回调，可在回调里记录日志、刷新界面或上报监控。
 * <p>
 * 例：
 * <pre>{@code
 * ProgressBar bar = ProgressBar.of(100);
 * bar.onChange(event -> System.out.printf("%d%% %s%n", event.getPercent(), event.getText()));
 * }</pre>
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
@FunctionalInterface
public interface ProgressListener {

	/**
	 * 进度变化时回调
	 *
	 * @param event 进度快照，包含当前值、总量、百分比与渲染后的文本
	 */
	void onProgress(ProgressEvent event);
}
