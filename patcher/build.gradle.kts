import org.gradle.internal.os.OperatingSystem

plugins {
    base
}

val os: OperatingSystem by rootProject.extra

val npm = if (os.isWindows) "npm.cmd" else "npm"

val npmInstall by tasks.registering(Exec::class) {
    group = "build"
    commandLine(npm, "install")
    inputs.file("package.json")
    outputs.file("package-lock.json")
    outputs.dir("node_modules")
}

val buildGui by tasks.registering(Exec::class) {
    dependsOn(npmInstall)
    group = "build"
    commandLine(npm, "run", "build")

    //recompile if tag changes
    inputs.dir("../.git/refs/tags")
    inputs.dir("node_modules")
    inputs.files(
        "tsconfig.json",
    )

    outputs.dir("build")
}

tasks.clean {
    delete("build")
}