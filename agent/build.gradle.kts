plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.google.protobuf") version "0.9.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.nilsen84"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

sourceSets {
    val compileOnly by creating {
        java {
            srcDir("src/compileOnly")
        }
    }

    main {
        compileClasspath += compileOnly.output
    }
}

dependencies {
    implementation("com.github.Nilsen84:kt-bytecode-dsl:v1.1")
    implementation("org.ow2.asm:asm-tree:9.4")
    implementation("com.google.protobuf:protobuf-kotlin:3.23.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    compileOnly("net.java.jinput:jinput:2.0.5")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.23.4"
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.jar {
    archiveClassifier.set("thin")
    manifest.attributes(
        "Premain-Class" to "io.github.nilsen84.lcqt.LcqtPatcher"
    )
}

tasks.shadowJar {
    archiveClassifier.set(null as String?)
}