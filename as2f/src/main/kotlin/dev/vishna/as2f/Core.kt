package dev.vishna.as2f

import com.eyeem.strings2arb.DartI18N
import com.eyeem.strings2arb.FolderCrawler
import dev.vishna.emojilog.std.*
import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource
import dev.vishna.stringcode.camelize
import dev.vishna.stringcode.saveAs
import kotlinx.coroutines.*
import java.io.File
import java.lang.IllegalStateException
import java.util.*

internal val log by lazy { defaultLogger() }

const val dartI18N = "/dart_i18n.mvel"
const val dartI18NClass = "/dart_i18n_class.mvel"
const val dartI18NSubClass = "/dart_i18n_subclass.mvel"
const val dartI18NQuantity = "/dart_i18n_quantity.mvel"

/**
 * Initial template this tool consumes
 */
const val as2f: ResourcePath = "/as2f.yaml"

fun bootstrapAs2fPatrolConfig(patrolFile: File) = if (File(pwd, "pubspec.yaml").exists()) {
    log.alert.."${patrolFile.name} not found, creating one for you..."
    as2f.asResource().saveAs(patrolFile.absolutePath)
    log.save.."${patrolFile.name} created, please edit it"
    true
} else {
    false
}

suspend fun generateCode(name: String, source: String, target: String, dryRun: Boolean) = supervisorScope {

    if (source.isBlank()) {
        throw IllegalStateException("No source value provided for $name")
    }

    val resourcesDirectory = source.asFile()

    if (!resourcesDirectory.exists()) {
        throw IllegalStateException("Provided source file for $name doesn't exist")
    }

    val strings = FolderCrawler
            .lookup(resourcesDirectory.absolutePath)
            .map { AndroidStrings(it) }

    val stringsEn = strings.firstOrNull { it.locale == "en" }!!

    val parentClass = stringsEn.asSModel()
    val subClasses = strings.filter { it != stringsEn }.map { it.asSModel(parentClass) }

    val dartI18N = DartI18N(listOf(parentClass) + subClasses)

    dartI18N.emit().saveToTarget(target, dryRun)
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