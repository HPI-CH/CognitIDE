package com.github.diekautz.ideplugin.utils

// https://kotlinlang.org/docs/custom-script-deps-tutorial.html#create-a-scripting-host
//todo attribute

import com.github.diekautz.ideplugin.utils.script.configureMavenDepsOnAnnotations
import kotlinx.coroutines.runBlocking
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.*
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(fileExtension = "config.kts", compilationConfiguration = ScriptConfiguration::class)
abstract class ConfigurationScript

object ScriptConfiguration : ScriptCompilationConfiguration({
    defaultImports(DependsOn::class, Repository::class)
    jvm {

        dependenciesFromCurrentContext(wholeClasspath = true)
    }
    // Callbacks
    refineConfiguration {
        // Process specified annotations with the provided handler
        onAnnotations(DependsOn::class, Repository::class, handler = ::configureMavenDepsOnAnnotations)
    }
    //compilerOptions("-no-stdlib")//, "-no-reflect"
    //compilerOptions("-Xadd-modules=ide-plugin") //ALL-MODULE-PATH
})



// Handler that reconfigures the compilation on the fly
fun configureMavenDepsOnAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
        ?: return context.compilationConfiguration.asSuccess()
    return runBlocking {
        resolver.resolveFromScriptSourceAnnotations(annotations)
    }.onSuccess {
        context.compilationConfiguration.with {
            dependencies.append(JvmDependency(it))
        }.asSuccess()
    }
}

private val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())