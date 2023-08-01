import org.gradle.configurationcache.extensions.capitalized
import org.gradle.internal.os.OperatingSystem

plugins {
    base
}

val os: OperatingSystem by rootProject.extra

val buildType = Attribute.of("buildType", String::class.java)

listOf("debug", "release").forEach { config ->
    val buildTask = tasks.register("compile${config.capitalized()}", Exec::class) {
        group = "build"
        commandLine("cargo", "build")
        if(config == "release") args("--release")
        outputs.file(os.getExecutableName("target/$config/injector"))
        outputs.upToDateWhen { false }
    }

    configurations.register("${config}Elements") {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes.attribute(buildType, config)
        outgoing.artifact(buildTask)
    }
}

if(os.isWindows) {
    artifacts.add("debugElements", file("target/debug/injector.pdb")) {
        builtBy("compileDebug")
    }
}

tasks.clean {
    delete("target")
}