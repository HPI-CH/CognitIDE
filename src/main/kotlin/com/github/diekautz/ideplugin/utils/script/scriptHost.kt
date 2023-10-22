package com.github.diekautz.ideplugin.utils.script

import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

fun evalFile(scriptFile: File, scriptArgs: String): ResultWithDiagnostics<EvaluationResult> {

    val evaluationContext = ScriptEvaluationConfiguration {
        constructorArgs(scriptArgs)
    }

    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptWithMavenDeps>()

    return BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, evaluationContext)
}

fun runScript(args: Array<String>) { //todo varargs etc
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