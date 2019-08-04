package dev.vishna.as2f

import java.lang.IllegalStateException

sealed class Type(val name: String) {
    companion object {
        operator fun invoke(name: String) : Type {
            val typeInstance = Type::class
                    .sealedSubclasses
                    .mapNotNull { it.objectInstance }
                    .find { it.name == name }
            return typeInstance ?: throw IllegalStateException("No type found for $name")
        }
    }
}

object TypeString : Type("String")
object TypeInt : Type("Int")
object TypeDate : Type("Date")
object TypeFloat : Type("Float")
object TypeLong : Type("Long")
object TypeBoolen : Type("Boolean")