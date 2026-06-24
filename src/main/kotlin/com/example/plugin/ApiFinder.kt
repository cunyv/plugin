package com.example.plugin

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.JavaPsiFacade

object ApiFinder {

    // Controller 缓存，避免每次搜索都重新扫描
    private var cachedControllers: Set<PsiClass>? = null
    private var cachedProject: Project? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_VALIDITY_MS = 60_000 // 缓存有效期 1 分钟

    /**
     * 返回匹配输入路径的接口描述列表
     */
    fun findMatches(project: Project, inputPath: String): List<Pair<String, PsiMethod>> {
        val matches = mutableListOf<Pair<String, PsiMethod>>()
        val controllers = findControllers(project)

        for (psiClass in controllers) {
            val classPath = AnnotationUtils.getClassMapping(psiClass) ?: ""

            for (method in psiClass.methods) {
                val methodPaths = AnnotationUtils.getMethodMappings(method)
                val httpMethod = AnnotationUtils.getHttpMethod(method)
                for (methodPath in methodPaths) {
                    val fullPath = combinePaths(classPath, methodPath)
                    if (matchPath(fullPath, inputPath)) {
                        val label = "[$httpMethod] ${psiClass.qualifiedName}.${method.name} → $fullPath"
                        matches.add(label to method)
                    }
                }
            }
        }

        return matches
    }

    /**
     * 清除缓存，当项目结构变化时调用
     */
    fun clearCache() {
        cachedControllers = null
        cachedProject = null
    }

    private fun matchPath(requestPath: String, inputPath: String): Boolean {
        if (requestPath.isBlank()) return false

        val normalizedRequest = normalizePath(requestPath) ?: return false
        val normalizedInput = normalizePath(inputPath) ?: return false

        // 精确匹配：将路径变量转换为正则
        val regex = normalizedRequest
            .replace(Regex("\\{[^/]+}"), "[^/]+")
            .replace(Regex("/+"), "/")

        // 1. 尝试精确匹配
        if (Regex("^$regex$").matches(normalizedInput)) {
            return true
        }

        // 2. 模糊匹配：输入路径的任意部分匹配
        // 例如：输入 user/list 可以匹配 /system/user/list
        //        输入 test 可以匹配 /test/testApi
        val inputParts = normalizedInput.split("/").filter { it.isNotEmpty() }
        val requestParts = normalizedRequest.split("/").filter { it.isNotEmpty() }

        // 如果输入部分比请求部分长，不可能匹配
        if (inputParts.size > requestParts.size) {
            return false
        }

        // 检查输入的每个部分是否在请求路径的对应部分中包含
        // 使用滑动窗口的方式匹配
        for (i in 0..requestParts.size - inputParts.size) {
            var allMatch = true
            for (j in inputParts.indices) {
                val requestPart = requestParts[i + j]
                val inputPart = inputParts[j]
                val isPathVariable = requestPart.matches(Regex("\\{[^/]+}"))

                // 路径变量只能匹配数字，不能匹配字母
                if (isPathVariable) {
                    // 只有当输入部分是纯数字时，才匹配路径变量
                    if (!inputPart.matches(Regex("\\d+"))) {
                        allMatch = false
                        break
                    }
                } else {
                    // 非路径变量需要包含匹配
                    if (!requestPart.contains(inputPart, ignoreCase = true)) {
                        allMatch = false
                        break
                    }
                }
            }
            if (allMatch) return true
        }

        return false
    }

    /**
     * 拼接类路径 + 方法路径，自动处理前后斜杠
     */
    private fun combinePaths(classPath: String?, methodPath: String?): String {
        val cp = normalizePath(classPath)
        val mp = normalizePath(methodPath)

        return when {
            cp.isNullOrEmpty() && mp.isNullOrEmpty() -> "/"
            cp.isNullOrEmpty() -> mp.orEmpty()
            mp.isNullOrEmpty() -> cp.orEmpty()
            else -> "$cp/$mp".replace(Regex("/+"), "/")
        }
    }

    private fun normalizePath(path: String?): String? {
        if (path.isNullOrBlank()) return null

        var result = path.trim()

        // 确保以斜杠开头
        if (!result.startsWith("/")) {
            result = "/$result"
        }

        // 移除末尾的斜杠（除非只有斜杠）
        if (result.length > 1 && result.endsWith("/")) {
            result = result.removeSuffix("/")
        }

        // 合并连续的斜杠
        result = result.replace(Regex("/+"), "/")

        return result
    }

    /**
     * Finds all classes annotated with @Controller or @RestController in the given project.
     * Uses caching to avoid repeated scans.
     */
    private fun findControllers(project: Project): Set<PsiClass> {
        val now = System.currentTimeMillis()
        // 如果缓存有效且未过期，直接返回
        if (cachedProject == project && cachedControllers != null
            && (now - cacheTimestamp) < CACHE_VALIDITY_MS) {
            return cachedControllers!!
        }

        // 使用 allScope 查找注解类（包括依赖库）
        val annotationScope = GlobalSearchScope.allScope(project)
        // 搜索 Controller 类时只在项目范围内
        val controllerScope = GlobalSearchScope.projectScope(project)

        val controllerFqcns = listOf(
            "org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController"
        )
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val controllers = mutableSetOf<PsiClass>()

        for (fqcn in controllerFqcns) {
            val annotationClass = javaPsiFacade.findClass(fqcn, annotationScope) ?: continue
            controllers.addAll(
                AnnotatedElementsSearch.searchPsiClasses(annotationClass, controllerScope).findAll()
            )
        }

        // 更新缓存
        cachedControllers = controllers
        cachedProject = project
        cacheTimestamp = System.currentTimeMillis()

        return controllers
    }
}