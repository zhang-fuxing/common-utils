package com.github.zhangfuxing.lambda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link LambdaUtils} 单元测试
 *
 * @author 张福兴
 * @date 2026/11/20
 */
public class LambdaUtilsTest {

	@Test
	public void testMethodName() {
		User user = new User("Tom", 18);
		assertEquals("getName", LambdaUtils.getMethodName(User::getName));
		assertEquals("getName", LambdaUtils.getMethodName(user::getName));
		assertEquals("setName", LambdaUtils.getMethodName(User::setName));
		assertEquals("setName", LambdaUtils.getMethodName(user::setName));
		assertEquals("max", LambdaUtils.getMethodName((LambdaUtils.TwoArg<Integer, Integer>) Math::max));
		assertEquals("<init>", LambdaUtils.getMethodName(User::new));
	}

	@Test
	public void testFieldName() {
		User user = new User("Tom", 18);
		assertEquals("name", LambdaUtils.getFieldName(User::getName));
		assertEquals("name", LambdaUtils.getFieldName(user::getName));
		assertEquals("name", LambdaUtils.getFieldName(User::setName));
		assertEquals("name", LambdaUtils.getFieldName(user::setName));
		assertEquals("active", LambdaUtils.getFieldName(User::isActive));
		assertEquals("URL", LambdaUtils.getFieldName(User::getURL));
		assertEquals("name", LambdaUtils.getFieldName(User::name));
	}

	@Test
	public void testInvalidLambda() {
		User user = new User("Tom", 18);
		assertThrows(IllegalArgumentException.class,
				() -> LambdaUtils.getMethodName((LambdaUtils.NoArg) () -> user.getName()));
		assertThrows(IllegalArgumentException.class,
				() -> LambdaUtils.getMethodName((LambdaUtils.NoArg) null));
	}

	public static class User {
		private String name;
		private int age;
		private boolean active;
		private String URL;

		public User(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isActive() {
			return active;
		}

		public String getURL() {
			return URL;
		}

		public String name() {
			return name;
		}
	}
}
