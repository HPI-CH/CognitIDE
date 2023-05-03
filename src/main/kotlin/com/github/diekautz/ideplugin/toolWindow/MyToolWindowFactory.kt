package com.github.diekautz.ideplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.diekautz.ideplugin.MyBundle
import com.github.diekautz.ideplugin.actions.ScanLSLStreamsAction
import com.github.diekautz.ideplugin.services.MyProjectService
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.JBTable


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    private val contentFactory = ContentFactory.SERVICE.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)

        val content = contentFactory.createContent(myToolWindow.getConectTabContent(), MyBundle.message("connect"), false)
        toolWindow.contentManager.addContent(content)
        val testContent = contentFactory.createContent(myToolWindow.getTestTabContent(), "Test", false)
        toolWindow.contentManager.addContent(testContent)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()
        private val project = toolWindow.project

        fun getTestTabContent() = panel {
            row {
            }
        }

        fun getConectTabContent() = panel {
                val model = Model()
                val tableModel = service.getStreamInfoTableModel()
                val table = JBTable(tableModel, tableModel.columnModel).apply {
                }
                group("1: Scan Devices") {
                    row {
                        button("Rescan", ScanLSLStreamsAction())
                            .label("Scan for LSL devices")
                        text("").bindText({ "Discovered ${service.streamInfos.size}" }, {})
                    }
                }
                group("2: Select Device") {
                    row {
                        scrollCell(table).apply {

                        }
                    }
                    row {
                        text("").bindText({
                            val streamInfo = service.streamInfos.getOrNull(table.selectedRow)
                            "Selected ${streamInfo?.name() ?: "none"}"
                        }, {})
                    }
                }
            }
    }

    internal data class Model(
        var selectedLSLUid: String? = null
    )
}
