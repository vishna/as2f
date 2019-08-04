package dev.vishna.as2f

import dev.vishna.stringcode.applyIf

data class Model(
        val knownModels: List<String>,
        val otherModels: List<String>,
        val packageName: String,
        val name: String,
        private val membersMap: Map<String, *>,
        val lang: String
) {

    val members: List<Member> = membersMap.asMembers(this, knownModels, otherModels, lang)
    val resolver = LangResolver(lang)

    @Suppress("unused")
    val fields: List<Member>
        get() = members.filter { it is Field }

    @Suppress("unused")
    fun mixins(subtype: String?): List<Mixin> = (members
            .filter { it is Mixin } as List<Mixin>)
            .applyIf(!subtype.isNullOrBlank()) {
                filter { it.subtype == subtype }
            }

    @Suppress("unused")
    val className: String
        get() = resolver.className(this)

    companion object {
        fun fromYAML(lang: String, packageName: String, yaml: Map<String, Map<String, *>>, otherModels: List<String>): List<Model> {

            val knownModels = yaml.map { (className, _) -> className }

            return yaml.map { (className, membersMap) ->
                Model(
                        knownModels = knownModels,
                        otherModels = otherModels,
                        lang = lang,
                        packageName = packageName,
                        name = className,
                        membersMap = membersMap
                )
            }
        }
    }
}