package com.example.plugin

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.openapi.project.Project

object AnnotationUtils {

    fun isController(psiClass: PsiClass): Boolean {
        return psiClass.getAnnotation("org.springframework.web.bind.annotation.RestController") != null ||
                psiClass.getAnnotation("org.springframework.stereotype.Controller") != null
    }

    fun getClassMapping(psiClass: PsiClass): String? {
        return getPath(psiClass.getAnnotation(
            "org.springframework.web.bind.annotation.RequestMapping"))
    }

    fun getMethodMapping(method: PsiMethod): String? {
        val annotations = listOf(
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.RequestMapping"
        )

        annotations.forEach {
            val ann = method.getAnnotation(it)
            if (ann != null) return getPath(ann)
        }
        return null
    }

    private fun getPath(annotation: PsiAnnotation?): String? {
        if (annotation == null) return null

        val attr = annotation.findDeclaredAttributeValue("value")
                ?: annotation.findDeclaredAttributeValue("path")
                ?: return null

        val text = attr.text.trim()

        // 情况1：单字符串
        if (text.startsWith("\"")) {
            return text.removeSurrounding("\"")
        }

        // 情况2：数组 {"a", "b"}
        if (text.startsWith("{") && text.endsWith("}")) {
            val inside = text.removePrefix("{").removeSuffix("}")
            val first = inside.split(",")
                    .map { it.trim() }
                    .firstOrNull() ?: return null

            return first.removeSurrounding("\"")
        }

        return null
    }
}