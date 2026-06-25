package top.allhere.apinavigator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
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
        // 创建列表模型 - 初始为空
        val listModel = DefaultListModel<ApiMatchResult>()
        val list = JBList(listModel)
        list.cellRenderer = ApiListCellRenderer()
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.emptyText.text = "输入路径搜索接口..."

        // 创建搜索输入框
        val searchField = JBTextField()
        searchField.font = searchField.font.deriveFont(14f)
        searchField.border = JBUI.Borders.empty(8)

        // 搜索过滤逻辑
        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = filterList()
            override fun removeUpdate(e: DocumentEvent) = filterList()
            override fun changedUpdate(e: DocumentEvent) = filterList()

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
        })

        // 创建弹窗
        var popup: JBPopup? = null

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
                        val selected = list.selectedValue
                        if (selected != null) {
                            navigateTo(project, selected.psiMethod)
                            HistoryManager.getInstance().addHistory(selected.fullPath)
                            popup?.cancel()
                        }
                    }
                    KeyEvent.VK_ESCAPE -> {
                        e.consume()
                        popup?.cancel()
                    }
                }
            }
        })

        // 鼠标双击跳转
        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val selected = list.selectedValue
                    if (selected != null) {
                        navigateTo(project, selected.psiMethod)
                        HistoryManager.getInstance().addHistory(selected.fullPath)
                        popup?.cancel()
                    }
                }
            }
        })

        // 创建内容面板
        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(searchField, BorderLayout.NORTH)
        contentPanel.add(JBScrollPane(list), BorderLayout.CENTER)

        // 创建弹窗
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(contentPanel, searchField)
            .setRequestFocus(true)
            .setFocusable(true)
            .setMovable(true)
            .setResizable(true)
            .setCancelOnClickOutside(true)
            .setMinSize(Dimension(500, 300))
            .createPopup()

        popup.showInFocusCenter()
    }

    private fun navigateTo(project: Project, method: com.intellij.psi.PsiMethod) {
        val descriptor = OpenFileDescriptor(
            project,
            method.containingFile.virtualFile,
            method.textOffset
        )
        descriptor.navigate(true)
    }
}
