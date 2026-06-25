package top.allhere.apinavigator

import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

/**
 * API 列表自定义渲染器
 * 用不同颜色标注 HTTP 方法类型
 */
class ApiListCellRenderer : ColoredListCellRenderer<ApiMatchResult>() {

    companion object {
        // HTTP 方法颜色配置
        private val GET_COLOR = JBColor(0x2E7D32, 0x66BB6A)       // 绿色
        private val POST_COLOR = JBColor(0x1565C0, 0x42A5F5)      // 蓝色
        private val PUT_COLOR = JBColor(0xEF6C00, 0xFFA726)       // 橙色
        private val DELETE_COLOR = JBColor(0xC62828, 0xEF5350)     // 红色
        private val REQUEST_COLOR = JBColor(0x616161, 0x9E9E9E)   // 灰色
        private val UNKNOWN_COLOR = JBColor(0x616161, 0x9E9E9E)   // 灰色
    }

    override fun customizeCellRenderer(
        list: JList<out ApiMatchResult>,
        value: ApiMatchResult,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        // HTTP 方法标签 [GET]
        val methodColor = getHttpMethodColor(value.httpMethod)
        val methodStyle = SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, methodColor)
        append("[${value.httpMethod}]", methodStyle)

        // 路径
        append(" ${value.fullPath}", SimpleTextAttributes.REGULAR_ATTRIBUTES)

        // 详情（类名.方法名）
        append("  ${value.toDetailLabel()}", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES)
    }

    /**
     * 获取 HTTP 方法颜色
     */
    private fun getHttpMethodColor(httpMethod: String): java.awt.Color {
        return when (httpMethod.uppercase()) {
            "GET" -> GET_COLOR
            "POST" -> POST_COLOR
            "PUT" -> PUT_COLOR
            "DELETE" -> DELETE_COLOR
            "REQUEST" -> REQUEST_COLOR
            else -> UNKNOWN_COLOR
        }
    }
}
