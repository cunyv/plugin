package top.allhere.apinavigator

import com.intellij.psi.PsiMethod

/**
 * API 匹配结果数据类
 */
data class ApiMatchResult(
    val httpMethod: String,    // GET/POST/PUT/DELETE
    val fullPath: String,      // /api/users/{id}
    val className: String,     // com.example.UserController
    val methodName: String,    // getUserById
    val psiMethod: PsiMethod   // 用于导航
) {
    /**
     * 显示标签：[GET] /api/users/{id}
     */
    fun toDisplayLabel(): String {
        return "[$httpMethod] $fullPath"
    }

    /**
     * 详情标签：UserController.getUserById
     */
    fun toDetailLabel(): String {
        val simpleClassName = className.substringAfterLast(".")
        return "$simpleClassName.$methodName"
    }
}
