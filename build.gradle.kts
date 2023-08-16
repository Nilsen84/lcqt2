import org.gradle.internal.os.OperatingSystem

plugins {
    base
    distribution
}

val os by extra(OperatingSystem.current()!!)

val buildType = Attribute.of("buildType", String::class.java)

val components by configurations.creating {
    attributes.attribute(buildType, "debug")
}

val dist by configurations.registering {
    extendsFrom(components)
    attributes.attribute(buildType, "release")
}

dependencies {
    components(project(":agent", "shadow"))
    components(project(":injector"))
    components(project(":gui", "asar"))
    components(project(":patcher", "file"
}

tasks.withType<Tar>().configureEach {
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
    create("debug") {
        contents {
            from(components)
        }
    }
}

tasks.register("runDebug", Exec::class) {
    group = "distribution"
    val installDebugDist by tasks.getting(Sync::class)
    dependsOn(installDebugDist)
    executable(os.getExecutableName(
        "${installDebugDist.destinationDir}/injector"
    ))
}

tasks.register("run", Exec::class) {
    group = "distribution"
    dependsOn(tasks.installDist)
    executable(os.getExecutableName(
        "${tasks.installDist.get().destinationDir}/Lunar Client Qt"
    ))
}
