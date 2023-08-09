package io.github.nilsen84.lcqt

import io.github.nilsen84.lcqt.patches.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.lang.instrument.Instrumentation

object LcqtPatcher {
    val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @get:JvmName("configDir")
    val configDir = getConfigDir()

    val config: Config = try {
        JSON.decodeFromString(configDir.resolve("config.json").readText())
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        Config()
    }

    @JvmStatic
    fun premain(configPath: String?, inst: Instrumentation) {
        val patches = mutableListOf<Patch>(ClassloaderPatch())
        if (config.cosmeticsEnabled) patches += CosmeticsPatch()
        if (config.freelookEnabled) patches += FreelookPatch()
        if (config.crackedEnabled) patches += CrackedAccountPatch()
        if (config.noHitDelayEnabled) patches += NoHitDelayPatch()
        if (config.debugModsEnabled) patches += DebugModsPatch()
        if (config.fpsSpoofEnabled) patches += FPSSpoofPatch()
        if (config.rawInputEnabled) patches += RawInputPatch()
        if (config.packFixEnabled) patches += PackFixPatch()

        println("RUNNING LCQT WITH PATCHES: " + patches.joinToString {
            it::class.simpleName!!
        })

        inst.addTransformer(Transformer(patches))
    }

    private fun getConfigDir(): File {
        val os = System.getProperty("os.name").lowercase()
        val home = System.getProperty("user.home")

        val configDir: File = when {
            os.contains("windows") -> System.getenv("APPDATA")?.let(::File) ?: File(home, "AppData\\Roaming")
            os.contains("mac") -> File(home, "Library/Application Support")
            else -> System.getenv("XDG_CONFIG_HOME")?.let(::File) ?: File(home, ".config")
        }

        return File(configDir, "lcqt2")
    }
}