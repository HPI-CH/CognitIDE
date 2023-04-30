package com.github.diekautz.ideplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.diekautz.ideplugin.MyBundle
import com.github.diekautz.ideplugin.actions.ScanLSLStreamsAction
import com.github.diekautz.ideplugin.services.MyProjectService
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.table.JBTable
import com.intellij.util.lateinitVal
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JScrollPane


class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    private val contentFactory = ContentFactory.SERVICE.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        toolWindow.contentManager.addContent(
            contentFactory.createContent(
                myToolWindow.content,
                "Tab",
                false
            )
        )

        val content = contentFactory.createContent(myToolWindow.getContent(), MyBundle.message("connect"), false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()
            val label = JBLabel(MyBundle.message("randomLabel", "?"))

            add(JBPanel<JBPanel<*>>().apply {
                add(label)
                add(JButton(MyBundle.message("shuffle")).apply {
                    addActionListener {
                        label.text = MyBundle.message("randomLabel", service.getRandomNumber())
                    }
                })
            }, BorderLayout.NORTH)

            val tableModel = service.getStreamInfoTableModel()
            val table = JBTable(tableModel, tableModel.columnModel)
            add(JScrollPane(table), BorderLayout.CENTER)

            add(JButton(MyBundle.message("scan")).apply {
                addActionListener {
                    service.resolveStreams()
                }
            }, BorderLayout.EAST)
        }

        val content: DialogPanel
            get() = panel {
                val model = Model()
                var comboBox by lateinitVal<ComboBox<*>>()
                group("1: Scan Devices") {
                    row {
                        button("Rescan", ScanLSLStreamsAction())
                            .label("Scan for LSL devices")
                            .actionListener { event, component ->
                                comboBox.removeAll()
                            }
                        text("").bindText({ "Discovered ${service.streamInfos.size}" }, {})
                    }
                }
                group("2: Select Device") {
                    row {
                        comboBox(service.streamInfoIds).bindItemNullable(model::selectedLSLUid).apply {
                            comboBox = component
                        }
                        text("").bindText({ "Selected ${model.selectedLSLUid}" }, {})
                    }
                }
            }
    }

    internal data class Model(
        var selectedLSLUid: String? = null
    )
}
