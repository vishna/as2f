package dev.vishna.as2f


abstract class LangResolver {

    fun type(member: Member): String {
        return when(member) {
            is Field -> memberType(member)
            is Mixin -> "Mixin"
        }
    }

    abstract fun memberType(field: Field) : String

    abstract fun className(model: Model) : String

    companion object {
        operator fun invoke(lang: String) : LangResolver {
            return when(lang) {
                "dart" -> DartResolver()
                else -> throw IllegalStateException("Language $lang is not supported yet")
            }
        }
    }

    open val specialNames: List<String>
        get() = emptyList()
}