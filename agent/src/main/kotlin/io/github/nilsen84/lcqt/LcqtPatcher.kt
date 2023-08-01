package io.github.nilsen84.lcqt

import io.github.nilsen84.lcqt.patches.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.lang.instrument.Instrumentation

object LcqtPatcher {
    val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }
    lateinit var configFile: File private set
    lateinit var config: Config private set

    @JvmStatic
    fun premain(configPath: String, inst: Instrumentation) {
        configFile = File(configPath)
        config = try {
            JSON.decodeFromString<Config>(File(configPath).readText())
        } catch (e: FileNotFoundException) {
            Config()
        }

        val patches = mutableListOf<Patch>(ClassloaderPatch())
        if (config.cosmeticsEnabled) patches += CosmeticsPatch()
        if (config.freelookEnabled) patches += FreelookPatch()
        if (config.crackedEnabled) patches += CrackedAccountPatch()

        println("RUNNING LCQT WITH PATCHES: " + patches.joinToString {
            it::class.simpleName!!
        })


        inst.addTransformer(Transformer(patches))
    }

}