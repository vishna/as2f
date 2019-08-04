package com.eyeem.strings2arb

import java.io.File

object FolderCrawler {
    fun lookup(resBasePath: String) : List<StringFileInfo> {
        val valueFolders = File(resBasePath).list().filter { File("$resBasePath/$it/strings.xml").exists() }

        val stringFiles = valueFolders
                .map { StringFileInfo(path = "$resBasePath/$it/strings.xml", locale = it.toLanguageCode()) }

        return stringFiles
    }
}


data class StringFileInfo(
        val path : String,
        val locale : String
)

private val langMappings = mapOf(
        "pt-rBR" to "pt_BR",
        "zh-rTW" to "zh_TW",
        "id" to "in"
)

fun String.toLanguageCode() : String {
    if (this == "values") {
        return "en"
    }

    val code = this.removePrefix("values-")
    return langMappings[code] ?: code
}