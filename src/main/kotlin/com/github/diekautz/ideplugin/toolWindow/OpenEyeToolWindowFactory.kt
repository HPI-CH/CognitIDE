package com.github.diekautz.ideplugin.toolWindow

import com.github.diekautz.ideplugin.actions.debug.ScanLSLStreamsAction
import com.github.diekautz.ideplugin.services.TobiiProService
import com.github.diekautz.ideplugin.services.debug.LSLService
import com.github.diekautz.ideplugin.services.debug.MousePositionService
import com.github.diekautz.ideplugin.services.recording.LookRecorderService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable


class OpenEyeToolWindowFactory : ToolWindowFactory {

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
            val tobiiProService = project.service<TobiiProService>()
            val lookRecorderService = project.service<LookRecorderService>()
            group("Tobii Pro Nano") {
                row {
                    button("Record") {
                        tobiiProService.startRecording()
                    }
                    button("Stop") {
                        tobiiProService.stopRecording()
                    }
                }
            }
            group("Recording") {
                row {
                    button("Clear") {
                        if (MessageDialogBuilder
                                .okCancel("Clear Recording", "Do you want clear all recorded data?")
                                .ask(project)
                        ) {
                            lookRecorderService.clearData()
                        }
                    }
                    button("Visualize in Editors") {
                        tobiiProService.visualizeInEditor()
                    }
                }
                row {
                    button("Save Gaze Snapshots") {
                        lookRecorderService.askAndSaveGazeSnapshots()
                    }
                    button("Save Element Gaze Points") {
                        lookRecorderService.askAndSaveElementsGazePoints()
                    }
                }
            }
        }

        fun debugTabContent() = panel {
            val lslService = project.service<LSLService>()
            val mousePositionService = project.service<MousePositionService>()
            val tableModel = lslService.getStreamInfoTableModel()

            row("Mouse") {
                button("Record") {
                    mousePositionService.trackMouse()
                }
                button("Stop") {
                    mousePositionService.stopTrackMouse()
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
