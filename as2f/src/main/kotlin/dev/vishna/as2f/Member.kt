package dev.vishna.as2f


import java.lang.IllegalStateException

import dev.vishna.stringcode.applyIf

class Mixin(override val info: Info, val subtype: String, val expression: String) : Member()

sealed class Field(override val info: Member.Info) : Member()
class StringField(info: Member.Info) : Field(info)
class IntField(info: Member.Info) : Field(info)
class DateField(info: Member.Info) : Field(info)
class FloatField(info: Member.Info) : Field(info)
class LongField(info: Member.Info) : Field(info)
class BooleanField(info: Member.Info) : Field(info)
class ArrayField(info: Member.Info, val field: Field) : Field(info)
class CustomField(info: Info, val customType: String) : Field(info)

sealed class Member {
    abstract val info: Info

    val resolver : LangResolver by lazy { LangResolver(info.lang) }

    val type: String
        get() = resolver.type(this)

    companion object {
        operator fun invoke(knownModels: List<String>, otherModels: List<String>, parent: Model, lang: String, key: String, value: Any?) : Member? {
            val resolver = LangResolver(lang)
            return when(value) {
                is String -> fieldFor(resolver, knownModels, parent, lang, key, value)
                is Map<*, *> -> arrayFor(resolver, knownModels, otherModels, parent, lang, key, value) ?: mixinFor(resolver, parent, lang, key, value)
                else -> throw IllegalStateException("cannot instantiate member for $value")
            }
        }
    }

    data class Info(
            val lang: String,
            val name: String,
            val nameUnescaped: String,
            val parent: Model,
            val optional: Boolean,
            val serializable: Boolean
    )

}

fun String.trailingUnderscoreIfSpecial(specials: List<String>) : String {
    if (this in specials) {
        return "${this}_"
    }
    return this
}

fun fieldFor(resolver: LangResolver, knownModels: List<String>, parent: Model, lang: String, key: String, type: String) : Member {
    val isOptional = type.endsWith("?")
    val isSerializable = !key.endsWith("^")
    val actualType = type
            .applyIf(isOptional) {
                removeSuffix("?")
            }

    val nameUnescaped = key.applyIf(!isSerializable) {
        removeSuffix("^")
    }

    val info = Member.Info(
            lang = lang,
            optional = isOptional,
            serializable = isSerializable,
            nameUnescaped = nameUnescaped,
            name = nameUnescaped.trailingUnderscoreIfSpecial(resolver.specialNames),
            parent = parent
    )

    if (type in knownModels) {
        return CustomField(info, actualType)
    }

    return when (Type(actualType)) {
        TypeString -> StringField(info)
        TypeInt -> IntField(info)
        TypeDate -> DateField(info)
        TypeFloat -> FloatField(info)
        TypeLong -> LongField(info)
        TypeBoolen -> BooleanField(info)
    }
}

fun arrayFor(resolver: LangResolver, knownModels: List<String>, otherModels: List<String>, parent: Model, lang: String, key: String, map: Map<*, *>) : ArrayField? {
    val entries = map.entries
    // needs to be tuple
    if (entries.size != 1) return null

    val firstEntry = entries.first()

    // key needs to indicate this is array
    if (firstEntry.key != "Array") return null

    val type = firstEntry.value?.toString() ?: return null

    val isOptional = type.endsWith("?")
    val isSerializable = !key.endsWith("^")
    val actualType = type
            .applyIf(isOptional) {
                removeSuffix("?")
            }

    val nameUnescaped = key.applyIf(!isSerializable) {
        removeSuffix("^")
    }

    val info = Member.Info(
            lang = lang,
            optional = isOptional,
            serializable = isSerializable,
            nameUnescaped = nameUnescaped,
            name = nameUnescaped.trailingUnderscoreIfSpecial(resolver.specialNames),
            parent = parent
    )

    if (type in knownModels || type in otherModels) {
        return ArrayField(info, CustomField(info, actualType))
    }

    return ArrayField(info, when (Type(actualType)) {
        TypeString -> StringField(info)
        TypeInt -> IntField(info)
        TypeDate -> DateField(info)
        TypeFloat -> FloatField(info)
        TypeLong -> LongField(info)
        TypeBoolen -> BooleanField(info)
    })
}

fun mixinFor(resolver: LangResolver, parent: Model, lang: String, key: String, map: Map<*, *>) : Mixin? {
    // key needs to indicate for which lanugage the mixin is meant
    if (!key.startsWith(lang)) return null

    // needs to be tuple
    if (map.size != 1) return null

    val (mixinType, expression) = map.entries.toList().first()
    if (mixinType !is String || !mixinType.endsWith("Mixin") || expression !is String) return null

    val mixinSubType = mixinType.removeSuffix("Mixin")

    val nameUnescaped = key.removePrefix("${lang}_")

    return Mixin(
            info = Member.Info(
                    lang = lang,
                    name = nameUnescaped,
                    nameUnescaped = nameUnescaped,
                    optional = false,
                    serializable = false,
                    parent = parent
            ),
            subtype = mixinSubType,
            expression = expression
    )
}

fun Map<String, *>.asMembers(parent: Model, knownModels: List<String>, otherModels: List<String>, lang: String) : List<Member> {
    return this.mapNotNull { (key, value) -> Member(knownModels, otherModels, parent, lang, key, value) }
}