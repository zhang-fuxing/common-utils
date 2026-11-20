package com.github.zhangfuxing.utils;

import com.github.zhangfuxing.progress.ProgressBar;
import com.github.zhangfuxing.progress.ProgressBarStyle;

/**
 * {@link ProgressBar} 的静态快捷入口，适合日志、一次性输出或简单循环
 * <p>
 * 例：
 * <pre>{@code
 * ProgressBarUtils.println(5, 10);                                    // [███████████████░░░░░░░░░░░░░░░] 50%
 * ProgressBarUtils.println(5, 10, ProgressBarStyle.ARROW);            // [==============>---------------] 50%
 * ProgressBarUtils.render(5, 10, ProgressBarStyle.ASCII);             // [###############---------------] 50%
 * }</pre>
 * 需要原地刷新、自定义样式或注册钩子时，使用 {@link #track(long)} 获得 {@link ProgressBar} 实例。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public class ProgressBarUtils {

	private ProgressBarUtils() {
	}

	/**
	 * 创建默认样式（{@link ProgressBarStyle#BLOCK}）的进度条
	 *
	 * @param total 进度总量
	 *
	 * @return 进度条
	 */
	public static ProgressBar track(long total) {
		return ProgressBar.of(total);
	}

	/**
	 * 创建指定样式的进度条
	 *
	 * @param total 进度总量
	 * @param style 内置样式
	 *
	 * @return 进度条
	 */
	public static ProgressBar track(long total, ProgressBarStyle style) {
		return ProgressBar.of(total, style);
	}

	/**
	 * 渲染进度条文本，默认样式
	 *
	 * @param current 当前进度
	 * @param total   进度总量
	 *
	 * @return 进度条文本
	 */
	public static String render(long current, long total) {
		return render(current, total, ProgressBarStyle.BLOCK);
	}

	/**
	 * 渲染指定样式的进度条文本
	 *
	 * @param current 当前进度
	 * @param total   进度总量
	 * @param style   内置样式
	 *
	 * @return 进度条文本
	 */
	public static String render(long current, long total, ProgressBarStyle style) {
		return ProgressBar.of(total, style).setCurrent(current).render();
	}

	/**
	 * 原地输出进度条，不换行，默认样式
	 *
	 * @param current 当前进度
	 * @param total   进度总量
	 */
	public static void print(long current, long total) {
		print(current, total, ProgressBarStyle.BLOCK);
	}

	/**
	 * 原地输出指定样式的进度条，不换行
	 *
	 * @param current 当前进度
	 * @param total   进度总量
	 * @param style   内置样式
	 */
	public static void print(long current, long total, ProgressBarStyle style) {
		ProgressBar.of(total, style).setCurrent(current).print();
	}

	/**
	 * 输出进度条并换行，默认样式
	 *
	 * @param current 当前进度
	 * @param total   进度总量
	 */
	public static void println(long current, long total) {
		println(current, total, ProgressBarStyle.BLOCK);
	}

	/**
	 * 输出指定样式的进度条并换行
	 *
	 * @param current 当前进度
	 * @param total   进度总量
	 * @param style   内置样式
	 */
	public static void println(long current, long total, ProgressBarStyle style) {
		ProgressBar.of(total, style).setCurrent(current).println();
	}
}
