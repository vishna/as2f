package com.eyeem.strings2arb

import dev.vishna.as2f.SModel
import dev.vishna.as2f.dartI18N
import dev.vishna.dartfmt.dartfmt
import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource

class DartI18N(val languages: List<SModel>) {
    val locales : List<Locale> = languages.map { Locale(it.locale) }
    val supportedLocales : List<Locale> = locales.filter { it != Locale("en", "", "en") }

    suspend fun emit() : String = dartI18N.asResource().interpolate(this)!!.dartfmt()
}

data class Locale(val code: String, val region: String, val value: String) {

    fun emitRegion() : String {
        if (region.isBlank()) {
            return "null"
        } else {
            return """"$region""""
        }
    }

    companion object {
        operator fun invoke(lang: String) : Locale {
            val parts = lang.split("_");
            return when (parts.size) {
                1 -> Locale(code = parts.first(), region = "", value = lang)
                2 -> Locale(code = parts.first(), region = parts.last(), value = lang)
                else -> throw IllegalStateException("$lang is not a valid language code")
            }
        }
    }
}