package com.github.diekautz.ideplugin.toolWindow

import com.github.diekautz.ideplugin.actions.ScanLSLStreamsAction
import com.github.diekautz.ideplugin.services.MyLSLService
import com.github.diekautz.ideplugin.services.MyMousePositionService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable


class MyToolWindowFactory : ToolWindowFactory {

    private val contentFactory = ContentFactory.SERVICE.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)

        arrayOf(
            contentFactory.createContent(myToolWindow.connectTabContent(), "Connect", false),
                    contentFactory.createContent(myToolWindow.inletTabContent(), "Inlet", false),
        ).forEach(toolWindow.contentManager::addContent)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val project = toolWindow.project
        private val dataModel = Model()
        fun connectTabContent() = panel {
            val mousePositionService = project.service<MyMousePositionService>()
            row {
                button("Record Mouse") {
                    mousePositionService.trackMouse()
                }
            }
        }

        fun inletTabContent() = panel {
            val lslService = project.service<MyLSLService>()
            val tableModel = lslService.getStreamInfoTableModel()
            group("1: Scan Devices") {
                row {
                    button("Rescan", ScanLSLStreamsAction())
                    text("").bindText({ "Discovered ${lslService.streamInfos.size}" }, {})
                }
            }

            val table = JBTable(tableModel, tableModel.columnModel)
            table.selectionModel.addListSelectionListener {
                thisLogger().info("Selection Event $it")
                dataModel.selectedLSLDeviceRow = table.selectedRow
            }
            group("2: Select Device") {

                row {
                    scrollCell(table)
                        .horizontalAlign(HorizontalAlign.FILL)
                }
                row {
                    text("Test")
                        .bindIntText(dataModel::selectedLSLDeviceRow)
                }
            }
        }
    }

    internal data class Model(
        var selectedLSLDeviceRow: Int = -1
    )
}
