# common-utils

日常开发中常用的 Java 工具类集合，目前包含字符串格式化、Map 构建与转换能力。

## 特性

- **三种占位符风格**：顺序 `{}`、索引 `{0}`、命名 `{name}`，满足不同场景
- **支持转义**：`\{` 输出字面量 `{`，不会误伤 Windows 路径
- **容错优先**：参数不够或索引越界时，占位符原样保留，不抛异常（适合日志、模板场景）
- **null 可控**：参数为 `null` 时输出 `"null"` 还是空字符串，由实例配置决定

## 环境要求

- 源码仅使用 Java 8 语法（lambda / 方法引用），本仓库在 JDK 17 + Maven 3.9 下构建验证通过

## 快速开始

```bash
git clone git@github.com:zhang-fuxing/common-utils.git
cd common-utils
mvn clean install
```

```xml
<dependency>
    <groupId>com.github.zhang-fuxing</groupId>
    <artifactId>common-utils</artifactId>
    <version>0.0.1</version>
</dependency>
```

> 尚未发布到公共仓库，需先执行 `mvn install` 安装到本地仓库。

## StringFormat 字符串格式化

### 顺序占位符 `{}`

```java
StringFormat sf = new StringFormat();

sf.format("hello, {}!", "world");            // hello, world!
sf.format("a={}, b={}", 1, 2);               // a=1, b=2

// 参数不足：多出来的占位符原样保留
sf.format("a={}, b={}", 1);                  // a=1, b={}

// 参数多余：多余的参数被忽略
sf.format("a={}", 1, 2, 3);                  // a=1
```

### 索引占位符 `{0}`

索引从 `0` 开始，可以重复引用、顺序任意：

```java
sf.formatByIndex("hello, {0}!", "world");    // hello, world!
sf.formatByIndex("b={1}, a={0}", 1, 2);      // b=2, a=1
sf.formatByIndex("{0}-{0}-{0}", "x");        // x-x-x

// 越界或非法形式：原样保留
sf.formatByIndex("a={0}, b={1}", 1);         // a=1, b={1}
sf.formatByIndex("a={01}, b={}", 1);         // a={01}, b={}
```

### 命名占位符 `{name}`

参数按 **key、value 成对**传入（个数为奇数会抛 `IllegalArgumentException`）：

```java
sf.formatByNamed("hello, {name}!", "name", "world");          // hello, world!
sf.formatByNamed("{name} {age}", "name", "zhang", "age", 18);  // zhang 18
sf.formatByNamed("{name}-{name}", "name", "zhang");            // zhang-zhang

// 未提供的 key：原样保留
sf.formatByNamed("hello, {name}!", "other", "x");              // hello, {name}!
```

key 允许字母、数字、下划线、`$`、点、中划线；`{}`、`{na me}` 这类非法形式按普通文本处理。

### 转义

| 写法 | 输出 | 说明 |
|---|---|---|
| `\{` | `{` | 转义占位符起始符 |
| `\\` | `\` | 转义反斜杠本身 |
| 其它位置的 `\` | 原样保留 | 保护 `C:\Users\admin` 这类路径 |

```java
sf.format("hello, \\{}!", "world");          // hello, {}!
sf.format("a\\\\{}b", 1);                    // a\1b
sf.format("C:\\Users\\admin");               // C:\Users\admin
```

> 注意：`\{` 始终被当作转义，所以路径后紧跟占位符时要写 `\\{name}` 才能替换。

### null 值处理

默认输出 `"null"`，与 `String.valueOf` 一致；需要输出空字符串时：

```java
new StringFormat("").format("hello, {}!", (Object) null);   // hello, !

sf.setNullValue("N/A");
sf.format("hello, {}!", (Object) null);                     // hello, N/A!
```

`setNullValue(null)` 等价于输出空字符串。

## FormatUtils 静态门面

不想 new 实例时可以直接调用静态方法：

```java
FormatUtils.formatString("hello, {}!", "world");
FormatUtils.formatStringByIndex("hello, {0}!", "world");
FormatUtils.formatStringByNamed("hello, {name}!", "name", "world");
```

## MapUtils Map 工具

```java
// key、value 成对构建 Map（个数为奇数抛 IllegalArgumentException）
Map<Object, Object> map = MapUtils.mapOf("name", "zhang", "age", 18);

// 键值统一转换
Map<String, String> result = MapUtils.convert(map, String::valueOf, String::valueOf);
```

## 构建与测试

```bash
mvn test          # 运行单元测试
mvn clean package # 打包
```

## 目录结构

```
src/main/java/com/github/zhangfuxing/
├── string/StringFormat.java   # 字符串格式化
└── utils/
    ├── FormatUtils.java       # StringFormat 静态门面
    └── MapUtils.java          # Map 构建与转换
```

## 许可证

暂未指定，补充 `LICENSE` 文件后更新。
