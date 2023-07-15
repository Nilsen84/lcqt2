import org.gradle.internal.os.OperatingSystem

plugins {
    base
}

val os: OperatingSystem by rootProject.extra

val npm = if (os.isWindows) "npm.cmd" else "npm"

val npmInstall by tasks.registering(Exec::class) {
    group = "build"
    commandLine(npm, "install")
}

val buildAsar by tasks.registering(Exec::class) {
    group = "build"
    commandLine(npm, "run", "build")

    outputs.file("out/gui.asar")
    inputs.dir("src")
    inputs.dir("public")
    inputs.dir("node_modules")
    inputs.files(
        "index.html",
        "package.json",
        "svelte.config.js",
        "tsconfig.json",
        "tsconfig.node.json",
        "vite.config.ts"
    )
}

tasks.clean {
    delete("out")
}

configurations.create("asar") {
    isCanBeResolved = false
}

artifacts {
    add("asar", buildAsar)
}