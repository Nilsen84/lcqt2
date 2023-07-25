plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.google.protobuf") version "0.9.3"
}

group = "io.github.nilsen84"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Nilsen84:kt-bytecode-dsl:v1.0")
    implementation("org.ow2.asm:asm-tree:9.4")
    implementation("com.google.protobuf:protobuf-kotlin:3.23.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.23.4"
    }

    generateProtoTasks {
        all().configureEach {
            builtins {
                register("kotlin")
            }
        }
    }
}

tasks.jar {
    exclude("*.proto")
    includeEmptyDirs = false
    from({ configurations.runtimeClasspath.get().map { zipTree(it) } }) {
        exclude("**/module-info.class")
    }
    manifest.attributes(
        "Premain-Class" to "io.github.nilsen84.lcqt.PremainKt"
    )
}