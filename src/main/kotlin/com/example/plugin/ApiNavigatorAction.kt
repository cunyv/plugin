package com.example.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class ApiNavigatorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val inputPath = Messages.showInputDialog(
            project,
            "请输入完整接口路径，例如 /user/update",
            "跳转到接口方法",
            Messages.getQuestionIcon()
        ) ?: return

        val matched = ApiFinder.findMatches(project, inputPath)

        when {
            matched.isEmpty() ->
                Messages.showInfoMessage(project, "未找到接口: $inputPath", "结果")

            matched.size == 1 ->
                ApiFinder.navigate(project, inputPath)

            else ->
                Messages.showInfoMessage(
                    project,
                    "找到多个匹配:\n${matched.joinToString("\n")}",
                    "匹配结果"
                )
        }
    }
}