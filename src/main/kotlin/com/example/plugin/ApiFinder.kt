package com.example.plugin

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiMethod

object ApiFinder {

    /**
     * 返回匹配输入路径的接口描述列表
     */
    fun findMatches(project: Project, inputPath: String): List<Pair<String, PsiMethod>> {
        val matches = mutableListOf<Pair<String, PsiMethod>>()
        val controllers = findControllers(project)

        for (psiClass in controllers) {

            val classPath = AnnotationUtils.getClassMapping(psiClass) ?: ""

            for (method in psiClass.methods) {
                val methodPath = AnnotationUtils.getMethodMapping(method) ?: continue
                val fullPath = combinePaths(classPath, methodPath)

                if (matchPath(fullPath, inputPath)) {
                    val label = "${psiClass.qualifiedName}.${method.name} → $fullPath"
                    matches.add(label to method)
                }
            }
        }

        return matches
    }

//    /**
//     * 跳转到匹配的接口方法，如果找到多个只跳转第一个
//     */
//    fun navigate(project: Project, inputPath: String): Boolean {
//        val scope = GlobalSearchScope.projectScope(project)
//        val cache = PsiShortNamesCache.getInstance(project)
//
//        for (className in cache.allClassNames) {
//            val classes: Array<PsiClass> = cache.getClassesByName(className, scope)
//            for (psiClass in classes) {
//                if (!AnnotationUtils.isController(psiClass)) continue
//
//                val classPath = AnnotationUtils.getClassMapping(psiClass) ?: ""
//                for (method in psiClass.methods) {
//                    val methodPath = AnnotationUtils.getMethodMapping(method) ?: continue
//                    val fullPath = combinePaths(classPath, methodPath)
//                    if (matchPath(fullPath, inputPath)) {
//                        val descriptor = OpenFileDescriptor(
//                                project,
//                                method.containingFile.virtualFile,
//                                method.textOffset
//                        )
//                        descriptor.navigate(true)
//                        return true
//                    }
//                    continue
//                }
//            }
//        }
//        return false
//    }

    private fun matchPath(requestPath: String, inputPath: String): Boolean {
        if (requestPath.isBlank()) return false

        val normalizedRequest = normalizePath(requestPath) ?: return false
        val normalizedInput = normalizePath(inputPath) ?: return false

        val regex = normalizedRequest
            .replace(Regex("\\{[^/]+}"), "[^/]+")
            .replace(Regex("/+"), "/")

        return Regex("^$regex$").matches(normalizedInput)
    }

    /**
     * 拼接类路径 + 方法路径，自动处理前后斜杠
     */
    private fun combinePaths(classPath: String?, methodPath: String?): String {
        val cp = normalizePath(classPath)?.trim('/') ?: ""
        val mp = normalizePath(methodPath)?.trim('/') ?: ""

        return when {
            cp.isEmpty() && mp.isEmpty() -> "/"
            cp.isEmpty() -> "/$mp"
            mp.isEmpty() -> "/$cp"
            else -> "/$cp/$mp"
        }
    }

    private fun normalizePath(path: String?): String? {
        if (path.isNullOrBlank()) return null

        var result = path.trim()

        if (!result.startsWith("/")) {
            result = "/$result"
        }

        result = result.replace(Regex("/+"), "/")

        return result
    }

    private fun findControllers(project: Project): Collection<PsiClass> {
        val scope = GlobalSearchScope.projectScope(project)
        val facade = JavaPsiFacade.getInstance(project)

        val controllers = mutableSetOf<PsiClass>()

        val controllerAnno = facade.findClass(
                "org.springframework.stereotype.Controller",
                scope
        )

        val restControllerAnno = facade.findClass(
                "org.springframework.web.bind.annotation.RestController",
                scope
        )

        controllerAnno?.let {
            controllers.addAll(
                    AnnotatedElementsSearch.searchPsiClasses(it, scope).findAll()
            )
        }

        restControllerAnno?.let {
            controllers.addAll(
                    AnnotatedElementsSearch.searchPsiClasses(it, scope).findAll()
            )
        }

        return controllers
    }
}