package dev.vishna.as2f

import dev.vishna.emojilog.std.*
import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.camelize
import dev.vishna.stringcode.saveAs
import kotlinx.coroutines.*
import java.io.File
import java.lang.IllegalStateException
import java.util.*

private val log by lazy { defaultLogger() }

/**
 * Template for the router paths
 */
const val dartVoyagerPathsClass: ResourcePath = "/dart_voyager_paths_class.mvel"

/**
 * Template for the automated test classes
 */
const val dartVoyagerTests: ResourcePath = "/dart_voyager_tests.mvel"

/**
 * Template for the automated scenario class
 */
const val dartVoyagerTestScenarioClass: ResourcePath = "/dart_voyager_tests_scenario_class.mvel"

/**
 * Template for the automated scenario execution block
 */
const val dartVoyagerTestScenarioExecutionBlock: ResourcePath = "/dart_voyager_tests_scenario_execution_block.mvel"

/**
 * Initial template this tool consumes
 */
const val voyagerCodegen: ResourcePath = "/voyager-codegen.yaml"

fun bootstrapAs2fPatrolConfig(patrolFile: File) = if (File(pwd, "pubspec.yaml").exists()) {
    log.alert.."${patrolFile.name} not found, creating one for you..."
    voyagerCodegen.asResource().saveAs(patrolFile.absolutePath)
    log.save.."${patrolFile.name} created, please edit it"
    true
} else {
    false
}

suspend fun generateCode(name: String, source: String, target: String, testTarget: String?, dryRun: Boolean) = supervisorScope {

    if (source.isBlank()) {
        throw IllegalStateException("No source value provided for $name")
    }

    val mjolnirFile = source.asFile()

    if (!mjolnirFile.exists()) {
        throw IllegalStateException("Provided source file for $name doesn't exist")
    }

    // TODO

    val jobs = mutableListOf<Job>()
    jobs += async { delay(500) }
    if (!testTarget.isNullOrBlank()) {
        jobs += async {
          delay(500)
        }
    }
    jobs.forEach { it.join() }
}

private fun String.saveToTarget(target: String, dryRun: Boolean) {
    if (dryRun) {
        log.tool..target
        "------------------------------".println
        println
        "------------------------------".println
    } else {
        // save to the target location
        val targetFile = target.asFile()
        val oldHashCode = if (!targetFile.exists()) {
            File(targetFile.parent).mkdirs()
            0
        } else {
            targetFile.readText().hashCode()
        }

        if (oldHashCode != hashCode()) {
            saveAs(target)
            log.save..target
        } else {
            log.skip..target
        }
    }
}