package com.github.hpich.cognitide.config

import com.intellij.json.JsonFileType
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.ui.dsl.builder.*
import java.awt.Color

class CognitIDESettingsConfigurable : BoundConfigurable(
    "CognitIDESettingsConfigurable",
) {
    private val model = CognitIDESettingsState.instance
    private val wrapper = Wrapper()
    private var currentSubPanel: DialogPanel? = null
    private val modifiedDeviceSpecs: MutableList<DeviceSpec> = mutableListOf()

    override fun createPanel(): DialogPanel {
        // Populate wrapper for the first time
        resetModifiedDeviceSpecs()
        updateSpecPanel()

        return panel {
            group("Recordings") {
                row("Recording Save Location:") {
                    textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor())
                        .bindText(model::recordingsSaveLocation)
                        .comment("The location where participant data will be saved.")
                }
            }

            group("Highlighting Configuration") {
                row("Command:") {
                    textField()
                        .bindText(model::highlightingCommand).comment(
                            "This command will be appended with parameters providing the folder of the recording " +
                                "as well as the timestamp that will be highlighted. \n" +
                                "Example: `python \"path/to/highlighting.py\"` will be executed as " +
                                "`python \"path/to/highlighting.py\" \"path/to/recording/folder\" timestamp`. " +
                                "Further information and an example script can be found " +
                                "<a href='https://github.com/HPI-CH/CognitIDE'>here</a>.",
                        )
                }
            }

            group("Study Workflow") {
                row("Study Workflow File:") {
                    textFieldWithBrowseButton(
                        fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(JsonFileType.INSTANCE),
                    ).bindText(model::workflowJsonPath).comment("The workflow JSON which should be executed")
                }
            }

            group("Device Streams") {
                row("Gaze Recording Source: ") {
                    comboBox(
                        mutableListOf("Tobii", "Mouse"),
                    ).bindItem(model::gazeSource).comment("Select which device should be used to record gaze data.")
                }
                separator("", Color.white)
                row {}.cell(wrapper)
                row {
                    // Button to add a new device spec
                    button("Add Stream") {
                        modifiedDeviceSpecs.add(DeviceSpec("Sensor " + (modifiedDeviceSpecs.size + 1), ""))
                        // Refresh the settings panel to show the new spec
                        updateSpecPanel()
                    }
                }
            }
        }
    }

    private fun updateSpecPanel() {
        // Persist the current values in the modifiedDeviceSpecs
        currentSubPanel?.apply()
        // Create new UI elements for all deviceSpecs
        currentSubPanel =
            panel {
                modifiedDeviceSpecs.forEach { deviceSpec -> buildSingleSpec(this, deviceSpec) }
            }
        // Render the new elements
        wrapper.setContent(currentSubPanel)
        wrapper.revalidate()
    }

    override fun isModified(): Boolean {
        return super.isModified() || currentSubPanel?.isModified() == true || modifiedDeviceSpecs != model.devices
    }

    override fun apply() {
        currentSubPanel?.apply()
        storeDeviceSpecs()
        model.devices = modifiedDeviceSpecs.toMutableList()
        super.apply()
    }

    override fun reset() {
        currentSubPanel?.reset()
        resetModifiedDeviceSpecs()
        updateSpecPanel()
        super.reset()
    }

    override fun cancel() {
        resetModifiedDeviceSpecs()
        super.cancel()
    }

    private fun deepCopyList(
        source: MutableList<DeviceSpec>,
        target: MutableList<DeviceSpec>,
    ) {
        target.clear()
        target.addAll(source.map { it.copy() })
    }

    private fun storeDeviceSpecs() {
        deepCopyList(modifiedDeviceSpecs, model.devices)
    }

    private fun resetModifiedDeviceSpecs() {
        deepCopyList(model.devices, modifiedDeviceSpecs)
    }

    private fun buildSingleSpec(
        panel: Panel,
        deviceSpec: DeviceSpec,
    ) {
        with(panel) {
            row("Stream Name:") { textField().bindText(deviceSpec::name) }
            row("Connector application path:") {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                    .bindText(deviceSpec::connectorPath)
                    .comment(
                        "The connector application is used to create an LSL stream for the given device. " +
                            "It will be used by the plugin to get the required data.\n" +
                            "Provide the application path <i>optionally</i> so the application can be opened when needed.",
                    )
            }
            // Button to remove this device spec
            row {
                button("Remove") {
                    modifiedDeviceSpecs.remove(deviceSpec)
                    // Refresh the settings panel to reflect the removal
                    updateSpecPanel()
                }
            }
        }
    }
}
