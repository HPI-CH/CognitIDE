package com.github.hpich.cognitide.utils.script

import java.io.File
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

// taken from: https://kotlinlang.org/docs/custom-script-deps-tutorial.html

fun evalFile(scriptFile: File, scriptArgs: String): ResultWithDiagnostics<EvaluationResult> {

    val evaluationContext = ScriptEvaluationConfiguration {
        constructorArgs(scriptArgs)
    }

    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<ScriptWithMavenDeps>()

    return BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), compilationConfiguration, evaluationContext)
}

fun runScript(args: Array<String>, pluginClassLoader: ClassLoader) { //todo varargs etc
    if (args.size != 2) {
        println("usage: <app> <script file> <script args>")
    } else {
        val scriptFile = File(args[0])
        val scriptArgs = args[1]
        println("Executing script $scriptFile")
        val currentThread = Thread.currentThread()
        val originalClassLoader = currentThread.getContextClassLoader()
        try {
            currentThread.setContextClassLoader(pluginClassLoader)
        val res = evalFile(scriptFile, scriptArgs)

        res.reports.forEach {
            if (it.severity > ScriptDiagnostic.Severity.DEBUG) {
                println(" : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
            }
        }
        } finally {
            currentThread.setContextClassLoader(originalClassLoader)
        }
    }
}