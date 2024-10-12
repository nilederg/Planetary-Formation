plugins {
    kotlin("jvm") version "2.0.20" // Use your Kotlin version
    application // Add this plugin for easy execution
}

application {
    mainClass.set("MainKt") // Since there's no package, just use "Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt" // Again, since there's no package
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

