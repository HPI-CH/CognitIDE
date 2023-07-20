package com.github.diekautz.ideplugin.services.debug

import com.github.diekautz.ideplugin.StreamInfoTableModel
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import edu.ucsd.sccn.LSL

@Service(Service.Level.PROJECT)
class LSLService {
    private val streamInfoTableModel = StreamInfoTableModel(mutableListOf())
    val streamInfos = mutableListOf<LSL.StreamInfo>()

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
