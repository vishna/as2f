package dev.vishna.as2f

import dev.vishna.emojilog.android.info
import dev.vishna.emojilog.android.warn
import dev.vishna.kmnd.execute
import dev.vishna.kmnd.weaveToBlocking
import kotlinx.coroutines.coroutineScope
import org.yaml.snakeyaml.Yaml
import java.io.*
import java.lang.IllegalStateException

internal fun String.asYaml() :  Map<String, Map<String, *>> = Yaml().load(StringReader(this)) as Map<String, Map<String, *>>

typealias ResourcePath = String

inline val <T> T.println
    get() = println(this)

/**
 * Holds current working directory as path.
 */
internal val pwd: FilePath by lazy { System.getProperty("user.dir") }

internal typealias FilePath = String
internal fun FilePath.asFile() : File {
    val file = File(this)
    return if (file.exists()) {
        file
    } else {
        File(pwd, this)
    }
}

suspend fun String.dartfmt() : String = coroutineScope {
    // TODO add some sort of LRU cache for this
    try {
        val dartOutputStream = ByteArrayOutputStream()
        val result = listOf("dartfmt").execute { outputStream, inputStream, errorStream ->

            outputStream.use {
                this@dartfmt weaveToBlocking outputStream
            }

            inputStream weaveToBlocking dartOutputStream
            errorStream weaveToBlocking System.err
        }

        if (result != 0) {
            throw IllegalStateException("dartfmt returned exit code $result")
        }

        dartOutputStream.toByteArray().toString(Charsets.UTF_8)
    } catch (e: IOException) {
        log.warn..e
        log.info.."If you see message about dartfmt, you might need to export PATH to dart-sdk"
        log.info.."""e.g. export PATH="${'$'}PATH:/path/to/flutter/bin/cache/dart-sdk/bin""""
        this@dartfmt
    }
}