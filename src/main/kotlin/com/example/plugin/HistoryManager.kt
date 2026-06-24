package com.example.plugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 搜索历史记录管理器
 * 使用 IntelliJ 的 PersistentStateComponent 持久化存储
 */
@State(
    name = "ApiNavigatorHistory",
    storages = [Storage("ApiNavigatorHistory.xml")]
)
class HistoryManager : PersistentStateComponent<HistoryManager.State> {

    data class State(
        var history: MutableList<String> = mutableListOf()
    )

    private var myState = State()

    companion object {
        private const val MAX_HISTORY_SIZE = 20

        fun getInstance(): HistoryManager {
            return ApplicationManager.getApplication().getService(HistoryManager::class.java)
        }
    }

    override fun getState(): State {
        return myState
    }

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    /**
     * 添加搜索记录
     */
    fun addHistory(path: String) {
        val trimmedPath = path.trim()
        if (trimmedPath.isEmpty()) return

        // 移除已存在的相同记录
        myState.history.remove(trimmedPath)

        // 添加到列表开头
        myState.history.add(0, trimmedPath)

        // 限制历史记录数量
        while (myState.history.size > MAX_HISTORY_SIZE) {
            myState.history.removeAt(myState.history.lastIndex)
        }
    }

    /**
     * 获取历史记录列表
     */
    fun getHistory(): List<String> {
        return myState.history.toList()
    }

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        myState.history.clear()
    }
}
