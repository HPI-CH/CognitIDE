plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "ide-plugin"
include("script-definition")
include("host")
include("host:src:main:kotlin")
findProject(":host:src:main:kotlin")?.name = "kotlin"
include("host")
include("hostNew")
