package top.allhere.apinavigator

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

        // 显示输入对话框，支持历史记录
        val input = showInputDialogWithHistory(project) ?: return

        val matches = ApiFinder.findMatches(project, input)

        if (matches.isEmpty()) {
            Messages.showInfoMessage("未找到匹配接口", "提示")
            return
        }

        // 保存到历史记录
        HistoryManager.getInstance().addHistory(input)

        if (matches.size == 1) {
            navigateTo(project, matches.first().second)
            return
        }

        showSelectionPopup(project, matches)
    }

    /**
     * 显示带历史记录的输入对话框
     */
    private fun showInputDialogWithHistory(project: Project): String? {
        val historyManager = HistoryManager.getInstance()
        val history = historyManager.getHistory()

        // 如果有历史记录，显示选择弹窗
        if (history.isNotEmpty()) {
            val options = mutableListOf<String>()
            options.add("📝 输入新路径...")
            options.addAll(history)

            val choice = Messages.showEditableChooseDialog(
                "请选择或输入接口路径：",
                "跳转到接口方法",
                Messages.getQuestionIcon(),
                options.toTypedArray(),
                options[0],
                null
            )

            if (choice == null) return null

            // 如果选择了"输入新路径"，弹出输入框
            if (choice == "📝 输入新路径...") {
                return Messages.showInputDialog(
                    project,
                    "请输入接口路径（支持模糊搜索，如 user/list）：",
                    "跳转到接口方法",
                    Messages.getQuestionIcon()
                )
            }

            return choice
        }

        // 没有历史记录，直接显示输入框
        return Messages.showInputDialog(
            project,
            "请输入接口路径（支持模糊搜索，如 user/list）：",
            "跳转到接口方法",
            Messages.getQuestionIcon()
        )
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
