package top.allhere.apinavigator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ApiNavigatorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val allApis = ApiFinder.findAll(project)

        if (allApis.isEmpty()) {
            com.intellij.openapi.ui.Messages.showInfoMessage("未找到任何接口", "提示")
            return
        }

        showSearchPopup(project, allApis)
    }

    private fun showSearchPopup(project: Project, allApis: List<ApiMatchResult>) {
        val dialog = ApiSearchDialog(project, allApis)
        dialog.show()
    }

    private class ApiSearchDialog(
        private val project: Project,
        private val allApis: List<ApiMatchResult>
    ) : DialogWrapper(project, true) {

        private lateinit var searchField: JBTextField
        private lateinit var list: JBList<ApiMatchResult>
        private lateinit var listModel: DefaultListModel<ApiMatchResult>

        init {
            title = "API Navigator"
            setSize(650, 450)
            isModal = false
            init()
        }

        override fun createCenterPanel(): JComponent {
            val panel = JPanel(BorderLayout())

            // 列表模型 - 初始为空
            listModel = DefaultListModel()
            list = JBList(listModel)
            list.cellRenderer = ApiListCellRenderer()
            list.selectionMode = ListSelectionModel.SINGLE_SELECTION
            list.emptyText.text = "输入路径搜索接口..."

            // 鼠标双击跳转
            list.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        navigateToSelected()
                    }
                }
            })

            // 搜索框
            searchField = JBTextField()
            searchField.font = searchField.font.deriveFont(14f)
            searchField.border = JBUI.Borders.empty(8)

            // 搜索过滤
            searchField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) = filterList()
                override fun removeUpdate(e: DocumentEvent) = filterList()
                override fun changedUpdate(e: DocumentEvent) = filterList()
            })

            // 键盘导航
            searchField.addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_DOWN -> {
                            e.consume()
                            val index = list.selectedIndex
                            if (index < listModel.size - 1) {
                                list.selectedIndex = index + 1
                                list.ensureIndexIsVisible(list.selectedIndex)
                            }
                        }
                        KeyEvent.VK_UP -> {
                            e.consume()
                            val index = list.selectedIndex
                            if (index > 0) {
                                list.selectedIndex = index - 1
                                list.ensureIndexIsVisible(list.selectedIndex)
                            }
                        }
                        KeyEvent.VK_ENTER -> {
                            e.consume()
                            navigateToSelected()
                        }
                    }
                }
            })

            // 布局
            panel.add(searchField, BorderLayout.NORTH)
            panel.add(JBScrollPane(list), BorderLayout.CENTER)
            panel.preferredSize = Dimension(600, 400)

            return panel
        }

        override fun getPreferredFocusedComponent(): JComponent {
            return searchField
        }

        private fun filterList() {
            val searchText = searchField.text.trim().lowercase()
            listModel.clear()
            if (searchText.isNotEmpty()) {
                allApis.filter { api ->
                    api.fullPath.lowercase().contains(searchText) ||
                    api.httpMethod.lowercase().contains(searchText)
                }.forEach { listModel.addElement(it) }
            }
            if (listModel.size > 0) {
                list.selectedIndex = 0
            }
        }

        private fun navigateToSelected() {
            val selected = list.selectedValue
            if (selected != null) {
                navigateTo(project, selected.psiMethod)
                HistoryManager.getInstance().addHistory(selected.fullPath)
                close(DialogWrapper.OK_EXIT_CODE)
            }
        }
    }

    companion object {
        fun navigateTo(project: Project, method: com.intellij.psi.PsiMethod) {
            val descriptor = OpenFileDescriptor(
                project,
                method.containingFile.virtualFile,
                method.textOffset
            )
            descriptor.navigate(true)
        }
    }
}
