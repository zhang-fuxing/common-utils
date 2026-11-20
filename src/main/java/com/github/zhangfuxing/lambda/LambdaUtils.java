package com.github.zhangfuxing.lambda;

import com.github.zhangfuxing.reflect.ReflectUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lambda 工具，通过可序列化的方法引用获取方法名和字段名
 * <p>
 * 使用方式：
 * <pre>{@code
 * LambdaUtils.getMethodName(User::getName);      // getName
 * LambdaUtils.getMethodName(user::getName);      // getName
 * LambdaUtils.getMethodName(User::setName);      // setName
 *
 * LambdaUtils.getFieldName(User::getName);       // name
 * LambdaUtils.getFieldName(user::isActive);      // active
 * LambdaUtils.getFieldName(User::setName);       // name
 * }</pre>
 * <p>
 * 只支持方法引用，不支持 {@code () -> user.getName()} 这类 lambda 代码块：
 * 代码块会被编译器生成 {@code lambda$...} 合成方法，无法还原真实方法名。
 * <p>
 * 方法名和字段名来自 {@link SerializedLambda}，要求方法引用所在的函数式接口继承
 * {@link Serializable}。本类提供的 {@link NoArg}、{@link OneArg}、{@link TwoArg}
 * 已继承 Serializable，可直接使用。方法引用支持 0～2 个参数。
 *
 * @author 张福兴
 * @version 1.0
 * @date 2026/11/20
 * @email zhangfuxing1010@163.com
 */
public final class LambdaUtils {

	private LambdaUtils() {
	}

	/**
	 * 获取无参数方法引用的方法名
	 *
	 * @param fn 方法引用，如 {@code user::getName}
	 *
	 * @return 方法名
	 */
	public static String getMethodName(NoArg fn) {
		return methodName(fn);
	}

	/**
	 * 获取单参数方法引用的方法名
	 *
	 * @param fn 方法引用，如 {@code User::getName}
	 *
	 * @return 方法名
	 */
	public static <T> String getMethodName(OneArg<T> fn) {
		return methodName(fn);
	}

	/**
	 * 获取双参数方法引用的方法名
	 *
	 * @param fn 方法引用，如 {@code User::setName}
	 *
	 * @return 方法名
	 */
	public static <T, U> String getMethodName(TwoArg<T, U> fn) {
		return methodName(fn);
	}

	/**
	 * 从无参数 getter 方法引用推导字段名
	 *
	 * @param getter getter 方法引用，如 {@code user::getName}
	 *
	 * @return 字段名
	 */
	public static String getFieldName(NoArg getter) {
		return toFieldName(methodName(getter));
	}

	/**
	 * 从单参数 getter 或 setter 方法引用推导字段名
	 * <p>
	 * 支持 {@code getName}、{@code isActive} 这类命名；没有 get/is 前缀时按
	 * record / fluent 风格原样返回。
	 *
	 * @param getter getter 或 setter 方法引用，如 {@code User::getName}、{@code user::setName}
	 *
	 * @return 字段名
	 */
	public static <T> String getFieldName(OneArg<T> getter) {
		return toFieldName(methodName(getter));
	}

	/**
	 * 从双参数 setter 方法引用推导字段名
	 *
	 * @param setter setter 方法引用，如 {@code User::setName}
	 *
	 * @return 字段名
	 */
	public static <T, U> String getFieldName(TwoArg<T, U> setter) {
		return toFieldName(methodName(setter));
	}

	/**
	 * 解析方法引用，提取方法名
	 */
	private static String methodName(Serializable lambda) {
		SerializedLambda serializedLambda = resolve(lambda);
		return serializedLambda.getImplMethodName();
	}

	/**
	 * 解析可序列化方法引用
	 */
	private static SerializedLambda resolve(Serializable lambda) {
		if (lambda == null) {
			throw new IllegalArgumentException("lambda 不能为 null");
		}
		try {
			Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
			if (!ReflectUtils.makeAccessible(writeReplace)) {
				throw new IllegalArgumentException("无法访问 Lambda 的 writeReplace 方法，请检查运行环境的反射权限");
			}
			Object replacement = writeReplace.invoke(lambda);
			if (!(replacement instanceof SerializedLambda)) {
				throw new IllegalArgumentException("无法解析 Lambda，请使用方法引用");
			}

			SerializedLambda serializedLambda = (SerializedLambda) replacement;
			if (serializedLambda.getImplMethodName().startsWith("lambda$")) {
				throw new IllegalArgumentException("请使用方法引用，不要使用 lambda 表达式: "
						+ serializedLambda.getImplMethodName());
			}
			return serializedLambda;
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("无法解析 Lambda，请使用方法引用", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("无法访问 Lambda 的 writeReplace 方法", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			throw new IllegalArgumentException("解析 Lambda 失败", cause == null ? e : cause);
		}
	}

	/**
	 * 按 JavaBean 命名规则把方法名转成字段名
	 */
	private static String toFieldName(String methodName) {
		if (methodName.startsWith("get") && methodName.length() > 3
				&& Character.isUpperCase(methodName.charAt(3))) {
			return decapitalize(methodName.substring(3));
		}
		if (methodName.startsWith("is") && methodName.length() > 2
				&& Character.isUpperCase(methodName.charAt(2))) {
			return decapitalize(methodName.substring(2));
		}
		if (methodName.startsWith("set") && methodName.length() > 3
				&& Character.isUpperCase(methodName.charAt(3))) {
			return decapitalize(methodName.substring(3));
		}
		return methodName;
	}

	/**
	 * 首字母小写；前两个字母都大写时保持原样（如 URL）
	 */
	private static String decapitalize(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0))
				&& Character.isUpperCase(name.charAt(1))) {
			return name;
		}
		char[] chars = name.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}

	/**
	 * 无参数方法引用，适用于 {@code user::getName}、{@code thread::start}
	 */
	@FunctionalInterface
	public interface NoArg extends Runnable, Serializable {
	}

	/**
	 * 单参数方法引用，适用于 {@code User::getName}、{@code user::setName}
	 */
	@FunctionalInterface
	public interface OneArg<T> extends Consumer<T>, Serializable {
	}

	/**
	 * 双参数方法引用，适用于 {@code User::setName}、{@code map::put}
	 */
	@FunctionalInterface
	public interface TwoArg<T, U> extends BiConsumer<T, U>, Serializable {
	}
}
