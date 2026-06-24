package com.example.plugin

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod

object AnnotationUtils {

    // 注解全限定名到 HTTP 方法类型的映射
    private val HTTP_METHOD_MAP = mapOf(
        "org.springframework.web.bind.annotation.GetMapping" to "GET",
        "org.springframework.web.bind.annotation.PostMapping" to "POST",
        "org.springframework.web.bind.annotation.PutMapping" to "PUT",
        "org.springframework.web.bind.annotation.DeleteMapping" to "DELETE",
        "org.springframework.web.bind.annotation.RequestMapping" to "REQUEST"
    )

    fun getClassMapping(psiClass: PsiClass): String? {
        return getPath(psiClass.getAnnotation(
            "org.springframework.web.bind.annotation.RequestMapping"))
    }

    /**
     * 获取方法上所有 Mapping 注解的路径列表
     * 支持单个路径和数组路径
     */
    fun getMethodMappings(method: PsiMethod): List<String> {
        val annotations = listOf(
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.RequestMapping"
        )

        for (annotationFqn in annotations) {
            val ann = method.getAnnotation(annotationFqn) ?: continue
            val paths = getPaths(ann)
            if (paths.isNotEmpty()) return paths
        }
        return emptyList()
    }

    /**
     * 获取方法的 HTTP 方法类型（GET/POST/PUT/DELETE/REQUEST）
     */
    fun getHttpMethod(method: PsiMethod): String {
        for ((annotationFqn, httpMethod) in HTTP_METHOD_MAP) {
            if (method.getAnnotation(annotationFqn) != null) {
                return httpMethod
            }
        }
        return "UNKNOWN"
    }

    private fun getPath(annotation: PsiAnnotation?): String? {
        val paths = getPaths(annotation)
        return paths.firstOrNull()
    }

    /**
     * 获取注解中的所有路径，支持单个路径和数组路径
     */
    private fun getPaths(annotation: PsiAnnotation?): List<String> {
        if (annotation == null) return emptyList()

        val attr = annotation.findDeclaredAttributeValue("value")
                ?: annotation.findDeclaredAttributeValue("path")
                ?: return emptyList()

        val text = attr.text.trim()

        // 情况1：单字符串
        if (text.startsWith("\"")) {
            return listOf(text.removeSurrounding("\""))
        }

        // 情况2：数组 {"a", "b"}
        if (text.startsWith("{") && text.endsWith("}")) {
            val inside = text.removePrefix("{").removeSuffix("}")
            return inside.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { it.removeSurrounding("\"") }
        }

        return emptyList()
    }
}