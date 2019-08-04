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
    compile("com.github.vishna:kmnd:master-SNAPSHOT")

    testCompile("junit", "junit", "4.12")
    testCompile("org.amshove.kluent:kluent:1.34")
}
