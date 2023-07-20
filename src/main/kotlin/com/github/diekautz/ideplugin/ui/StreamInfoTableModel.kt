package com.github.diekautz.ideplugin.ui

import edu.ucsd.sccn.LSL.StreamInfo
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableColumnModel
import javax.swing.table.TableColumn

class StreamInfoTableModel(val data: MutableList<StreamInfo>) : AbstractTableModel() {
    private val columnNames = arrayOf("Name", "Type", "Host")

    val columnModel = DefaultTableColumnModel().apply {
        columnNames.forEachIndexed { index, name ->
            addColumn(TableColumn(index).apply {
                headerValue = name
            })
        }
    }

    override fun getRowCount(): Int = data.size

    override fun getColumnCount(): Int = columnNames.size

    override fun getValueAt(rowIndex: Int, columnIndex: Int): String = when (columnIndex) {
        0 -> data[rowIndex].name()
        1 -> data[rowIndex].type()
        2 -> data[rowIndex].hostname()
        else -> "null"
    }

    override fun getColumnName(column: Int): String = columnNames[column]
}