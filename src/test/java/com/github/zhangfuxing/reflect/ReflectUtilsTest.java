package com.github.zhangfuxing.reflect;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ReflectUtils} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/20
 */
public class ReflectUtilsTest {

	@Test
	public void testJdkVersion() {
		assertTrue(ReflectUtils.getJavaVersion() >= 8);
		assertEquals(ReflectUtils.getJavaVersion() >= 9, ReflectUtils.isJdk9OrLater());
	}

	@Test
	public void testFieldAccess() {
		Sample sample = new Sample("Tom", 18);
		assertEquals("Tom", ReflectUtils.getFieldValue(sample, "name"));
		assertEquals("parent", ReflectUtils.getFieldValue(sample, "parentField"));

		ReflectUtils.setFieldValue(sample, "name", "Jerry");
		assertEquals("Jerry", ReflectUtils.getFieldValue(sample, "name"));

		assertEquals("static", ReflectUtils.getStaticFieldValue(Sample.class, "staticField"));
		ReflectUtils.setStaticFieldValue(Sample.class, "staticField", "changed");
		assertEquals("changed", ReflectUtils.getStaticFieldValue(Sample.class, "staticField"));
		assertEquals("interface", ReflectUtils.getFieldValue(sample, "INTERFACE_FIELD"));

		Field parentField = ReflectUtils.getField(Sample.class, "parentField");
		assertEquals(Parent.class, parentField.getDeclaringClass());
		assertTrue(ReflectUtils.makeAccessible(parentField));
	}

	@Test
	public void testMethodInvocation() {
		Sample sample = new Sample("Tom", 18);
		assertEquals("Hi Tom", ReflectUtils.invokeMethod(sample, "greet", "Hi "));
		assertEquals(3, ReflectUtils.invokeStaticMethod(Sample.class, "add", 1, 2));
		assertEquals("int:7", ReflectUtils.invokeMethod(sample, "overloaded", 7));
		assertEquals("String:x", ReflectUtils.invokeMethod(sample, "overloaded", "x"));
		assertEquals("value:null", ReflectUtils.invokeMethod(sample, "nullable", new Object[]{null}));

		assertEquals("a,b", ReflectUtils.invokeMethod(sample, "join", "a", "b"));
		assertEquals("a,b", ReflectUtils.invokeMethod(sample, "join", new Object[]{new String[]{"a", "b"}}));
		assertEquals(3L, ReflectUtils.invokeMethod(sample, "widen", 3));
		assertEquals(3L, ReflectUtils.invokeStaticMethod(Sample.class, "sum", 1, 2));
	}

	@Test
	public void testConstructorAndLookup() {
		Sample sample = ReflectUtils.newInstance(Sample.class, "Bob", 20);
		assertEquals("Bob", ReflectUtils.getFieldValue(sample, "name"));
		assertEquals(20, ReflectUtils.getFieldValue(sample, "age"));

		Constructor<?> constructor = ReflectUtils.getConstructor(Sample.class, String.class, int.class);
		assertNotNull(constructor);

		Method greet = ReflectUtils.getMethod(Sample.class, "greet", String.class);
		assertNotNull(greet);
		assertEquals(Sample.class, greet.getDeclaringClass());
	}

	@Test
	public void testInvalidMember() {
		Sample sample = new Sample("Tom", 18);
		assertThrows(IllegalArgumentException.class, () -> ReflectUtils.getFieldValue(sample, "missing"));
		assertThrows(IllegalArgumentException.class, () -> ReflectUtils.invokeMethod(sample, "missing"));
		assertThrows(IllegalArgumentException.class, () -> ReflectUtils.newInstance(Sample.class, "only-name"));
		assertThrows(IllegalArgumentException.class, () -> ReflectUtils.getStaticFieldValue(Sample.class, "name"));
	}

	private interface Named {
		String INTERFACE_FIELD = "interface";
	}

	private static class Parent {
		private String parentField = "parent";
	}

	private static class Sample extends Parent implements Named {
		private String name;
		private int age;
		private static String staticField = "static";

		private Sample(String name, int age) {
			this.name = name;
			this.age = age;
		}

		private String greet(String prefix) {
			return prefix + name;
		}

		private static int add(int a, int b) {
			return a + b;
		}

		private String overloaded(int value) {
			return "int:" + value;
		}

		private String overloaded(String value) {
			return "String:" + value;
		}

		private String nullable(String value) {
			return "value:" + value;
		}

		private String join(String... values) {
			return String.join(",", values);
		}

		private long widen(long value) {
			return value;
		}

		private static long sum(long... values) {
			long sum = 0;
			for (long value : values) {
				sum += value;
			}
			return sum;
		}
	}
}
