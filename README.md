# common-utils

日常开发中常用的 Java 工具类集合，目前包含字符串格式化、Map 构建与转换能力。

## 特性

- **三种占位符风格**：顺序 `{}`、索引 `{0}`、命名 `{name}`，满足不同场景
- **支持转义**：`\{` 输出字面量 `{`，不会误伤 Windows 路径
- **容错优先**：参数不够或索引越界时，占位符原样保留，不抛异常（适合日志、模板场景）
- **null 可控**：参数为 `null` 时输出 `"null"` 还是空字符串，由实例配置决定
- **数据大小格式化**：字节数转 `1.5 KB` 这类可读文本，支持 1024 / 1000 进制、小数位数与空格配置；也能反向解析 `300MB` 并在各单位间互转

## 环境要求

- 源码仅使用 Java 8 语法（lambda / 方法引用），`pom.xml` 通过 `maven.compiler.release=8` 约束字节码版本；本仓库在 JDK 17 + Maven 3.9 下构建验证通过

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
│   └── DataSizeUnit.java      # 数据大小单位与换算
├── string/StringFormat.java   # 字符串格式化
└── utils/
    ├── FormatUtils.java       # StringFormat 静态门面
    ├── MapUtils.java          # Map 构建与转换
    └── Utils.java             # 通用工具：空判断、集合转 Map、日期转换、数据大小、安全取值、遍历
```

## 许可证

暂未指定，补充 `LICENSE` 文件后更新。
