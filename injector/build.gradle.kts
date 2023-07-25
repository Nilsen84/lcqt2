import org.gradle.configurationcache.extensions.capitalized
import org.gradle.internal.os.OperatingSystem

plugins {
    base
}

val os: OperatingSystem by rootProject.extra

val buildType = Attribute.of("buildType", String::class.java)

listOf("debug", "release").forEach { config ->
    val buildTask = tasks.register("build${config.capitalized()}", Exec::class) {
        group = "build"
        commandLine("cargo", "build")
        if(config == "release") args("--release")
        outputs.file(os.getExecutableName("target/$config/injector"))
        outputs.upToDateWhen { false }
    }

    configurations.register("${config}Exe") {
        isCanBeResolved = false
        attributes.attribute(buildType, config)
    }

    artifacts.add("${config}Exe", buildTask)
}

if(os.isWindows) {
    artifacts.add("debugExe", file("target/debug/injector.pdb")) {
        builtBy("buildDebug")
    }
}

tasks.clean {
    delete("target")
}