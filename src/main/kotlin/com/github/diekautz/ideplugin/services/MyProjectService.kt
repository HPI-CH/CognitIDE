package com.github.diekautz.ideplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.diekautz.ideplugin.MyBundle
import com.github.diekautz.ideplugin.StreamInfoTableModel
import edu.ucsd.sccn.LSL

@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) {

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    fun getRandomNumber() = (1..100).random()

    private val streamInfoTableModel = StreamInfoTableModel(mutableListOf())
    val streamInfos = mutableListOf<LSL.StreamInfo>()

    val streamInfoIds = streamInfos.map { it.uid() }
    fun getStreamInfoTableModel() = streamInfoTableModel
    fun resolveStreams(): List<LSL.StreamInfo> {
        streamInfos.clear()
        streamInfos.addAll(LSL.resolve_streams())
        thisLogger().info("Resolved ${streamInfos.size} LSL streams.")

        streamInfoTableModel.data.clear()
        streamInfoTableModel.data.addAll(streamInfos)
        streamInfoTableModel.fireTableDataChanged()
        return streamInfos
    }

}
