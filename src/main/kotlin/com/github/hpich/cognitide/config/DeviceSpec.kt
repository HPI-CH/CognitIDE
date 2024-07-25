package com.github.hpich.cognitide.config

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag

@Tag("DeviceSpec")
data class DeviceSpec(
    @Attribute var name: String = "",
    @Attribute var connectorPath: String = "",
)
