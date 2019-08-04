package dev.vishna.as2f

import dev.vishna.stringcode.asResource
import dev.vishna.mvel.interpolate
import java.util.Locale

sealed class Localizable {
    open fun emit() : String = "// TODO implement"
    open fun id() : String = TODO()
}

data class BasicLocalizable(
        val locale: String,
        val key: String,
        val value: String
) : Localizable() {
    override fun emit() = """String get $key => "$value";"""
    override fun id(): String = key
}

data class ArgsLocalizable(
        val locale: String,
        val key: String,
        val value: String,
        val args: List<String>
) : Localizable() {
    override fun emit() = """String $key(${args.map { "String $it" }.joinToString(separator = ", ")}) => "$value";"""
    override fun id(): String = key
}

data class QuantityLocalizable(
        val locale: String,
        val key: String,
        val args: List<String>,
        val items: List<QuantityItem>
) : Localizable() {
    override fun emit(): String = dartI18NQuantity.asResource().interpolate(this)!!
    override fun id(): String = key
    fun emitArgs() : String {
        return (listOf("int quantity") + args.map { "String $it" }).joinToString(separator = ", ")
    }
}

sealed class Quantity(val value: String) {
    object zero : Quantity("ZERO")
    object one : Quantity("ONE")
    object two : Quantity("TWO")
    object few : Quantity("FEW")
    object many : Quantity("MANY")
    object other : Quantity("OTHER")

    companion object {
        fun from(value : String) : Quantity {
            return when (value.toLowerCase(Locale.US)) {
                "zero" -> zero
                "one" -> one
                "two" -> two
                "few" -> few
                "many" -> many
                "other" -> other
                else -> throw IllegalStateException("Unexpected $value for quantity")
            }
        }
    }
}

data class QuantityItem(val quantity: Quantity, val value: String)

sealed class TextDirection(val value: String) {
    object ltr : TextDirection("ltr")
    object rtl : TextDirection("rtl")
}