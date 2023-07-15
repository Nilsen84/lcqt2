import org.gradle.internal.os.OperatingSystem

plugins {
    base
}

val os: OperatingSystem by rootProject.extra

val cargoBuild by tasks.registering(Exec::class) {
    group = "build"
    commandLine("cargo", "build", "--release")

    outputs.file(os.getExecutableName("target/release/injector"))
    //cargo is so fast theres no point in specifying inputs
    outputs.upToDateWhen { false }
}

tasks.clean {
    delete("target")
}

configurations.create("exe") {
    isCanBeResolved = false
}

artifacts {
    add("exe", cargoBuild)
}