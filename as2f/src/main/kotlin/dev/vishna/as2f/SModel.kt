package dev.vishna.as2f

import dev.vishna.mvel.interpolate
import dev.vishna.stringcode.asResource

data class SModel(
        val locale : String,
        val isOverride : Boolean,
        val textDirection : TextDirection,
        val localizables : List<Localizable>
) {
    fun emit() : String {
        return (if(isOverride) dartI18NSubClass else dartI18NClass).asResource().interpolate(this)!!
    }
}