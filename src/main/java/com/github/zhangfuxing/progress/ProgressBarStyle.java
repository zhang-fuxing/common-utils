package com.github.zhangfuxing.progress;

/**
 * 内置的进度条样式
 * <p>
 * 每种样式由「已完成字符」「未完成字符」「头部字符（可选）」和「左右边框」组成；
 * 已完成字符与未完成字符都为空时，只输出百分比，见 {@link #PERCENT}。
 * <p>
 * 样式只提供默认外观，创建 {@link ProgressBar} 后仍可通过 setter 覆盖任意一项，组合出自定义样式。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public enum ProgressBarStyle {

	/**
	 * 实心方块：█ 表示已完成，░ 表示未完成
	 */
	BLOCK("█", "░", "", "[", "]"),

	/**
	 * 阴影方块：▓ 表示已完成，▒ 表示未完成
	 */
	SHADE("▓", "▒", "", "[", "]"),

	/**
	 * ASCII 字符：仅使用 # 与 -，兼容不支持 Unicode 的终端
	 */
	ASCII("#", "-", "", "[", "]"),

	/**
	 * 箭头：已完成部分用 = 填充，末端用 &gt; 作为箭头
	 */
	ARROW("=", "-", ">", "[", "]"),

	/**
	 * 方块：■ 表示已完成，□ 表示未完成
	 */
	SQUARE("■", "□", "", "[", "]"),

	/**
	 * 圆点：● 表示已完成，○ 表示未完成
	 */
	DOT("●", "○", "", "[", "]"),

	/**
	 * 星星：★ 表示已完成，☆ 表示未完成
	 */
	STAR("★", "☆", "", "[", "]"),

	/**
	 * 线条：━ 表示已完成，─ 表示未完成，不带边框
	 */
	LINE("━", "─", "", "", ""),

	/**
	 * 仅百分比：不绘制条形，只输出 "50%" 这类文本
	 */
	PERCENT("", "", "", "", "");

	/**
	 * 已完成部分使用的字符
	 */
	private final String completeChar;

	/**
	 * 未完成部分使用的字符
	 */
	private final String incompleteChar;

	/**
	 * 已完成部分末端使用的字符，为空表示不使用头部字符
	 */
	private final String headChar;

	/**
	 * 左侧边框
	 */
	private final String leftBracket;

	/**
	 * 右侧边框
	 */
	private final String rightBracket;

	ProgressBarStyle(String completeChar, String incompleteChar, String headChar, String leftBracket, String rightBracket) {
		this.completeChar = completeChar;
		this.incompleteChar = incompleteChar;
		this.headChar = headChar;
		this.leftBracket = leftBracket;
		this.rightBracket = rightBracket;
	}

	/**
	 * @return 已完成部分使用的字符
	 */
	public String getCompleteChar() {
		return completeChar;
	}

	/**
	 * @return 未完成部分使用的字符
	 */
	public String getIncompleteChar() {
		return incompleteChar;
	}

	/**
	 * @return 已完成部分末端使用的字符，未设置时为空字符串
	 */
	public String getHeadChar() {
		return headChar;
	}

	/**
	 * @return 左侧边框
	 */
	public String getLeftBracket() {
		return leftBracket;
	}

	/**
	 * @return 右侧边框
	 */
	public String getRightBracket() {
		return rightBracket;
	}

	/**
	 * 是否只输出百分比、不绘制条形
	 *
	 * @return 已完成字符与未完成字符都为空时返回 true
	 */
	public boolean isPercentOnly() {
		return completeChar.isEmpty() && incompleteChar.isEmpty();
	}
}
