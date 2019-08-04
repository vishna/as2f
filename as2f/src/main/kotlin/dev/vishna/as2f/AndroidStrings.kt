package dev.vishna.as2f

import com.eyeem.strings2arb.*
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

data class AndroidStrings(
        val locale: String,
        val strings : List<StringNode>,
        val plurals : List<PluralNode>
) {
    companion object {
        operator fun invoke(info: StringFileInfo) : AndroidStrings {
            val builderFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = builderFactory.newDocumentBuilder()
            val doc = docBuilder.parse(info.asFileStream())

            val stringNodesXml = doc
                    .getElementsByTagName("string")
                    .asList()

            val stringNodes = stringNodesXml.mapNotNull { stringNode ->
                try {
                    StringNode(
                            key = stringNode.attributes.getNamedItem("name").nodeValue,
                            value = stringNode.firstChild.nodeValue
                    )
                } catch (ise: IllegalStateException) {
                    null// System.err.println("${stringNode.attributes.getNamedItem("name").nodeValue}@${info.locale} NULL!!1")
                }
            }

            val pluralNodesXml = doc
                    .getElementsByTagName("plurals")
                    .asList()

            val plurals = pluralNodesXml.map { pluralNode ->
                val key = pluralNode.attributes.getNamedItem("name").nodeValue

                val quantitiesXml = pluralNode.childNodes.asList().filter { it.nodeName == "item" }

                val pluralNodes = ArrayList<StringNode>()
                quantitiesXml.mapNotNull {
                    pluralNodes += StringNode(
                            key = it.attributes.getNamedItem("quantity").nodeValue,
                            value = it.firstChild.nodeValue
                    )
                }

                PluralNode(key = key, quantities =  pluralNodes)
            }

            return AndroidStrings(
                    locale = info.locale,
                    strings = stringNodes,
                    plurals = plurals
            )
        }
    }
}

data class StringNode (
        val key: String,
        val value: String
)

data class PluralNode (
        val key: String,
        val quantities : List<StringNode>
)

fun StringFileInfo.asFileStream() : InputStream = File(path).inputStream()

fun NodeList.asList() : List<Node> {
    val output = ArrayList<Node>()
    for (i in 0 until this.length) {
        output += this.item(i)
    }
    return output
}

fun AndroidStrings.asArbMap() : Map<String, String> {
    val arbMap = HashMap<String, String>()
    val dartResolver = DartResolver()
    val currentlyUnsupported = listOf(
            "notificationMsgAgr_Plurals_MultLike",
            "notificationMsgAgr_Plurals_MultLikeSinglePhoto"
    )

    strings.forEach {
        arbMap[it.key.trailingUnderscoreIfSpecial(dartResolver.specialNames)] = it.value.toArb()
        // println("${it.key} -> ${it.value} -> ${it.value.toArb()}")
    }

    plurals.forEach { plural ->

        if (plural.key in currentlyUnsupported) {
            return@forEach
        }

        plural.quantities.forEach { quantity ->
            arbMap["${plural.key.trailingUnderscoreIfSpecial(dartResolver.specialNames)}${quantity.key.capitalize()}"] = quantity.value.toArb()
            // println("${plural.key}${quantity.key.capitalize()} -> ${quantity.value} -> ${quantity.value.toArb()}")
        }
    }

    return arbMap
}

fun Map<String, String>.asArbText() : String {
    val lines = ArrayList<String>()
    lines += "{"
    val totalCount = this.entries.size
    this.entries.forEachIndexed { index, entry ->
        val semicolon = if (index < totalCount) { "," } else { ""}
        lines += """  "${entry.key}": "${entry.value}"$semicolon"""
    }
    lines += "}"

    return lines.joinToString(separator = "\n")
}

/**
 * very unoptimized converter
 */
private fun String.toArb() : String {
    var input = this
    replacementMap.forEach { key, value ->
        input = input.replace(key, value)
    }
    return input
}

private val replacementMap = mapOf(
        "%s" to "\${arg}",
        "%d" to "\${num}",
        "%1\$s" to "\${arg1}",
        "%2\$s" to "\${arg2}",
        "%1\$d" to "\${num1}",
        "%2\$d" to "\${num2}",
        "%%" to "%",
        "\n" to " ",
        "\\\'" to "'",
        """\@""" to "@",
        "@" to """\u0025"""
)

fun String.findArgs() : List<String> {
    val regex = Regex("\\{([A-Za-z0-9]+)}", RegexOption.MULTILINE)
    return regex.findAll(this).map{ result -> result.groups[1]!!.value }.toList().sorted()
}

val _dartResolver = DartResolver()

@Suppress("UNCHECKED_CAST")
fun <T : Localizable> SModel.findLocalizable(key: String) : T? {
    return localizables.firstOrNull { it.id() == key } as T?
}

fun BasicLocalizable.verifyWithParent(parent: SModel?) : BasicLocalizable? {
    if (parent == null) {
        return this
    }

    val parentLocalizable = try {
        parent.findLocalizable<BasicLocalizable>(this.key)
    } catch (exception: ClassCastException) {
        val base = parent.findLocalizable<Localizable>(this.key)
        throw IllegalArgumentException("Translation type $this doesn't match the original: $base")
    }

    if (parentLocalizable is BasicLocalizable) {
        return this
    }

    return null
}

fun ArgsLocalizable.verifyWithParent(parent: SModel?) : ArgsLocalizable? {
    if (parent == null) {
        return this
    }

    val parentLocalizable = parent.findLocalizable<ArgsLocalizable>(this.key)

    if (parentLocalizable is ArgsLocalizable && parentLocalizable.args == args) {
        return this
    }

    throw IllegalStateException("key $key for locale $locale has $args while parent translation has ${parentLocalizable?.args}")
}

fun QuantityLocalizable.verifyWithParent(parent: SModel?) : QuantityLocalizable? {
    if (parent == null) {
        return this
    }

    val parentLocalizable = parent.findLocalizable<QuantityLocalizable>(this.key)

    if (parentLocalizable is QuantityLocalizable && parentLocalizable.args == args) {
        return this
    }

    throw IllegalStateException("key $key for locale $locale has $args while parent translation has ${parentLocalizable?.args}")
}

fun AndroidStrings.asSModel(parent: SModel? = null) : SModel {

    val localizables = strings.mapNotNull {
        val dartValue = it.value.toArb()
        val dartKey = it.key.trailingUnderscoreIfSpecial(_dartResolver.specialNames)

        val args = dartValue.findArgs()
        if (args.isEmpty()) {
            BasicLocalizable(locale, dartKey, dartValue).verifyWithParent(parent)
        } else {
            ArgsLocalizable(locale, dartKey, dartValue, args).verifyWithParent(parent)
        }
    }

    val quantityLocalizables = plurals.mapNotNull {

        val dartKey = it.key.trailingUnderscoreIfSpecial(_dartResolver.specialNames)

        val args = it.quantities.map { it.value.toArb().findArgs() }.flatten().distinctBy { it }.sorted()

        val quantityItems = it.quantities.map { QuantityItem(Quantity.from(it.key), it.value.toArb()) }

        QuantityLocalizable(locale, dartKey, args, quantityItems).verifyWithParent(parent)
    }

    return SModel(
            locale = locale,
            isOverride = locale != "en",
            textDirection = locale.asTextDirection(),
            localizables = localizables + quantityLocalizables
    )
}

fun String.asTextDirection() : TextDirection {
    if (this == "ar") return TextDirection.rtl
    return TextDirection.ltr
}