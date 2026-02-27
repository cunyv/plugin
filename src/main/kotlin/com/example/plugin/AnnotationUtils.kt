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

        val value = annotation.findAttributeValue("value")
            ?: annotation.findAttributeValue("path")
            ?: return null

        return value.text.trim('"')
    }
}