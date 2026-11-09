package com.github.zhangfuxing.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/9
 * @email zhangfuxing1010@163.com
 */
public class MapUtils {

	public static Map<Object, Object> mapOf(Object... args) {
		Map<Object, Object> container = new HashMap<>();
		return mapOf(container, args);
	}


	public static Map<Object, Object> mapOf(Map<Object, Object> container, Object... args) {
		if (container == null) {
			container = new HashMap<>();
		}

		if (args == null || args.length == 0) {
			return container;
		}
		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("args length must be even");
		}
		for (int i = 0; i < args.length; i += 2) {
			container.put(args[i], args[i + 1]);
		}
		return container;
	}

	public static <K, V> Map<K, V> convert(Map<?, ?> map, Function<Object, K> keyMapper, Function<Object, V> valueMapper) {
		return convert(new HashMap<>(), map, keyMapper, valueMapper);
	}


	public static <K, V> Map<K, V> convert(Map<K, V> container, Map<?, ?> map, Function<Object, K> keyMapper, Function<Object, V> valueMapper) {
		if (map == null) {
			return null;
		}
		if (keyMapper == null || valueMapper == null) {
			throw new IllegalArgumentException("keyMapper and valueMapper must not be null");
		}

		if (container == null) {
			container = new HashMap<>(map.size(), 1.0f);
		}
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			K newKey = keyMapper.apply(key);
			V newValue = valueMapper.apply(value);
			container.put(newKey, newValue);
		}

		return container;
	}
}
