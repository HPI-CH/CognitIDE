package com.github.diekautz.ideplugin.config

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*

class CognitIDESettingsConfigurable : BoundConfigurable(
    "CognitIDESettingsConfigurable"
) {
    private val model = CognitIDESettingsState.instance

    override fun createPanel() = panel {
        group("Recordings") {
            row("Recording Save Location:") {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor())
                    .bindText(model::recordingsSaveLocation)
                    .comment("The location where participant data will be saved.")
            }

            lateinit var interruptCheckBox: Cell<JBCheckBox>
            row {
                interruptCheckBox = checkBox("Interrupt user:")
                    .bindSelected(model::interruptUser)
            }
            panel {
                indent {
                    row("Delay:") {
                        spinner(1..99999)
                            .bindIntValue(model::interruptDelay)
                    }
                    row("Count:") {
                        spinner(1..100)
                            .bindIntValue(model::interruptCount)
                    }
                    row {
                        checkBox("Stop recording after last")
                            .bindSelected(model::interruptStopRecordingAfterLast)
                    }
                }
            }.enabledIf(interruptCheckBox.selected)
        }
        group("External Applications") {
            group("TobiiPro") {
                row("Use devices:"){
                    checkBox("Yes").bindSelected(model::includeTobii)
                        .comment("Should data from devices of this type be recorded?")
                }
                row("Connector path:") {
                    textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                        .bindText(model::tobiiProConnectorExecutable)
                        .comment(
                            "Please <a href='https://labstreaminglayer.readthedocs.io/info/supported_devices.html'>build the TobiiPro Connector</a>. " +
                                    "It is used to create an LSL stream for the eye tracker. " +
                                    "It will be used by the plugin to get the required data.\n" +
                                    "Provide the application path <i>optionally</i> so the application can be opened when needed."
                        )
                }
                group("Eye Tracker Manager") {
                    row("Path:") {
                        textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                            .bindText(model::eyeTrackerManagerExecutable)
                            .comment(
                                "<i>optional:</i> Provide the path to your " +
                                        "<a href='https://www.tobii.com/products/software/applications-and-developer-kits/tobii-pro-eye-tracker-manager'>Tobii Pro Eye Tracker Manager</a>. " +
                                        "It will be called on the fly to setup the calibration of a new participant."
                            )
                    }
                    row("Serial number:") {
                        textField()
                            .bindText(model::eyeTrackerSerial)
                            .comment(
                                "<i>optional:</i> Provide the serial number of your tobii pro device (e.g. TPNA1-030109123456.\n" +
                                        "If provided, the calibration will be opened directly."
                            )
                    }
                }
            }

            group("Shimmer") {
                row("Use devices:"){
                    checkBox("Yes").bindSelected(model::includeShimmer)
                        .comment("Should data from devices of this type be recorded?")
                }
                row("Connector path:") {
                    textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                        .bindText(model::shimmerConnectorExecutable)
                        .comment(
                            "Please <a href='https://labstreaminglayer.readthedocs.io/info/supported_devices.html'>build the Shimmer Connector</a>. " + //TODO
                                    "It is used to create an LSL stream for Shimmer devices. " +
                                    "It will be used by the plugin to get the required data.\n" +
                                    "Provide the application path <i>optionally</i> so the application can be opened when needed."
                        )
                }
            }

            group("Emotiv") {
                row("Use devices:"){
                    checkBox("Yes").bindSelected(model::includeEmotiv)
                        .comment("Should data from devices of this type be recorded?")
                }
                row("Connector path:") {
                    textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
                        .bindText(model::emotivConnectorExecutable)
                        .comment(
                            "Please <a href='https://www.emotiv.com/emotivpro/'>install EmotivPro</a>. " + //TODO
                                    "It is used to create an LSL stream for Emotiv devices. " +
                                    "It will be used by the plugin to get the required data.\n" +
                                    "Provide the application path <i>optionally</i> so the application can be opened when needed."
                        )
                }
            }
        }
    }
}
