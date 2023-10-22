package com.github.diekautz.ideplugin.hostNew //todo

import com.github.diekautz.ideplugin.scriptdefinition.ScriptWithMavenDeps
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Paths
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.scriptCompilationClasspathFromContextOrStdlib
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

fun evalFile(scriptFile: File, scriptArgs: String): ResultWithDiagnostics<EvaluationResult> {

    val evaluationContext = ScriptEvaluationConfiguration {
        constructorArgs(scriptArgs)
    }

    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptWithMavenDeps>()

    return BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, evaluationContext)
}

fun main(args: Array<String>) { //todo varargs etc
    if (args.size != 2) {
        println("usage: <app> <script file> <script args>")
    } else {
        val scriptFile = File(args[0])
        val scriptArgs = args[1]
        println("Executing script $scriptFile")

        val res = evalFile(scriptFile, scriptArgs)

        res.reports.forEach {
            if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
                println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
            }
        }
    }
}