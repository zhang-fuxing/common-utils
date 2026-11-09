package com.github.zhangfuxing.utils;

import java.text.SimpleDateFormat;
import java.time.*;
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
	private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


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
		return toMillisecond(temporalAccessor, ZoneId.of("Asia/Shanghai"));
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
	 * 格式化日期 ： yyyy-MM-dd HH:mm:ss.sss
	 *
	 * @param date 日期
	 *
	 * @return 格式化后的日期
	 */
	public static String formatDatetime(Date date) {
		if (date == null) {
			return null;
		}
		return FULL_DATE_FORMAT.format(date);
	}

	/**
	 * 格式化日期 ： yyyy-MM-dd HH:mm:ss.sss
	 *
	 * @param temporalAccessor 日期
	 *
	 * @return 格式化后的日期
	 */
	public static String formatDatetime(TemporalAccessor temporalAccessor) {
		return formatDatetime(toDate(temporalAccessor));
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
