package com.example.plugin

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import java.util.regex.Pattern

object ApiFinder {

    /**
     * 返回匹配输入路径的接口描述列表
     */
    fun findMatches(project: Project, inputPath: String): List<String> {
        val matches = mutableListOf<String>()
        val scope = GlobalSearchScope.projectScope(project)
        val cache = PsiShortNamesCache.getInstance(project)

        // 遍历所有类名
        for (className in cache.allClassNames) {
            val classes: Array<PsiClass> = cache.getClassesByName(className, scope)
            for (psiClass in classes) {
                if (!AnnotationUtils.isController(psiClass)) continue

                val classPath = AnnotationUtils.getClassMapping(psiClass) ?: ""
                for (method in psiClass.methods) {
                    val methodPath = AnnotationUtils.getMethodMapping(method) ?: continue
                    val fullPath = combinePaths(classPath, methodPath)
                    val regexPath = fullPath.replace(Regex("\\{[^/]+}"), "[^/]+") // 支持路径参数

                    if (Pattern.matches(regexPath, inputPath)) {
                        matches.add("${psiClass.qualifiedName}.${method.name} → $fullPath")
                    }
                }
            }
        }
        return matches
    }

    /**
     * 跳转到匹配的接口方法，如果找到多个只跳转第一个
     */
    fun navigate(project: Project, inputPath: String): Boolean {
        val scope = GlobalSearchScope.projectScope(project)
        val cache = PsiShortNamesCache.getInstance(project)

        for (className in cache.allClassNames) {
            val classes: Array<PsiClass> = cache.getClassesByName(className, scope)
            for (psiClass in classes) {
                if (!AnnotationUtils.isController(psiClass)) continue

                val classPath = AnnotationUtils.getClassMapping(psiClass) ?: ""
                for (method in psiClass.methods) {
                    val methodPath = AnnotationUtils.getMethodMapping(method) ?: continue
                    val fullPath = combinePaths(classPath, methodPath)
                    val regexPath = fullPath.replace(Regex("\\{[^/]+}"), "[^/]+") // 支持路径参数

                    if (Pattern.matches(regexPath, inputPath)) {
                        val descriptor = OpenFileDescriptor(
                                project,
                                method.containingFile.virtualFile,
                                method.textOffset
                        )
                        descriptor.navigate(true)
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 拼接类路径 + 方法路径，自动处理前后斜杠
     */
    private fun combinePaths(classPath: String?, methodPath: String?): String {
        val cp = classPath?.trim('/') ?: ""
        val mp = methodPath?.trim('/') ?: ""
        return when {
            cp.isEmpty() && mp.isEmpty() -> "/"
            cp.isEmpty() -> "/$mp"
            mp.isEmpty() -> "/$cp"
            else -> "/$cp/$mp"
        }
    }
}