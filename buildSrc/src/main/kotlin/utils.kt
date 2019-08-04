import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {
    dependencies {
        "compile"(kotlin("stdlib"))
        "compile"("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutinesVersion}")
        "compile"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutinesVersion}")
    }
}

object Versions {
    const val kotlinVersion = "1.3.21"
    const val kotlinCoroutinesVersion = "1.0.1"
}