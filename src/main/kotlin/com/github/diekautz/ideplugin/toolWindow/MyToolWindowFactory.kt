package com.github.diekautz.ideplugin.toolWindow

import com.github.diekautz.ideplugin.actions.ScanLSLStreamsAction
import com.github.diekautz.ideplugin.services.MyLSLService
import com.github.diekautz.ideplugin.services.MyMousePositionService
import com.github.diekautz.ideplugin.services.MyTobiiProService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable


class MyToolWindowFactory : ToolWindowFactory {

    private val contentFactory = ContentFactory.SERVICE.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)

        arrayOf(
            contentFactory.createContent(myToolWindow.recordTabContent(), "Record", false),
            contentFactory.createContent(myToolWindow.debugTabContent(), "Debug", false),
        ).forEach(toolWindow.contentManager::addContent)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val project = toolWindow.project
        private val dataModel = Model()
        fun recordTabContent() = panel {
            val tobiiProService = project.service<MyTobiiProService>()
            row("Tobii Pro Nano") {
                button("Record") {
                    tobiiProService.startRecording()
                }
                button("Stop") {
                    tobiiProService.stopRecording()
                }
                button("Visualize in Editor") {
                    tobiiProService.visualizeInEditor()
                }
            }
        }

        fun debugTabContent() = panel {
            val lslService = project.service<MyLSLService>()
            val mousePositionService = project.service<MyMousePositionService>()
            val tableModel = lslService.getStreamInfoTableModel()

            row("Mouse") {
                button("Record") {
                    mousePositionService.trackMouse()
                }
                button("Stop") {
                    mousePositionService.stopTrackMouse()
                }
                button("Visualize in Editor") {
                    mousePositionService.visualizeInEditor()
                }
            }
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
