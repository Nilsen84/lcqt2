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

    inputs.dir("src")
    inputs.dir("inject")
    inputs.dir("node_modules")
    inputs.files(
        "index.html",
        "svelte.config.js",
        "tsconfig.json",
        "tsconfig.node.json",
        "vite.config.ts",
        "postcss.config.js",
        "tailwind.config.js"
    )

    outputs.dir("out")
}

tasks.clean {
    delete("out")
}

configurations.create("asar") {
    isCanBeConsumed = true
    isCanBeResolved = false
    outgoing.artifact(file("out/gui.asar")) {
        builtBy(buildGui)
    }
}