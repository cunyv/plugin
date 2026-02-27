# Spring Boot API Navigator

**插件 ID**: `com.example.plugin`  
**版本**: 1.0-SNAPSHOT  
**适用 IDE**: IntelliJ IDEA 2023.1+ (Community 或 Ultimate)  
**JDK**: 17

---

## 功能介绍

`Spring Boot API Navigator` 是一个 IntelliJ IDEA 插件，用于快速跳转到 Spring Boot Java 项目中的 Controller 接口方法。

主要功能：

- 输入完整接口路径，例如 `/user/update`，直接跳转到对应的 Java Controller 方法
- 支持以下注解：
    - `@RestController`
    - `@Controller`
    - `@RequestMapping`
    - `@GetMapping`
    - `@PostMapping`
    - `@PutMapping`
    - `@DeleteMapping`
- 支持路径参数，例如 `/user/{id}` 可以匹配 `/user/123`
- 快捷键触发 Action（Windows/Linux：`Ctrl+Alt+G`，Mac：`option+command+G`）
- 仅扫描 Java 类（不扫描 Kotlin）

---

## 安装方式

### 1. 使用 Gradle 构建插件

在项目根目录执行：

```bash
./gradlew clean build