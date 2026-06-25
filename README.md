# Spring Boot API Navigator

**插件 ID**: `top.allhere.apinavigator`
**版本**: 2.0.1
**适用 IDE**: IntelliJ IDEA 2023.1+ (Community 或 Ultimate)
**JDK**: 17

---

一个 IntelliJ IDEA 插件，用于在 Spring Boot 项目中通过接口路径快速跳转到对应的 Controller 方法。

---

## ✨ 功能特性

### 🔍 精准接口跳转

输入接口路径，例如：`/system/user/123` 或 `system/user/123`
插件会自动匹配：`/system/user/{id}` 并跳转到对应方法。

### 🔎 模糊搜索

支持不完整路径匹配：
- 输入 `user/list` 可以匹配 `/system/user/list`
- 输入 `testApi` 可以匹配 `/test/testApi`
- 支持路径变量匹配：输入 `user/123` 可以匹配 `/user/{id}`

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

自动匹配：`/system/user/123`

#### 4️⃣ 支持数组 mapping

```java
@GetMapping({"/", "/{id}"})
```

自动解析并匹配。

---

### 🎯 Search Everywhere 风格弹窗

现代化的无边框搜索体验：
- 弹窗打开时为空，输入后实时过滤显示结果
- 支持实时搜索过滤（输入即过滤）
- HTTP 方法用不同颜色标注：
  - 🟢 **GET** - 绿色
  - 🔵 **POST** - 蓝色
  - 🟠 **PUT** - 橙色
  - 🔴 **DELETE** - 红色
- 显示格式：`[GET] /api/users/{id}  UserController.getUserById`
- 支持键盘上下导航、回车跳转、Esc 关闭
- 支持鼠标双击跳转
- 弹窗可移动、可调整大小
- 点击外部自动关闭

### 📝 搜索历史记录

自动保存搜索历史，支持快速选择：
- 最多保存 20 条历史记录
- 持久化存储，重启 IDE 后仍然有效
- 支持从历史记录中快速选择

---

### 🔄 自动路径标准化

插件自动处理：
- 输入带 `/` 或不带 `/`
- 双斜杠 `//`
- 末尾斜杠
- 路径变量

例如，以下输入都能正确匹配：
```text
system/user/123
/system/user/123
system//user//123
/system/user/123/
```

---

## 🚀 使用方式

### 快捷键

默认快捷键：
- Windows / Linux：`Ctrl + Alt + G`
- macOS：`Option + Command + G`

按下后输入接口路径即可（支持不带斜杠）。

### 菜单入口

Tools → 跳转到接口方法

---

## 📦 安装方式

### 方式一：开发模式运行

```bash
./gradlew runIde
```

会启动一个 Sandbox IDEA，插件自动加载。

### 方式二：构建后安装

构建插件：

```bash
./gradlew buildPlugin
```

生成文件位置：

```text
build/distributions/api-navigator-xxx.zip
```

安装步骤：
1. 打开 IDEA
2. Settings → Plugins
3. ⚙ → Install Plugin from Disk
4. 选择 zip 文件
5. 重启 IDE

---

## 🛠 技术实现说明

### 扫描机制

- 使用 `AnnotatedElementsSearch`
- 只查找带 Controller 注解的类
- 避免扫描全部类名（性能优化）
- 支持 Controller 缓存，提升重复搜索速度

### 路径匹配机制

1. 拼接 class + method mapping
2. 自动标准化路径（处理斜杠）
3. 将 `{id}` 转换为正则 `[^/]+`
4. 精准全路径匹配

---

## 📁 项目结构

```text
api-navigator/
├── src/main/kotlin/top/allhere/apinavigator/
│   ├── ApiFinder.kt            # Controller 扫描与匹配逻辑
│   ├── ApiNavigatorAction.kt   # 快捷键入口与弹窗选择
│   ├── ApiMatchResult.kt       # API 匹配结果数据类
│   ├── ApiListCellRenderer.kt  # 自定义列表渲染器（颜色标注）
│   ├── AnnotationUtils.kt      # 注解解析工具
│   └── HistoryManager.kt       # 搜索历史记录管理
└── src/main/resources/META-INF/
    └── plugin.xml               # 插件配置
```

---

## 🔧 开发环境

- JDK 17
- IntelliJ IDEA 2023.1+
- Gradle IntelliJ Plugin

---

## 📈 当前版本能力

- ✔ 只扫描 Controller
- ✔ 支持不带斜杠的路径
- ✔ 支持路径变量
- ✔ 支持数组 mapping
- ✔ Search Everywhere 风格无边框弹窗
- ✔ 实时搜索过滤（输入后显示结果）
- ✔ HTTP 方法颜色标注（GET/POST/PUT/DELETE）
- ✔ Controller 缓存优化
- ✔ 路径自动规范化
- ✔ 模糊搜索
- ✔ 搜索历史记录
- ✔ 键盘导航（上下箭头、回车跳转、Esc 关闭）
- ✔ 鼠标双击跳转

---

## 🚀 后续可扩展方向

- 支持 HTTP Method 过滤（只搜索 GET/POST）
- 支持 Swagger/OpenAPI 注解识别
- 支持 Kotlin Controller

---

## 📄 License

This project is licensed under the MIT License.
