package com.github.zhangfuxing.string;

import com.github.zhangfuxing.utils.MapUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * 字符串格式化工具，支持三种占位符风格：
 * <ul>
 *     <li>{@link #format(String, Object...)}：顺序占位符 {@code {}}</li>
 *     <li>{@link #formatByIndex(String, Object...)}：索引占位符 {@code {0}}、{@code {1}}</li>
 *     <li>{@link #formatByNamed(String, Object...)}：命名占位符 {@code {name}}</li>
 * </ul>
 *
 * <p>
 * 通用规则：
 * <ul>
 *     <li>使用 {@code \} 转义占位符，{@code \{}} 输出字面量 {@code \{}，{@code \\} 输出单个 {@code \}；
 *     其它位置的 {@code \} 原样保留（方便输出 Windows 路径）。</li>
 *     <li>占位符数量多于参数、或索引越界时，占位符原样保留，不抛异常。</li>
 *     <li>参数值为 {@code null} 时的输出内容由 {@link #setNullValue(String)} 控制，默认为 {@code "null"}。</li>
 * </ul>
 *
 * <p>
 * 注意：{@link #nullValue} 为实例状态，非线程安全，请在多线程共享前完成配置
 * （或直接使用默认配置，默认配置下实例是只读的）。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/9
 * @email zhangfuxing1010@163.com
 */
public class StringFormat {

	/**
	 * 参数为 null 时的默认输出值
	 */
	public static final String DEFAULT_NULL_VALUE = "null";

	/**
	 * 占位符起始标记
	 */
	private static final char PLACEHOLDER_START = '{';

	/**
	 * 占位符结束标记
	 */
	private static final char PLACEHOLDER_END = '}';

	/**
	 * 转义字符
	 */
	private static final char ESCAPE_CHAR = '\\';

	/**
	 * 索引占位符允许的最大位数，超过则视为普通文本，避免 parseInt 溢出
	 */
	private static final int MAX_INDEX_DIGITS = 9;

	private static final Object[] EMPTY_ARGS = new Object[0];

	/**
	 * 参数为 null 时的输出值，默认为 {@link #DEFAULT_NULL_VALUE}
	 */
	private String nullValue = DEFAULT_NULL_VALUE;

	public StringFormat() {
	}

	/**
	 * @param nullValue 参数为 null 时的输出值，传 null 表示输出空字符串
	 */
	public StringFormat(String nullValue) {
		this.nullValue = nullValue;
	}

	/**
	 * 格式化字符串，使用 {} 作为占位符，使用 args 格式化字符串
	 * <p>
	 * 例：format("hello, {}!", "world") 返回 "hello, world!"
	 * <p>
	 * 如果想保留{}, 使用 \ 进行转义
	 * <p>
	 * 例：format("hello, \\{}!", "world") 返回 "hello, {}!"
	 * <p>
	 * 占位符比参数多时，多出来的占位符原样保留；参数比占位符多时，多余的参数被忽略。
	 *
	 * @param format 格式化字符串
	 * @param args   参数
	 *
	 * @return 格式化后的字符串，format 为 null 时返回 null
	 */
	public String format(String format, Object... args) {
		Object[] params = args == null ? EMPTY_ARGS : args;
		// 顺序模式下占位符按出现顺序依次消费参数
		int[] cursor = {0};
		return formatInternal(format, key -> {
			if (!key.isEmpty() || cursor[0] >= params.length) {
				return null;
			}
			return stringify(params[cursor[0]++]);
		});
	}

	/**
	 * 格式化字符串，使用 {index} 作为占位符，使用 args 格式化字符串
	 * <p>
	 * 例：formatByIndex("hello, {0}!", "world") 返回 "hello, world!"
	 * <p>
	 * 索引从 0 开始，可以重复引用同一个参数，顺序也可以任意；
	 * 不符合 {@code {数字}} 形式的（如 {@code {}}、{@code {name}}、{@code {0 }}、{@code {01}}）视为普通文本。
	 * <p>
	 * 如果想保留{0}, 使用 \ 进行转义
	 * <p>
	 * 例：formatByIndex("hello, \\{0}!","world") 返回 "hello, {0}!"
	 *
	 * @param format 格式化字符串
	 * @param args   参数
	 *
	 * @return 格式化后的字符串，format 为 null 时返回 null
	 */
	public String formatByIndex(String format, Object... args) {
		Object[] params = args == null ? EMPTY_ARGS : args;
		return formatInternal(format, key -> {
			if (!isIndex(key)) {
				return null;
			}
			int index = Integer.parseInt(key);
			if (index >= params.length) {
				return null;
			}
			return stringify(params[index]);
		});
	}

	/**
	 * 格式化字符串，使用 {name} 作为占位符，使用 args 格式化字符串
	 * <p>
	 * 例：formatByNamed("hello, {name}!", "name", "world") 返回 "hello, world!"
	 * <p>
	 * args 必须按 key、value 成对传入（个数为奇数时抛 IllegalArgumentException），
	 * 允许只传参数却不使用、也允许多次引用同一个 key。
	 * <p>
	 * 如果想保留{name}, 使用 \ 进行转义
	 * <p>
	 * 例：formatByNamed("hello, \\{name}!","name", "world") 返回 "hello, {name}!"
	 *
	 * @param format 格式化字符串
	 * @param args   key、value 成对的参数
	 *
	 * @return 格式化后的字符串，format 为 null 时返回 null
	 *
	 * @throws IllegalArgumentException args 个数为奇数时
	 */
	public String formatByNamed(String format, Object... args) {
		Map<String, String> params = MapUtils.convert(MapUtils.mapOf(args), String::valueOf, this::stringify);
		return formatInternal(format, key -> isName(key) ? params.get(key) : null);
	}

	/**
	 * @return 参数为 null 时的输出值，可能为 null（表示输出空字符串）
	 */
	public String getNullValue() {
		return nullValue;
	}

	/**
	 * 设置参数为 null 时的输出值，可用于控制输出 "null" 还是空字符串
	 *
	 * @param nullValue 输出值，传 null 表示输出空字符串
	 *
	 * @return this，便于链式调用
	 */
	public StringFormat setNullValue(String nullValue) {
		this.nullValue = nullValue;
		return this;
	}

	/**
	 * 解析占位符并完成拼接
	 *
	 * @param format   格式化字符串
	 * @param resolver 占位符解析器，返回 null 表示不是占位符、原样保留
	 *
	 * @return 格式化后的字符串
	 */
	private String formatInternal(String format, Function<String, String> resolver) {
		if (format == null) {
			return null;
		}

		int length = format.length();
		StringBuilder sb = new StringBuilder(length + 16);
		int i = 0;
		while (i < length) {
			char c = format.charAt(i);
			if (c == ESCAPE_CHAR) {
				// 只有 \\ 和 \{ 是转义，其它位置的 \ 原样保留，避免破坏 Windows 路径
				if (i + 1 < length) {
					char next = format.charAt(i + 1);
					if (next == ESCAPE_CHAR || next == PLACEHOLDER_START) {
						sb.append(next);
						i += 2;
						continue;
					}
				}
				sb.append(c);
				i++;
				continue;
			}

			if (c == PLACEHOLDER_START) {
				int end = format.indexOf(PLACEHOLDER_END, i + 1);
				if (end > i) {
					String replacement = resolver.apply(format.substring(i + 1, end));
					if (replacement != null) {
						sb.append(replacement);
						i = end + 1;
						continue;
					}
				}
			}

			sb.append(c);
			i++;
		}
		return sb.toString();
	}

	/**
	 * 把参数值转成字符串，null 使用 nullValue 配置
	 */
	private String stringify(Object value) {
		if (value != null) {
			return String.valueOf(value);
		}
		return nullValue == null ? "" : nullValue;
	}

	/**
	 * 是否为合法的索引占位符 key：0-9 组成的数字，不允许前导零（{@code {01}} 视为普通文本）,
	 * 且位数不超过 {@link #MAX_INDEX_DIGITS}
	 */
	private static boolean isIndex(String s) {
		if (s.isEmpty() || s.length() > MAX_INDEX_DIGITS) {
			return false;
		}
		if (s.length() > 1 && s.charAt(0) == '0') {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否为合法的命名占位符 key：字母、数字、下划线、$、点、中划线
	 */
	private static boolean isName(String s) {
		if (s.isEmpty()) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			boolean valid = Character.isLetterOrDigit(c)
					|| c == '_' || c == '$' || c == '.' || c == '-';
			if (!valid) {
				return false;
			}
		}
		return true;
	}
}
