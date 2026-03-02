## License

This project is licensed under the MIT License.
# Spring Boot API Navigator

**插件 ID**: `com.example.plugin`  
**版本**: 1.0-SNAPSHOT  
**适用 IDE**: IntelliJ IDEA 2023.1+ (Community 或 Ultimate)  
**JDK**: 17

---

# Spring Boot API Navigator

一个 IntelliJ IDEA 插件，用于在 Spring Boot 项目中通过完整接口路径快速跳转到对应的 Controller 方法。

---

## ✨ 功能特性

### 🔍 精准接口跳转

输入完整接口路径，例如：/system/user/123
插件会自动匹配：/system/user/{id}并跳转到对应方法。

---

### ⚡ 仅扫描 Controller 类（高性能）

插件只扫描：

- `@Controller`
- `@RestController`

不会遍历全部类，大型项目依然流畅。

---

### 🧠 支持多种 Spring 写法

#### 1️⃣ 支持类 + 方法拼接

```java
@RequestMapping("/system/user")
@GetMapping("/list")
```
#### 2️⃣ 支持不带斜杠写法
```java
@RequestMapping("system/user")
@GetMapping("list")
```
#### 3️⃣ 支持路径变量
```java
@GetMapping("/{id}")
```
自动匹配：
/system/user/123
#### 4️⃣ 支持数组 mapping
```java
@GetMapping({"/", "/{id}"})
```
自动解析并匹配。
#### 🎯 多结果弹窗选择
当匹配到多个接口时：
* 自动弹出选择框
* 显示：类名.方法名 → 完整路径
* 选择后跳转
#### 🔄 自动路径标准化
插件自动处理：
* 输入带 / 或不带 /
* 双斜杠 //
* 末尾斜杠
* 路径变量

例如：
```text
system/user/123
/system/user/123
system//user//123
```
### 🚀 使用方式
#### 快捷键
默认快捷键：
* Windows / Linux：Ctrl + Alt + G
* macOS：option + command + G

按下后输入完整路径即可。
### 📦 安装方式
#### 方式一：开发模式运行
```text
./gradlew runIde
```
会启动一个 Sandbox IDEA，插件自动加载。
#### 方式二：构建后安装
构建插件：
```text
./gradlew buildPlugin
```
生成文件位置：
```text
build/distributions/plugin-xxx.zip
```
安装步骤：
1. 打开 IDEA
2. Settings → Plugins
3. ⚙ → Install Plugin from Disk
4. 选择 zip 文件
5. 重启 IDE
### 🛠 技术实现说明
#### 扫描机制
* 使用 AnnotatedElementsSearch
* 只查找带 Controller 注解的类
* 避免扫描全部类名（性能优化）
#### 路径匹配机制
1. 拼接 class + method mapping
2. 自动标准化路径
3. 将 {id} 转换为正则 [^/]+
4. 精准全路径匹配
### 📁 项目结构
```text
plugin/
├── ApiFinder.kt            # Controller 扫描与匹配逻辑
├── ApiNavigatorAction.kt   # 快捷键入口与弹窗选择
├── AnnotationUtils.kt      # 注解解析工具
└── META-INF/plugin.xml     # 插件配置
```
### 🔧 开发环境
* JDK 17
* IntelliJ IDEA 2023.1+
* Gradle IntelliJ Plugin
### 📈 当前版本能力
* ✔ 只扫描 Controller
* ✔ 支持不带斜杠
* ✔ 支持路径变量
* ✔ 支持数组 mapping
* ✔ 多结果选择弹窗
* ✔ 性能优化
* ✔ 路径自动规范化
### 🚀 后续可扩展方向
* 支持 HTTP Method 过滤（GET / POST）
* 支持模糊搜索
* 支持缓存 Controller 提升速度
* 支持 Swagger 注解识别
* 支持 Kotlin Controller
### 📄 License
