package com.example.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiMethod

class ApiNavigatorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val input = Messages.showInputDialog(
                project,
                "请输入接口路径（支持不带斜杠，如 test/list）：",
                "跳转到接口方法",
                Messages.getQuestionIcon()
        ) ?: return

        val matches = ApiFinder.findMatches(project, input)

        if (matches.isEmpty()) {
            Messages.showInfoMessage("未找到匹配接口", "提示")
            return
        }

        if (matches.size == 1) {
            navigateTo(project, matches.first().second)
            return
        }

        showSelectionPopup(project, matches)
    }

    private fun showSelectionPopup(
            project: Project,
            matches: List<Pair<String, PsiMethod>>
    ) {
        val items = matches.map { it.first }

        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(items)
                .setTitle("请选择接口")
                .setItemChosenCallback { selected ->
                    val method = matches.first { it.first == selected }.second
                    navigateTo(project, method)
                }
                .createPopup()
                .showInFocusCenter()
    }

    private fun navigateTo(project: Project, method: PsiMethod) {
        val descriptor = OpenFileDescriptor(
                project,
                method.containingFile.virtualFile,
                method.textOffset
        )
        descriptor.navigate(true)
    }
}