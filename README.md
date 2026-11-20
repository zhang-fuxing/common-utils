# common-utils

日常开发中常用的 Java 工具类集合，目前包含字符串格式化、Map 构建与转换、数据大小格式化、数字解析、反射、Lambda 名称提取以及控制台进度条能力。

## 特性

- **三种占位符风格**：顺序 `{}`、索引 `{0}`、命名 `{name}`，满足不同场景
- **支持转义**：`\{` 输出字面量 `{`，不会误伤 Windows 路径
- **容错优先**：参数不够或索引越界时，占位符原样保留，不抛异常（适合日志、模板场景）
- **null 可控**：参数为 `null` 时输出 `"null"` 还是空字符串，由实例配置决定
- **数据大小格式化**：字节数转 `1.5 KB` 这类可读文本，支持 1024 / 1000 进制、小数位数与空格配置；也能反向解析 `300MB` 并在各单位间互转
- **数字解析与转换**：把任意对象解析为 Byte / Short / Integer / Long / Float / Double / BigInteger / BigDecimal；`parseXxx` 严格校验，`toXxx` 宽松转换，支持默认值与 null 安全
- **反射工具**：字段读写、方法调用、构造实例；自动处理继承、私有成员、重载与变长参数，并按 JDK 版本选择访问策略
- **Lambda 名称提取**：通过可序列化方法引用获取方法名，并按 JavaBean 规则推导字段名
- **控制台进度条**：内置 9 种文本样式，支持自定义字符、宽度、百分比与附加文本；可原地刷新，并提供进度变化钩子

## 环境要求

- 源码仅使用 Java 8 语法（lambda / 方法引用），`pom.xml` 通过 `maven.compiler.release=8` 约束字节码版本；本仓库在 JDK 17 + Maven 3.9 下构建验证通过
- 测试基于 JUnit 5（Jupiter），通过 `mvn test` 运行

## 快速开始

### 源码构建

```bash
git clone git@github.com:zhang-fuxing/common-utils.git
cd common-utils
mvn clean install
```

### 通过 JitPack 使用

在 `pom.xml` 中添加 JitPack 仓库：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

然后添加依赖：

```xml
<dependency>
    <groupId>com.github.zhang-fuxing</groupId>
    <artifactId>common-utils</artifactId>
    <version>0.0.8</version>
</dependency>
```

Gradle：

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.zhang-fuxing:common-utils:0.0.8'
}
```

> JitPack 会在标签首次被请求时自动构建。构建状态可在
> [https://jitpack.io/#zhang-fuxing/common-utils](https://jitpack.io/#zhang-fuxing/common-utils) 查看；
> 构建完成前依赖可能暂时无法下载。

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

FormatUtils.formatDataSize(1536);               // 1.5 KB
FormatUtils.formatDataSize(1536, 0);            // 2 KB
FormatUtils.parseDataSize("300MB");             // 314572800
FormatUtils.convertDataSize("300MB", "GB");     // 0.29 GB
```

## DataSizeFormat 数据大小格式化

把字节数格式化为可读文本，默认 1024 进制、保留 2 位小数并去掉末尾多余的 0：

```java
DataSizeFormat f = new DataSizeFormat();

f.format(0);                 // 0 B
f.format(1023);              // 1023 B
f.format(1536);              // 1.5 KB
f.format(1234567);           // 1.18 MB
f.format(-1536);             // -1.5 KB
f.format(Long.MAX_VALUE);    // 8 EB
```

单位依次为 `B`、`KB`、`MB`、`GB`、`TB`、`PB`、`EB`，配置项均可链式调用：

```java
new DataSizeFormat().setScale(0).format(1536);                      // 2 KB     小数位数
new DataSizeFormat().setSpace(false).format(1536);                  // 1.5KB    去掉数值与单位间的空格
new DataSizeFormat().setStripTrailingZeros(false).format(1024);     // 1.00 KB  保留末尾的 0
new DataSizeFormat(DataSizeFormat.DECIMAL_BASE, 2).format(1500);    // 1.5 KB   改用 1000 进制
```

`Utils` 里也提供了默认配置的快捷方法：

```java
Utils.formatDataSize(1536);      // 1.5 KB
Utils.formatDataSize(1536, 0);   // 2 KB
```

### 智能解析与单位换算

用户输入的 `300MB` 这类文本可以直接解析成字节数，也能在任意单位间互转：

```java
DataSizeFormat f = new DataSizeFormat();

f.parse("300MB");                              // 314572800
f.parse("1.5 GB");                             // 1610612736
f.parse("1024");                               // 1024（不带单位按字节）
f.parse("-1.5kb");                             // -1536
f.parse("512字节");                             // 512

f.format(314572800, DataSizeUnit.GB);          // 0.29 GB    强制单位输出
f.convert("300MB", DataSizeUnit.GB);           // 0.29 GB    解析 + 换算
f.convertTo("300MB", DataSizeUnit.GB);         // 0.29296875 换算后的数值
f.toUnit(314572800, DataSizeUnit.MB);          // 300
```

单位写法宽松，忽略大小写：`B`、`KB`、`MB`、`GB`、`TB`、`PB`、`EB` 为标准写法，同时支持单字母（`K`/`M`/`G`）、IEC（`KiB`/`MiB`）和中文（`字节`/`兆字节`）。单位倍数由实例的进制决定，所以 `parse(format(bytes)) == bytes` 恒成立（`toUnit` / `convertTo` 返回精确值，取整只发生在 `format`）。

`Utils` 的快捷方法：

```java
Utils.parseDataSize("300MB");           // 314572800
Utils.convertDataSize("300MB", "GB");   // 0.29 GB
```

## NumberParse 数字解析与转换

把任意对象解析为指定的数字类型，覆盖 8 种常用类型。`NumberParse` 提供两套语义：

- `parseXxx`：严格解析，值必须精确且在目标类型范围内，失败抛 `NumberFormatException`
- `toXxx`：宽松转换，按 `Number.byteValue()` / `intValue()` 等窄化语义处理，允许小数截断和溢出回绕，失败返回 `null`

### 严格解析

每种类型提供三个方法：

| 类型 | 严格解析（失败抛 `NumberFormatException`） | 带默认值 | 失败返回 null |
|---|---|---|---|
| `Byte` | `parseByte(Object)` | `parseByte(Object, Byte)` | `parseByteOrNull(Object)` |
| `Short` | `parseShort(Object)` | `parseShort(Object, Short)` | `parseShortOrNull(Object)` |
| `Integer` | `parseInt(Object)` | `parseInt(Object, Integer)` | `parseIntOrNull(Object)` |
| `Long` | `parseLong(Object)` | `parseLong(Object, Long)` | `parseLongOrNull(Object)` |
| `Float` | `parseFloat(Object)` | `parseFloat(Object, Float)` | `parseFloatOrNull(Object)` |
| `Double` | `parseDouble(Object)` | `parseDouble(Object, Double)` | `parseDoubleOrNull(Object)` |
| `BigInteger` | `parseBigInteger(Object)` | `parseBigInteger(Object, BigInteger)` | `parseBigIntegerOrNull(Object)` |
| `BigDecimal` | `parseBigDecimal(Object)` | `parseBigDecimal(Object, BigDecimal)` | `parseBigDecimalOrNull(Object)` |

```java
NumberParse.parseInt(" 123 ");                       // 123
NumberParse.parseInt(123L);                          // 123
NumberParse.parseInt(new BigDecimal("123.00"));      // 123
NumberParse.parseInt("abc", 0);                      // 0
NumberParse.parseIntOrNull("abc");                   // null

NumberParse.parseLong("1234567890123");              // 1234567890123
NumberParse.parseBigInteger("12345678901234567890"); // 12345678901234567890
NumberParse.parseBigDecimal("123.45");               // 123.45
```

严格解析规则：

- 字符串先去除首尾空白，再交给目标类型的标准解析方法；
- 输入本身是 `Number` 时按数值转换，整型目标会校验“必须是整数”和“必须在目标类型范围内”，例如 `parseInt(new BigDecimal("123.00"))` 返回 `123`，`parseInt(1.5)` 抛异常；
- 其他对象先调用 `toString()` 再按字符串解析；
- `null` 在严格解析中抛 `NumberFormatException`，在带默认值方法中返回默认值，在 `OrNull` 方法中返回 `null`。

### 宽松转换

| 类型 | 方法 | `Number` 输入的转换语义 |
|---|---|---|
| `Byte` | `toByte(Object)` | `byteValue()` |
| `Short` | `toShort(Object)` | `shortValue()` |
| `Integer` | `toInt(Object)` | `intValue()` |
| `Long` | `toLong(Object)` | `longValue()` |
| `Float` | `toFloat(Object)` | `floatValue()` |
| `Double` | `toDouble(Object)` | `doubleValue()` |
| `BigInteger` | `toBigInteger(Object)` | 小数向零截断 |
| `BigDecimal` | `toBigDecimal(Object)` | 小数精确保留；`NaN` / `Infinity` 返回 `null` |

```java
NumberParse.toByte(1.5d);              // 1，截断小数
NumberParse.toByte(300);               // 44，按 byte 回绕
NumberParse.toShort(32768);            // -32768，按 short 回绕
NumberParse.toInt("1.5");              // 1，字符串按 BigDecimal 转换
NumberParse.toInt("abc");              // null
NumberParse.toBigInteger(1.5d);        // 1，向零截断
NumberParse.toBigDecimal(0.1f);        // 0.1
```

宽松转换规则：

- `toXxx` 不抛 `NumberFormatException`，`null`、空白字符串或无法识别的输入返回 `null`；
- `Number` 输入直接使用对应包装类的 `xxxValue()`，因此小数会向零截断，定长整型溢出会回绕；
- 其他对象先转字符串，整型目标按 `BigDecimal` 解析后转换，`Float` / `Double` 按 `Float.parseFloat` / `Double.parseDouble` 规则解析；
- `toBigDecimal` 对 `Float` / `Double` 使用十进制字符串构造，避免二进制浮点误差；`NaN` / `Infinity` 返回 `null`。

## ReflectUtils 反射工具

`ReflectUtils` 封装字段读写、方法调用和构造实例，支持私有成员、父类成员、重载方法和变长参数。

```java
User user = new User("张三", 18);

Object name = ReflectUtils.getFieldValue(user, "name");
ReflectUtils.setFieldValue(user, "name", "李四");

String greet = (String) ReflectUtils.invokeMethod(user, "greet", "Hello");
int max = (int) ReflectUtils.invokeStaticMethod(Math.class, "max", 1, 2);
User copy = ReflectUtils.newInstance(User.class, "王五", 20);
```

| 方法 | 说明 |
|---|---|
| `getFieldValue(Object, String)` / `setFieldValue(Object, String, Object)` | 读写实例字段，支持私有字段和父类字段 |
| `getStaticFieldValue(Class, String)` / `setStaticFieldValue(Class, String, Object)` | 读写静态字段 |
| `invokeMethod(Object, String, Object...)` | 调用实例方法，自动匹配重载和变长参数 |
| `invokeStaticMethod(Class, String, Object...)` | 调用静态方法 |
| `newInstance(Class, Object...)` | 调用构造方法创建实例，支持私有构造方法 |
| `getField` / `getMethod` / `getConstructor` | 按名称和参数类型查找成员 |
| `makeAccessible(AccessibleObject)` | 尝试开启反射访问，返回是否成功 |

### JDK 版本适配

- JDK 8：使用 `setAccessible(true)`；
- JDK 9+：使用 `trySetAccessible()`，遵守模块访问控制；
- 目标包未对当前模块开放时，`makeAccessible` 返回 `false`，字段或方法操作抛出带说明的 `IllegalStateException`；
- 工具不会使用 `Unsafe` 强行绕过模块限制。确实需要访问 JDK 内部类时，应由使用方通过 `--add-opens` 显式授权，或改用目标类的公开 API。

## LambdaUtils 方法名/字段名提取

通过可序列化的方法引用读取真实方法名，并按 JavaBean 命名规则推导字段名。

```java
LambdaUtils.getMethodName(User::getName);      // getName
LambdaUtils.getMethodName(user::getName);      // getName
LambdaUtils.getMethodName(User::setName);      // setName

LambdaUtils.getFieldName(User::getName);       // name
LambdaUtils.getFieldName(user::isActive);      // active
LambdaUtils.getFieldName(User::setName);       // name
LambdaUtils.getFieldName(User::getURL);        // URL
```

| 方法 | 说明 |
|---|---|
| `getMethodName(NoArg)` | 无参数方法引用，如 `user::getName` |
| `getMethodName(OneArg<T>)` | 单参数方法引用，如 `User::getName` |
| `getMethodName(TwoArg<T, U>)` | 双参数方法引用，如 `User::setName` |
| `getFieldName(NoArg)` | 从无参数 getter 推导字段名 |
| `getFieldName(OneArg<T>)` | 从单参数 getter / setter 推导字段名 |
| `getFieldName(TwoArg<T, U>)` | 从双参数 setter 推导字段名 |

`getFieldName` 会去掉 `get`、`is`、`set` 前缀并做首字母小写；没有这些前缀时按 record / fluent 风格原样返回。`getURL` 这类前两个字母大写的名称会保持 `URL`。

> 只支持方法引用，不支持 `() -> user.getName()` 这类 lambda 代码块。代码块会生成 `lambda$...` 合成方法，无法可靠还原真实方法名。

## ProgressBar 控制台进度条

把任务进度输出到控制台或日志，默认样式为实心方块、宽度 30、显示百分比：

```java
ProgressBar bar = ProgressBar.of(100);
for (int i = 0; i < 100; i++) {
    // 处理任务
    bar.advance().print();      // \r 覆盖当前行，不换行
}
bar.finish();                   // 补满进度并换行
```

- `advance()` / `advance(long delta)`：增加进度，超过总量时按总量处理
- `setCurrent(long current)`：直接设置进度，同样会做上限保护
- `render()`：只返回文本，不输出
- `print()` / `println()`：原地刷新 / 输出并换行
- `finish()`：进度置为总量并换行

```java
ProgressBar bar = ProgressBar.of(10);
bar.setCurrent(5).render();     // [███████████████░░░░░░░░░░░░░░░] 50%
```

### 内置样式 ProgressBarStyle

以宽度 10、进度 50% 为例：

| 样式 | 已完成 | 未完成 | 示例 |
|---|---|---|---|
| `BLOCK` | `█` | `░` | `[█████░░░░░] 50%` |
| `SHADE` | `▓` | `▒` | `[▓▓▓▓▓▒▒▒▒▒] 50%` |
| `ASCII` | `#` | `-` | `[#####-----] 50%` |
| `ARROW` | `=` | `-` | `[====>-----] 50%` |
| `SQUARE` | `■` | `□` | `[■■■■■□□□□□] 50%` |
| `DOT` | `●` | `○` | `[●●●●●○○○○○] 50%` |
| `STAR` | `★` | `☆` | `[★★★★★☆☆☆☆☆] 50%` |
| `LINE` | `━` | `─` | `━━━━━───── 50%` |
| `PERCENT` | - | - | `50%` |

样式通过构造参数或 `setStyle` 指定，默认 `BLOCK`：

```java
ProgressBar bar = ProgressBar.of(100, ProgressBarStyle.ARROW);
bar.setStyle(ProgressBarStyle.DOT);   // 运行时切换
```

`ARROW` 的 `>` 会占用已完成部分的最后一格；进度为 0 或 100% 时不显示箭头。

### 自定义样式

所有样式项都可以链式覆盖，未设置的项沿用当前样式：

```java
ProgressBar bar = ProgressBar.of(100)
        .setWidth(10)
        .setCompleteChar("★")
        .setIncompleteChar("☆")
        .setHeadChar(">")
        .setLeftBracket("|")
        .setRightBracket("|")
        .setShowPercent(false)
        .setShowCount(true)
        .setSuffix("处理中");

bar.setCurrent(50).render();    // |★★★★>☆☆☆☆☆| (50/100) 处理中
```

### 进度钩子

`onChange` 注册回调，进度值变化时会收到 `ProgressEvent`：

```java
ProgressBar bar = ProgressBar.of(100);
bar.onChange(event -> {
    log.info("{}", event.getText());                    // 渲染后的进度条文本
    progressBarComponent.setValue(event.getPercent());  // 同步到界面
});
```

`ProgressEvent` 提供 `getCurrent()`、`getTotal()`、`getRemaining()`、`getPercent()`、`getText()` 与 `isComplete()`。
只有进度值真正变化时才回调；`onChange(null)` 可取消钩子。

### 快捷工具类 ProgressBarUtils

一次性输出无需创建实例，静态门面默认使用 `BLOCK` 样式、宽度 30：

```java
ProgressBarUtils.render(5, 10);                          // [███████████████░░░░░░░░░░░░░░░] 50%
ProgressBarUtils.render(5, 10, ProgressBarStyle.ASCII);  // [###############---------------] 50%
ProgressBarUtils.print(5, 10);                           // 原地输出，不换行
ProgressBarUtils.println(5, 10);                         // 输出并换行

ProgressBar bar = ProgressBarUtils.track(10, ProgressBarStyle.DOT);
```

需要自定义样式、钩子或输出流时，用 `track` 拿到 `ProgressBar` 实例再配置。
`ProgressBar` 的更新、渲染、输出方法均为同步方法，同一个实例可在多线程中共享；输出先后顺序取决于调用顺序。

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
├── number/
│   ├── DataSizeFormat.java    # 数据大小格式化与解析
│   ├── DataSizeUnit.java      # 数据大小单位与换算
│   └── NumberParse.java       # 任意对象转数字类型（严格解析 / 宽松转换）
├── lambda/LambdaUtils.java    # 方法引用提取方法名 / 字段名
├── progress/
│   ├── ProgressBar.java       # 控制台文本进度条
│   ├── ProgressBarStyle.java  # 内置进度条样式
│   ├── ProgressEvent.java     # 进度快照
│   └── ProgressListener.java  # 进度变化钩子
├── reflect/ReflectUtils.java  # 反射工具：字段、方法、构造与 JDK 访问适配
├── string/StringFormat.java   # 字符串格式化
└── utils/
    ├── FormatUtils.java       # StringFormat 静态门面
    ├── MapUtils.java          # Map 构建与转换
    ├── ProgressBarUtils.java  # ProgressBar 静态门面
    └── Utils.java             # 通用工具：空判断、集合转 Map、日期转换、数据大小、安全取值、遍历
```

## 许可证

暂未指定，补充 `LICENSE` 文件后更新。
