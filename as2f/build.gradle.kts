import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    maven
}

kotlinProject()

dependencies {
    compile("org.yaml:snakeyaml:1.8")
    compile("com.github.vishna:mvel-ktx:master-SNAPSHOT")
    compile("com.github.vishna:string-code-ktx:master-SNAPSHOT")
    compile("com.github.vishna:watchservice-ktx:master-SNAPSHOT")
    compile("com.github.vishna:emojilog:master-SNAPSHOT")
    compile("com.github.vishna:dartfmt-ktx:0.0.4")

    testCompile("junit", "junit", "4.12")
    testCompile("org.amshove.kluent:kluent:1.34")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}