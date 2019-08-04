package dev.vishna.as2f

import dev.vishna.as2f.LangResolver
import dev.vishna.stringcode.smartCamelize
import java.lang.IllegalStateException

class DartResolver : LangResolver() {
    override fun className(model: Model): String = model.name.smartCamelize()

    override fun memberType(field: Field): String {

        return when(field) {
            is StringField -> "String"
            is IntField -> "int"
            is DateField -> "DateTime"
            is FloatField -> "double"
            is LongField -> "int"
            is BooleanField -> "bool"
            is ArrayField -> "List<${memberType(field.field)}>"
            is CustomField -> field.customType.smartCamelize()
        }
    }

    fun dynamicType(field: Field) : String {

        return with(field.info) {
            when (field) {
                is CustomField -> """${field.customType.smartCamelize()}"""
                is DateField -> """DateTime"""
                is ArrayField -> """List<${memberType(field.field)}>"""
                else -> """dynamic"""
            }
        }
    }

    fun ctor1(field: Field) : String {

        if (!field.info.serializable) { // means it isn't part of json
            return "null"
        }

        return with(field.info) {
            when (field) {
                is CustomField -> """${field.customType.smartCamelize()}.fromJson(json['$nameUnescaped'])"""
                is DateField -> """DateTime.parse(json['$nameUnescaped'])"""
                is ArrayField -> """json['$nameUnescaped']?.map((it) => ${arrayCtor(field.field)})?.toList()${arrayCast(field.field)}"""
                is FloatField -> "json['$nameUnescaped']?.toDouble()"
                is StringField -> "json['$nameUnescaped']?.toString()"
                else -> """json['$nameUnescaped']"""
            }
        }
    }

    private fun arrayCtor(innerField: Field) : String {
        return when (innerField) {
            is FloatField -> "it?.toDouble()"
            is LongField,
            is BooleanField,
            is StringField,
            is DateField,
            is IntField -> "it"
            is CustomField -> """${innerField.customType.smartCamelize()}?.fromJson(it)"""
            is ArrayField -> throw IllegalStateException("nested arrays unsupported because developer is lazy.")
        }
    }

    private fun arrayCast(innerField: Field) : String {
        return when (innerField) {
            is FloatField,
            is LongField,
            is BooleanField,
            is StringField,
            is DateField,
            is IntField -> """?.cast<${memberType(innerField)}>()"""
            is CustomField -> """?.cast<${innerField.customType.smartCamelize()}>()"""
            is ArrayField -> throw IllegalStateException("nested arrays unsupported because developer is lazy.")
        }
    }

    private fun arrayMap(innerField: Field) : String {
        return when (innerField) {
            is FloatField,
            is LongField,
            is BooleanField,
            is StringField,
            is DateField,
            is IntField -> "it"
            is CustomField -> """it?.copyWith()"""
            is ArrayField -> throw IllegalStateException("nested arrays unsupported because developer is lazy.")
        }
    }

    fun copyWith(field: Field) : String {
        return with(field.info) {
            when (field) {
                is FloatField,
                is LongField,
                is BooleanField,
                is StringField,
                is DateField,
                is IntField -> """${field.info.name} ?? this.${field.info.name}"""
                is CustomField -> """${field.info.name} ?? this.${field.info.name}?.copyWith()"""
                is ArrayField -> """${field.info.name} ?? this.${field.info.name}?.map((it) => ${arrayMap(innerField = field.field)} )?.toList()"""
            }
        }
    }

    fun dtor(field: Field) : String {

        if (!field.info.serializable) {
            return "null"
        }

        return with(field.info) {
            when (field) {
                is FloatField,
                is LongField,
                is BooleanField,
                is StringField,
                is IntField -> name
                is DateField -> """$name?.toIso8601String()"""
                is CustomField -> """$name?.toJson()"""
                is ArrayField -> """$name?.map((it) => ${arrayDtor(field.field)})?.toList()"""
            }
        }
    }

    fun arrayDtor(field: Field) : String {
        return when (field) {
            is FloatField,
            is LongField,
            is BooleanField,
            is StringField,
            is IntField -> "it"
            is DateField -> """it?.toIso8601String()"""
            is CustomField -> """it?.toJson()"""
            is ArrayField -> throw IllegalStateException("nested arrays unsupported because developer is lazy.")
        }
    }

    fun objectId(model: Model) : String {

        val hasIdField = model.fields.find { it is StringField && it.info.name == "id" } != null

        if (hasIdField) {
            return """"${className(model)}#${'$'}{id}";"""
        } else {
            return "null; // id field not recognized by generator"
        }
    }

    override val specialNames: List<String>
        get() = listOf("new", "do", "with", "other")
}