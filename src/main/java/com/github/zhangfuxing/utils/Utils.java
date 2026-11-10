package com.github.zhangfuxing.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/9
 * @email zhangfuxing1010@163.com
 */
public abstract class Utils {

	/**
	 * 默认时区
	 */
	public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

	/**
	 * 默认日期时间格式：yyyy-MM-dd HH:mm:ss.SSS
	 * <p>
	 * DateTimeFormatter 不可变，可安全地在多线程间共享（SimpleDateFormat 不是线程安全的）
	 */
	public static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


	/**
	 * 测试条件，满足条件则抛出 IllegalArgumentException 异常
	 *
	 * @param condition 测试条件
	 * @param message   异常信息 IllegalArgumentException Message
	 */
	public static void testThrow(boolean condition, String message) {
		testThrow(condition, () -> new IllegalArgumentException(message));
	}

	/**
	 * 测试条件，满足条件则抛出异常
	 *
	 * @param condition         测试条件
	 * @param exceptionSupplier 异常提供者
	 */
	public static void testThrow(boolean condition, Supplier<? extends RuntimeException> exceptionSupplier) {
		if (exceptionSupplier == null) {
			return;
		}
		if (condition) {
			throw exceptionSupplier.get();
		}

	}

	/**
	 * 测试条件，满足条件则执行
	 *
	 * @param condition 测试条件
	 * @param runnable  执行内容
	 */
	public static void testThen(boolean condition, Runnable runnable) {
		if (runnable == null) {
			return;
		}
		if (condition) {
			runnable.run();
		}
	}

	/**
	 * 判断Map是否为空
	 *
	 * @param map map
	 *
	 * @return 是否为空
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * 判断数组是否为空
	 *
	 * @param array 数组
	 *
	 * @return 是否为空
	 */
	public static <T> boolean isEmpty(T[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * 判断可迭代数据集是否为空
	 *
	 * @param dataset 可迭代数据集合
	 *
	 * @return 是否为空
	 */
	public static <T> boolean isEmpty(Iterable<T> dataset) {
		return dataset == null || !dataset.iterator().hasNext();
	}

	/**
	 * 去除字符串两端的空格
	 *
	 * @param str 字符串
	 *
	 * @return 去除两端空格后的字符串
	 */
	public static String trim(String str) {
		return str == null ? null : str.trim();
	}

	/**
	 * 判断字符串是否为空
	 *
	 * @param str 字符串
	 *
	 * @return 是否为空
	 */
	public static boolean isBlank(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static boolean isNotBlank(String string) {
		return !isBlank(string);
	}

	/**
	 * 分割字符串
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @param limit     限制
	 *
	 * @return 分割后的字符串数组
	 */
	public static String[] split(String str, String separator, int limit) {
		return str == null ? null : str.split(separator, limit);
	}

	/**
	 * 分割字符串
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 *
	 * @return 分割后的字符串数组
	 */
	public static String[] split(String str, String separator) {
		return split(str, separator, 0);
	}

	/**
	 * 将可迭代数据集转换为HashMap 若值重复，新值将覆盖旧值
	 *
	 * @param dataset 可迭代数据集合
	 * @param keyFunc 键提取函数
	 *
	 * @return HashMap
	 */
	public static <T, K> Map<K, T> toHashMap(Iterable<T> dataset, Function<T, K> keyFunc) {
		return toHashMap(dataset, keyFunc, Function.identity(), (e1, e2) -> e2);
	}

	/**
	 * 将可迭代数据集转换为HashMap 若值重复，新值将覆盖旧值
	 *
	 * @param dataset   可迭代数据集合
	 * @param keyFunc   键提取函数
	 * @param valueFunc 值提取函数
	 *
	 * @return HashMap
	 */
	public static <T, K, V> Map<K, V> toHashMap(Iterable<T> dataset, Function<T, K> keyFunc, Function<T, V> valueFunc) {
		return toHashMap(dataset, keyFunc, valueFunc, (e1, e2) -> e2);

	}

	/**
	 * 将可迭代数据集转换为HashMap
	 *
	 * @param dataset       可迭代数据集合
	 * @param keyFunc       键提取函数
	 * @param valueFunc     值提取函数
	 * @param mergeFunction 值重复时的合并函数
	 *
	 * @return HashMap
	 */
	public static <T, K, V> Map<K, V> toHashMap(Iterable<T> dataset, Function<T, K> keyFunc, Function<T, V> valueFunc,
												BiFunction<? super V, ? super V, ? extends V> mergeFunction) {
		return toMap(dataset, new HashMap<>(), keyFunc, valueFunc, mergeFunction);
	}

	/**
	 * 将可迭代数据集转换为Map,若值重复，新值将覆盖旧值
	 *
	 * @param dataset   可迭代数据集合
	 * @param container 容器
	 * @param keyFunc   键提取函数
	 *
	 * @return Map
	 */
	public static <T, K> Map<K, T> toMap(Iterable<T> dataset,
										 Map<K, T> container,
										 Function<T, K> keyFunc) {
		return toMap(dataset, container, keyFunc, Function.identity(), (e1, e2) -> e2);
	}

	/**
	 * 将可迭代数据集转换为Map,若值重复，新值将覆盖旧值
	 *
	 * @param dataset   可迭代数据集合
	 * @param container 容器
	 * @param keyFunc   键提取函数
	 * @param valueFunc 值提取函数
	 *
	 * @return Map
	 */
	public static <T, K, V> Map<K, V> toMap(Iterable<T> dataset,
											Map<K, V> container,
											Function<T, K> keyFunc,
											Function<T, V> valueFunc) {
		return toMap(dataset, container, keyFunc, valueFunc, (e1, e2) -> e2);
	}

	/**
	 * 将可迭代数据集转换为Map
	 *
	 * @param dataset       可迭代数据集合
	 * @param container     容器
	 * @param keyFunc       键提取函数
	 * @param valueFunc     值提取函数
	 * @param mergeFunction 值重复时的合并函数
	 *
	 * @return Map
	 */
	public static <T, K, V> Map<K, V> toMap(Iterable<T> dataset,
											Map<K, V> container,
											Function<T, K> keyFunc,
											Function<T, V> valueFunc,
											BiFunction<? super V, ? super V, ? extends V> mergeFunction) {
		container = container == null ? new HashMap<>() : container;
		if (isEmpty(dataset)) {
			return container;
		}
		for (T item : dataset) {
			K key = keyFunc.apply(item);
			V value = valueFunc.apply(item);
			container.merge(key, value, mergeFunction);
		}
		return container;
	}


	public static long toMillisecond(TemporalAccessor temporalAccessor) {
		return toMillisecond(temporalAccessor, DEFAULT_ZONE);
	}

	public static long toMillisecond(TemporalAccessor temporalAccessor, ZoneId zoneId) {
		if (temporalAccessor == null) {
			return 0;
		}
		if (temporalAccessor instanceof LocalDate) {
			LocalDate localDate = (LocalDate) temporalAccessor;
			return localDate.atStartOfDay(zoneId)
					.toInstant()
					.toEpochMilli();
		}
		if (temporalAccessor instanceof LocalTime) {
			LocalTime localTime = (LocalTime) temporalAccessor;
			return localTime.atDate(LocalDate.now(zoneId))
					.atZone(zoneId)
					.toInstant()
					.toEpochMilli();
		}
		if (temporalAccessor instanceof LocalDateTime) {
			LocalDateTime localDateTime = (LocalDateTime) temporalAccessor;
			return localDateTime.atZone(zoneId)
					.toInstant()
					.toEpochMilli();
		}
		return Instant.from(temporalAccessor).toEpochMilli();
	}

	public static java.util.Date toDate(TemporalAccessor temporalAccessor) {
		return toDate(toMillisecond(temporalAccessor));
	}

	public static long toMillisecond(java.util.Date date) {
		if (date == null) {
			return 0;
		}
		return date.getTime();
	}

	/**
	 * 判断多个集合是否都为空
	 *
	 * @param iterables 多个集合
	 *
	 * @return 是否都为空, true: 集合都为空, false: 至少有一个集合不为空
	 */
	public static boolean isEmptyAll(Iterable<?>... iterables) {
		return Arrays.stream(iterables).allMatch(Utils::isEmpty);
	}

	public static Date toDate(long millisecond) {
		return new Date(millisecond);
	}

	/**
	 * 格式化日期 ： yyyy-MM-dd HH:mm:ss.SSS
	 *
	 * @param date 日期
	 *
	 * @return 格式化后的日期
	 */
	public static String formatDatetime(Date date) {
		if (date == null) {
			return null;
		}
		return formatMillis(date.getTime());
	}

	/**
	 * 格式化日期 ： yyyy-MM-dd HH:mm:ss.SSS
	 * <p>
	 * 支持 LocalDate（取当天 00:00:00.000）、LocalTime（取当天该时刻）、LocalDateTime、Instant 等
	 *
	 * @param temporalAccessor 日期
	 *
	 * @return 格式化后的日期
	 */
	public static String formatDatetime(TemporalAccessor temporalAccessor) {
		if (temporalAccessor == null) {
			return null;
		}
		return formatMillis(toMillisecond(temporalAccessor));
	}

	/**
	 * 格式化时间戳 ： yyyy-MM-dd HH:mm:ss.SSS
	 *
	 * @param millisecond 时间戳（毫秒）
	 *
	 * @return 格式化后的日期
	 */
	private static String formatMillis(long millisecond) {
		return FULL_DATE_FORMAT.format(Instant.ofEpochMilli(millisecond).atZone(DEFAULT_ZONE));
	}

	/**
	 * 格式化数据大小，1024 进制、保留 2 位小数、去掉末尾多余的 0
	 * <p>
	 * 例：formatDataSize(0) -> "0 B"，formatDataSize(1536) -> "1.5 KB"，formatDataSize(1048576) -> "1 MB"
	 *
	 * @param bytes 字节数
	 *
	 * @return 格式化后的文本
	 *
	 * @see FormatUtils#formatDataSize(long)
	 */
	public static String formatDataSize(long bytes) {
		return FormatUtils.formatDataSize(bytes);
	}

	/**
	 * 格式化数据大小，指定小数位数（1024 进制）
	 * <p>
	 * 例：formatDataSize(1536, 0) -> "2 KB"，formatDataSize(1536, 3) -> "1.5 KB"
	 *
	 * @param bytes 字节数
	 * @param scale 小数位数，范围 0 ~ 10
	 *
	 * @return 格式化后的文本
	 */
	public static String formatDataSize(long bytes, int scale) {
		return FormatUtils.formatDataSize(bytes, scale);
	}

	/**
	 * 解析数据大小文本为字节数
	 * <p>
	 * 例：parseDataSize("300MB") -> 314572800，parseDataSize("1.5 GB") -> 1610612736
	 *
	 * @param text 数据大小文本，如 "300MB"、"1.5GB"、"1024"
	 *
	 * @return 字节数
	 *
	 * @see FormatUtils#parseDataSize(String)
	 */
	public static long parseDataSize(String text) {
		return FormatUtils.parseDataSize(text);
	}

	/**
	 * 数据大小单位换算
	 * <p>
	 * 例：convertDataSize("300MB", "GB") -> "0.29 GB"，convertDataSize("1024", "KB") -> "1 KB"
	 *
	 * @param text 数据大小文本，如 "300MB"
	 * @param unit 目标单位，如 "GB"、"MB"、"字节"，忽略大小写
	 *
	 * @return 目标单位下的格式化文本
	 *
	 * @see FormatUtils#convertDataSize(String, String)
	 */
	public static String convertDataSize(String text, String unit) {
		return FormatUtils.convertDataSize(text, unit);
	}

	/**
	 * 安全读取值，如果源对象为空或函数为空则返回默认值
	 *
	 * @param source         源对象
	 * @param valueFuncation 读值函数
	 *
	 * @return 值
	 */
	public static <T, R> R safeReadValue(T source, Function<T, R> valueFuncation) {
		return safeReadValue(source, valueFuncation, null);
	}

	/**
	 * 安全读取值，如果源对象为空或函数为空则返回默认值
	 *
	 * @param source         源对象
	 * @param valueFuncation 读值函数
	 * @param defaultValue   默认值
	 *
	 * @return 值
	 */
	public static <T, R> R safeReadValue(T source, Function<T, R> valueFuncation, R defaultValue) {
		if (source == null || valueFuncation == null) {
			return defaultValue;
		}
		R value = valueFuncation.apply(source);
		return orElseValue(value, defaultValue);
	}

	/**
	 * 如果源对象为空则返回默认值，否则返回源对象
	 *
	 * @param source       源对象
	 * @param defaultValue 默认值
	 *
	 * @return 值
	 */
	public static <T> T orElseValue(T source, T defaultValue) {
		if (source == null) {
			return defaultValue;
		}
		return source;
	}

	/**
	 * 返回第一个不为空的字符串
	 *
	 * @param strings 可变参数，代表可能为空的字符串数组。
	 *
	 * @return 第一个不为空的字符串，如果所有字符串都为空，则返回 null。
	 */
	public static String firstNotBlank(String... strings) {
		if (strings == null) {
			return null;
		}
		return Arrays.stream(strings).filter(Objects::nonNull).filter(Utils::isNotBlank).findFirst().orElse(null);
	}

	@SafeVarargs
	public static <T> Stream<T> stream(T... dataArray) {
		if (dataArray == null) {
			return Stream.empty();
		}
		return Arrays.stream(dataArray);
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		if (iterable == null) {
			return Stream.empty();
		}
		if (iterable instanceof Collection<?>) {
			return ((Collection<T>) iterable).stream();
		}

		List<T> temp = new ArrayList<>();
		for (T t : iterable) {
			temp.add(t);
		}
		return temp.stream();
	}

	/**
	 * 遍历集合
	 *
	 * @param iterable 集合
	 * @param action   遍历操作
	 */
	public static <T> void forEach(Iterable<T> iterable, Consumer<T> action) {
		forEach(iterable, action, null);
	}

	/**
	 * 遍历集合
	 *
	 * @param iterable           集合
	 * @param action             遍历操作
	 * @param interruptePredicte 中断条件
	 */
	public static <T> void forEach(Iterable<T> iterable, Consumer<T> action, Predicate<T> interruptePredicte) {
		if (iterable == null || action == null) {
			return;
		}
		for (T item : iterable) {
			if (interruptePredicte != null && interruptePredicte.test(item)) {
				break;
			}
			action.accept(item);
		}
	}
}
