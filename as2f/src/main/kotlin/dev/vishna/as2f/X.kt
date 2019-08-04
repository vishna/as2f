package dev.vishna.as2f

import dev.vishna.kmnd.execute
import dev.vishna.kmnd.weaveToBlocking
import kotlinx.coroutines.coroutineScope
import org.yaml.snakeyaml.Yaml
import java.io.*

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
}