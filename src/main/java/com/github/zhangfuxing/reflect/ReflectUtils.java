package com.github.zhangfuxing.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 反射工具，提供字段读写、方法调用和构造实例能力
 * <p>
 * 访问控制按 JDK 版本选择：
 * <ul>
 *     <li>JDK 8：使用 {@link AccessibleObject#setAccessible(boolean)}</li>
 *     <li>JDK 9+：使用 {@code trySetAccessible()}，尊重模块访问控制</li>
 * </ul>
 * JDK 9+ 下如果目标包没有对当前模块开放，工具不会尝试绕过限制，而是返回 false
 * 或抛出带说明的异常。可通过启动参数 {@code --add-opens} 显式开放目标包，
 * 或改用目标类的公开 API。
 * <p>
 * 例：
 * <pre>{@code
 * ReflectUtils.getFieldValue(user, "name");
 * ReflectUtils.setFieldValue(user, "name", "张三");
 * ReflectUtils.invokeMethod(user, "greet", "Hello");
 * ReflectUtils.invokeStaticMethod(Math.class, "max", 1, 2);
 * ReflectUtils.newInstance(User.class, "张三", 18);
 * }</pre>
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public final class ReflectUtils {

	private static final Object[] EMPTY_ARGS = new Object[0];

	private static final int JAVA_VERSION = detectJavaVersion();

	private static final AccessStrategy ACCESS_STRATEGY = JAVA_VERSION >= 9
			? new Java9AccessStrategy()
			: new Java8AccessStrategy();

	private ReflectUtils() {
	}

	/**
	 * 当前 JDK 主版本号，无法识别时返回 8
	 */
	public static int getJavaVersion() {
		return JAVA_VERSION;
	}

	/**
	 * 是否为 JDK 9 及以上版本
	 */
	public static boolean isJdk9OrLater() {
		return JAVA_VERSION >= 9;
	}

	/**
	 * 查找字段，优先查找当前类，再沿父类向上查找，最后查找接口常量
	 *
	 * @param type      目标类型
	 * @param fieldName 字段名
	 *
	 * @return 字段
	 *
	 * @throws IllegalArgumentException 字段不存在
	 */
	public static Field getField(Class<?> type, String fieldName) {
		checkType(type);
		checkName(fieldName, "fieldName");

		Class<?> current = type;
		while (current != null) {
			try {
				return current.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}

		Field field = findInterfaceField(type, fieldName, new HashSet<Class<?>>());
		if (field != null) {
			return field;
		}
		throw new IllegalArgumentException("找不到字段 " + type.getName() + "." + fieldName);
	}

	/**
	 * 查找方法，优先查找 public 方法，再沿父类查找声明的方法
	 *
	 * @param type           目标类型
	 * @param methodName     方法名
	 * @param parameterTypes 参数类型
	 *
	 * @return 方法
	 *
	 * @throws IllegalArgumentException 方法不存在
	 */
	public static Method getMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
		checkType(type);
		checkName(methodName, "methodName");
		Class<?>[] types = parameterTypes == null ? new Class<?>[0] : parameterTypes;

		try {
			return type.getMethod(methodName, types);
		} catch (NoSuchMethodException ignored) {
			// 继续查找非 public 方法
		}

		Class<?> current = type;
		while (current != null) {
			try {
				return current.getDeclaredMethod(methodName, types);
			} catch (NoSuchMethodException ignored) {
				current = current.getSuperclass();
			}
		}
		throw new IllegalArgumentException("找不到方法 " + type.getName() + "." + methodName
				+ "(" + typeNames(types) + ")");
	}

	/**
	 * 查找构造方法
	 *
	 * @param type           目标类型
	 * @param parameterTypes 参数类型
	 *
	 * @return 构造方法
	 *
	 * @throws IllegalArgumentException 构造方法不存在
	 */
	public static Constructor<?> getConstructor(Class<?> type, Class<?>... parameterTypes) {
		checkType(type);
		Class<?>[] types = parameterTypes == null ? new Class<?>[0] : parameterTypes;
		try {
			return type.getDeclaredConstructor(types);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("找不到构造方法 " + type.getName()
					+ "(" + typeNames(types) + ")", e);
		}
	}

	/**
	 * 读取实例字段，支持私有字段和父类字段
	 *
	 * @param target    目标对象
	 * @param fieldName 字段名
	 *
	 * @return 字段值
	 */
	public static Object getFieldValue(Object target, String fieldName) {
		if (target == null) {
			throw new IllegalArgumentException("target 不能为 null");
		}
		Field field = getField(target.getClass(), fieldName);
		requireAccessible(field, "字段 " + field.getDeclaringClass().getName() + "." + fieldName);
		try {
			return field.get(target);
		} catch (IllegalAccessException e) {
			throw accessException("字段 " + field.getDeclaringClass().getName() + "." + fieldName, e);
		}
	}

	/**
	 * 读取静态字段
	 *
	 * @param type      目标类型
	 * @param fieldName 字段名
	 *
	 * @return 字段值
	 */
	public static Object getStaticFieldValue(Class<?> type, String fieldName) {
		Field field = getField(type, fieldName);
		requireStatic(field);
		requireAccessible(field, "字段 " + field.getDeclaringClass().getName() + "." + fieldName);
		try {
			return field.get(null);
		} catch (IllegalAccessException e) {
			throw accessException("字段 " + field.getDeclaringClass().getName() + "." + fieldName, e);
		}
	}

	/**
	 * 设置实例字段，支持私有字段和父类字段
	 *
	 * @param target    目标对象
	 * @param fieldName 字段名
	 * @param value     字段值
	 */
	public static void setFieldValue(Object target, String fieldName, Object value) {
		if (target == null) {
			throw new IllegalArgumentException("target 不能为 null");
		}
		Field field = getField(target.getClass(), fieldName);
		requireAccessible(field, "字段 " + field.getDeclaringClass().getName() + "." + fieldName);
		try {
			field.set(target, value);
		} catch (IllegalAccessException e) {
			throw accessException("字段 " + field.getDeclaringClass().getName() + "." + fieldName, e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("字段 " + field.getDeclaringClass().getName() + "."
					+ fieldName + " 的值类型不匹配: " + value, e);
		}
	}

	/**
	 * 设置静态字段
	 *
	 * @param type      目标类型
	 * @param fieldName 字段名
	 * @param value     字段值
	 */
	public static void setStaticFieldValue(Class<?> type, String fieldName, Object value) {
		Field field = getField(type, fieldName);
		requireStatic(field);
		requireAccessible(field, "字段 " + field.getDeclaringClass().getName() + "." + fieldName);
		try {
			field.set(null, value);
		} catch (IllegalAccessException e) {
			throw accessException("字段 " + field.getDeclaringClass().getName() + "." + fieldName, e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("字段 " + field.getDeclaringClass().getName() + "."
					+ fieldName + " 的值类型不匹配: " + value, e);
		}
	}

	/**
	 * 调用实例方法，支持私有方法和父类方法
	 * <p>
	 * 参数会按精确类型、装箱/拆箱、基本类型拓宽和父类型兼容性匹配重载。
	 *
	 * @param target     目标对象
	 * @param methodName 方法名
	 * @param args       参数
	 *
	 * @return 方法返回值
	 */
	public static Object invokeMethod(Object target, String methodName, Object... args) {
		if (target == null) {
			throw new IllegalArgumentException("target 不能为 null");
		}
		Object[] actualArgs = args == null ? EMPTY_ARGS : args;
		MethodMatch match = resolveMethod(target.getClass(), methodName, actualArgs, false);
		return invokeMatch(target, match);
	}

	/**
	 * 调用静态方法
	 *
	 * @param type       目标类型
	 * @param methodName 方法名
	 * @param args       参数
	 *
	 * @return 方法返回值
	 */
	public static Object invokeStaticMethod(Class<?> type, String methodName, Object... args) {
		checkType(type);
		Object[] actualArgs = args == null ? EMPTY_ARGS : args;
		MethodMatch match = resolveMethod(type, methodName, actualArgs, true);
		return invokeMatch(null, match);
	}

	/**
	 * 构造实例，支持私有构造方法
	 *
	 * @param type 目标类型
	 * @param args 构造参数
	 *
	 * @return 实例
	 */
	public static <T> T newInstance(Class<T> type, Object... args) {
		checkType(type);
		Object[] actualArgs = args == null ? EMPTY_ARGS : args;
		ConstructorMatch match = resolveConstructor(type, actualArgs);
		Constructor<?> constructor = match.constructor;
		requireAccessible(constructor, "构造方法 " + type.getName());
		try {
			return type.cast(constructor.newInstance(match.arguments));
		} catch (InvocationTargetException e) {
			throw propagate(e);
		} catch (InstantiationException e) {
			throw new IllegalStateException("无法实例化 " + type.getName(), e);
		} catch (IllegalAccessException e) {
			throw accessException("构造方法 " + type.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("构造方法 " + type.getName() + " 参数不匹配", e);
		}
	}

	/**
	 * 尝试开启反射访问
	 * <p>
	 * JDK 8 使用 {@code setAccessible(true)}；JDK 9+ 使用 {@code trySetAccessible()}。
	 * 目标包未开放时返回 false，不会尝试绕过模块访问控制。
	 *
	 * @param object 字段、方法或构造方法
	 *
	 * @return 是否成功开启访问
	 */
	public static boolean makeAccessible(AccessibleObject object) {
		if (object == null) {
			throw new IllegalArgumentException("object 不能为 null");
		}
		return ACCESS_STRATEGY.makeAccessible(object);
	}

	// -------------------------------------------------------------------- 内部

	private static Field findInterfaceField(Class<?> type, String fieldName, Set<Class<?>> visited) {
		for (Class<?> iface : type.getInterfaces()) {
			if (!visited.add(iface)) {
				continue;
			}
			try {
				return iface.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				Field field = findInterfaceField(iface, fieldName, visited);
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}

	private static void checkType(Class<?> type) {
		if (type == null) {
			throw new IllegalArgumentException("type 不能为 null");
		}
	}

	private static void checkName(String name, String description) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException(description + " 不能为空");
		}
	}

	private static void requireStatic(Field field) {
		if (!Modifier.isStatic(field.getModifiers())) {
			throw new IllegalArgumentException("字段不是静态字段: "
					+ field.getDeclaringClass().getName() + "." + field.getName());
		}
	}

	private static void requireAccessible(AccessibleObject object, String description) {
		if (!makeAccessible(object)) {
			throw new IllegalStateException("无法访问" + description + "；" + accessHint());
		}
	}

	private static IllegalStateException accessException(String description, Throwable cause) {
		return new IllegalStateException("无法访问" + description + "；" + accessHint(), cause);
	}

	private static String accessHint() {
		if (JAVA_VERSION >= 9) {
			return "目标所在的包未对当前模块开放，请通过启动参数 --add-opens 显式开放，或改用公开 API";
		}
		return "请检查 SecurityManager 的反射访问权限";
	}

	private static int detectJavaVersion() {
		String version = System.getProperty("java.specification.version", "1.8");
		try {
			if (version.startsWith("1.")) {
				version = version.substring(2);
			}
			int dot = version.indexOf('.');
			if (dot >= 0) {
				version = version.substring(0, dot);
			}
			int dash = version.indexOf('-');
			if (dash >= 0) {
				version = version.substring(0, dash);
			}
			return Integer.parseInt(version);
		} catch (RuntimeException e) {
			return 8;
		}
	}

	private static MethodMatch resolveMethod(Class<?> type, String methodName, Object[] args, boolean staticOnly) {
		List<Method> candidates = collectMethods(type, methodName, staticOnly);
		MethodMatch best = null;
		boolean ambiguous = false;

		for (Method method : candidates) {
			MethodMatch match = matchMethod(method, args);
			if (match == null) {
				continue;
			}
			if (best == null || match.score < best.score) {
				best = match;
				ambiguous = false;
			} else if (match.score == best.score) {
				if (moreSpecific(match.method, best.method)) {
					best = match;
					ambiguous = false;
				} else if (!moreSpecific(best.method, match.method)) {
					ambiguous = true;
				}
			}
		}

		if (best == null) {
			throw new IllegalArgumentException("找不到匹配的方法 " + type.getName() + "." + methodName
					+ "(" + typeNames(args) + ")");
		}
		if (ambiguous) {
			throw new IllegalArgumentException("方法 " + type.getName() + "." + methodName
					+ " 存在多个匹配重载，参数: " + typeNames(args));
		}
		return best;
	}

	private static List<Method> collectMethods(Class<?> type, String methodName, boolean staticOnly) {
		List<Method> methods = new ArrayList<>();
		Set<String> signatures = new HashSet<>();

		for (Method method : type.getMethods()) {
			addMethod(methods, signatures, method, methodName, staticOnly);
		}
		Class<?> current = type;
		while (current != null) {
			for (Method method : current.getDeclaredMethods()) {
				addMethod(methods, signatures, method, methodName, staticOnly);
			}
			current = current.getSuperclass();
		}
		return methods;
	}

	private static void addMethod(List<Method> methods, Set<String> signatures,
								  Method method, String methodName, boolean staticOnly) {
		if (!method.getName().equals(methodName)
				|| method.isBridge()
				|| method.isSynthetic()
				|| (staticOnly && !Modifier.isStatic(method.getModifiers()))) {
			return;
		}
		String signature = method.getName() + Arrays.toString(method.getParameterTypes());
		if (signatures.add(signature)) {
			methods.add(method);
		}
	}

	private static MethodMatch matchMethod(Method method, Object[] args) {
		ArgumentsMatch match = matchArguments(method, args);
		if (match == null) {
			return null;
		}
		return new MethodMatch(method, match.arguments, match.score);
	}

	private static ConstructorMatch resolveConstructor(Class<?> type, Object[] args) {
		ConstructorMatch best = null;
		boolean ambiguous = false;

		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
			ArgumentsMatch match = matchArguments(constructor, args);
			if (match == null) {
				continue;
			}
			ConstructorMatch candidate = new ConstructorMatch(constructor, match.arguments, match.score);
			if (best == null || candidate.score < best.score) {
				best = candidate;
				ambiguous = false;
			} else if (candidate.score == best.score) {
				if (moreSpecific(candidate.constructor, best.constructor)) {
					best = candidate;
					ambiguous = false;
				} else if (!moreSpecific(best.constructor, candidate.constructor)) {
					ambiguous = true;
				}
			}
		}

		if (best == null) {
			throw new IllegalArgumentException("找不到匹配的构造方法 " + type.getName()
					+ "(" + typeNames(args) + ")");
		}
		if (ambiguous) {
			throw new IllegalArgumentException("构造方法 " + type.getName()
					+ " 存在多个匹配重载，参数: " + typeNames(args));
		}
		return best;
	}

	private static ArgumentsMatch matchArguments(Executable executable, Object[] args) {
		Class<?>[] params = executable.getParameterTypes();
		if (!executable.isVarArgs()) {
			if (params.length != args.length) {
				return null;
			}
			int score = scoreParameters(params, args, params.length);
			return score < 0 ? null : new ArgumentsMatch(args, score);
		}

		int fixedCount = params.length - 1;
		Class<?> arrayType = params[fixedCount];
		Class<?> componentType = arrayType.getComponentType();

		if (args.length == params.length) {
			int score = scoreParameters(params, args, params.length);
			if (score >= 0) {
				return new ArgumentsMatch(args, score);
			}
		}

		if (args.length < fixedCount) {
			return null;
		}
		int score = scoreParameters(params, args, fixedCount);
		if (score < 0) {
			return null;
		}
		for (int i = fixedCount; i < args.length; i++) {
			int parameterScore = scoreParameter(componentType, args[i]);
			if (parameterScore < 0) {
				return null;
			}
			score += parameterScore;
		}
		return new ArgumentsMatch(packVarArgs(args, fixedCount, componentType), score);
	}

	private static int scoreParameters(Class<?>[] params, Object[] args, int count) {
		if (args.length < count) {
			return -1;
		}
		int score = 0;
		for (int i = 0; i < count; i++) {
			int parameterScore = scoreParameter(params[i], args[i]);
			if (parameterScore < 0) {
				return -1;
			}
			score += parameterScore;
		}
		return score;
	}

	private static int scoreParameter(Class<?> param, Object arg) {
		if (arg == null) {
			return param.isPrimitive() ? -1 : 0;
		}

		Class<?> argType = arg.getClass();
		if (param.isPrimitive()) {
			Class<?> wrapper = wrapperOf(param);
			if (wrapper == argType) {
				return 1;
			}
			Class<?> primitive = primitiveOf(argType);
			if (primitive == null) {
				return -1;
			}
			int distance = wideningDistance(primitive, param);
			return distance < 0 ? -1 : 3 + distance;
		}
		if (param == argType) {
			return 0;
		}
		return param.isAssignableFrom(argType) ? 2 : -1;
	}

	private static Object[] packVarArgs(Object[] args, int fixedCount, Class<?> componentType) {
		Object array = Array.newInstance(componentType, args.length - fixedCount);
		for (int i = fixedCount; i < args.length; i++) {
			Array.set(array, i - fixedCount, args[i]);
		}
		Object[] packed = new Object[fixedCount + 1];
		System.arraycopy(args, 0, packed, 0, fixedCount);
		packed[fixedCount] = array;
		return packed;
	}

	private static boolean moreSpecific(Executable first, Executable second) {
		Class<?>[] firstParams = first.getParameterTypes();
		Class<?>[] secondParams = second.getParameterTypes();
		if (firstParams.length != secondParams.length) {
			return false;
		}
		boolean strict = false;
		for (int i = 0; i < firstParams.length; i++) {
			if (firstParams[i].equals(secondParams[i])) {
				continue;
			}
			if (firstParams[i].isPrimitive() || secondParams[i].isPrimitive()) {
				return false;
			}
			if (!secondParams[i].isAssignableFrom(firstParams[i])) {
				return false;
			}
			strict = true;
		}
		return strict;
	}

	private static Object invokeMatch(Object target, MethodMatch match) {
		Method method = match.method;
		requireAccessible(method, "方法 " + method.getDeclaringClass().getName() + "#" + method.getName());
		try {
			return method.invoke(target, match.arguments);
		} catch (InvocationTargetException e) {
			throw propagate(e);
		} catch (IllegalAccessException e) {
			throw accessException("方法 " + method.getDeclaringClass().getName() + "#" + method.getName(), e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("方法 " + method.getDeclaringClass().getName() + "#"
					+ method.getName() + " 参数不匹配", e);
		}
	}

	private static RuntimeException propagate(InvocationTargetException e) {
		Throwable cause = e.getCause();
		if (cause instanceof RuntimeException) {
			return (RuntimeException) cause;
		}
		if (cause instanceof Error) {
			throw (Error) cause;
		}
		return new IllegalStateException("反射调用抛出异常", cause);
	}

	private static Class<?> wrapperOf(Class<?> primitive) {
		if (primitive == byte.class) {
			return Byte.class;
		}
		if (primitive == short.class) {
			return Short.class;
		}
		if (primitive == int.class) {
			return Integer.class;
		}
		if (primitive == long.class) {
			return Long.class;
		}
		if (primitive == float.class) {
			return Float.class;
		}
		if (primitive == double.class) {
			return Double.class;
		}
		if (primitive == boolean.class) {
			return Boolean.class;
		}
		if (primitive == char.class) {
			return Character.class;
		}
		return primitive;
	}

	private static Class<?> primitiveOf(Class<?> type) {
		if (type == Byte.class) {
			return byte.class;
		}
		if (type == Short.class) {
			return short.class;
		}
		if (type == Integer.class) {
			return int.class;
		}
		if (type == Long.class) {
			return long.class;
		}
		if (type == Float.class) {
			return float.class;
		}
		if (type == Double.class) {
			return double.class;
		}
		if (type == Boolean.class) {
			return boolean.class;
		}
		if (type == Character.class) {
			return char.class;
		}
		return null;
	}

	private static int wideningDistance(Class<?> from, Class<?> to) {
		if (from == to) {
			return 0;
		}
		if (from == byte.class) {
			if (to == short.class) {
				return 1;
			}
			if (to == int.class) {
				return 2;
			}
			if (to == long.class) {
				return 3;
			}
			if (to == float.class) {
				return 4;
			}
			if (to == double.class) {
				return 5;
			}
		}
		if (from == short.class) {
			if (to == int.class) {
				return 1;
			}
			if (to == long.class) {
				return 2;
			}
			if (to == float.class) {
				return 3;
			}
			if (to == double.class) {
				return 4;
			}
		}
		if (from == char.class) {
			if (to == int.class) {
				return 1;
			}
			if (to == long.class) {
				return 2;
			}
			if (to == float.class) {
				return 3;
			}
			if (to == double.class) {
				return 4;
			}
		}
		if (from == int.class) {
			if (to == long.class) {
				return 1;
			}
			if (to == float.class) {
				return 2;
			}
			if (to == double.class) {
				return 3;
			}
		}
		if (from == long.class) {
			if (to == float.class) {
				return 1;
			}
			if (to == double.class) {
				return 2;
			}
		}
		if (from == float.class && to == double.class) {
			return 1;
		}
		return -1;
	}

	private static String typeNames(Class<?>[] types) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(types[i] == null ? "null" : types[i].getName());
		}
		return builder.toString();
	}

	private static String typeNames(Object[] args) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(args[i] == null ? "null" : args[i].getClass().getName());
		}
		return builder.toString();
	}

	private interface AccessStrategy {
		boolean makeAccessible(AccessibleObject object);
	}

	private static final class Java8AccessStrategy implements AccessStrategy {
		@Override
		public boolean makeAccessible(AccessibleObject object) {
			try {
				object.setAccessible(true);
				return true;
			} catch (SecurityException e) {
				return false;
			}
		}
	}

	private static final class Java9AccessStrategy implements AccessStrategy {

		private final Method trySetAccessible = findTrySetAccessible();

		@Override
		public boolean makeAccessible(AccessibleObject object) {
			if (trySetAccessible == null) {
				try {
					object.setAccessible(true);
					return true;
				} catch (SecurityException e) {
					return false;
				}
			}
			try {
				return Boolean.TRUE.equals(trySetAccessible.invoke(object));
			} catch (IllegalAccessException e) {
				return false;
			} catch (InvocationTargetException e) {
				return false;
			} catch (SecurityException e) {
				return false;
			}
		}

		private static Method findTrySetAccessible() {
			try {
				return AccessibleObject.class.getMethod("trySetAccessible");
			} catch (NoSuchMethodException e) {
				return null;
			}
		}
	}

	private static final class ArgumentsMatch {
		private final Object[] arguments;
		private final int score;

		private ArgumentsMatch(Object[] arguments, int score) {
			this.arguments = arguments;
			this.score = score;
		}
	}

	private static final class MethodMatch {
		private final Method method;
		private final Object[] arguments;
		private final int score;

		private MethodMatch(Method method, Object[] arguments, int score) {
			this.method = method;
			this.arguments = arguments;
			this.score = score;
		}
	}

	private static final class ConstructorMatch {
		private final Constructor<?> constructor;
		private final Object[] arguments;
		private final int score;

		private ConstructorMatch(Constructor<?> constructor, Object[] arguments, int score) {
			this.constructor = constructor;
			this.arguments = arguments;
			this.score = score;
		}
	}
}
