import org.gradle.internal.os.OperatingSystem

plugins {
    base
    distribution
}

val os by extra(OperatingSystem.current()!!)
val dist by configurations.creating

dependencies {
    dist(project(":patcher", "fat"))
    dist(project(":injector", "exe"))
    dist(project(":gui", "asar"))
}

tasks.distTar {
    compression = Compression.GZIP
    archiveExtension.set("tar.gz")
}

distributions {
    main {
        contents {
            from(dist).rename {
                it.replace("injector", "Lunar Client Qt")
            }
            into("/")
        }
    }
}

tasks.register("run", Exec::class) {
    group = "distribution"
    dependsOn(tasks.installDist)
    executable(os.getExecutableName(
        "${tasks.installDist.get().destinationDir}/Lunar Client Qt"
    ))
}