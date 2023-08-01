package io.github.nilsen84.lcqt

import io.github.nilsen84.lcqt.patches.ClassloaderPatch
import io.github.nilsen84.lcqt.patches.CosmeticsPatch
import io.github.nilsen84.lcqt.patches.CrackedAccountPatch
import io.github.nilsen84.lcqt.patches.FreelookPatch
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.instrument.Instrumentation

object LcqtPatcher {
    val JSON = Json { ignoreUnknownKeys = true; prettyPrint = true }
    lateinit var configFile: File private set
    lateinit var config: Config private set

    @JvmStatic
    fun premain(configPath: String, inst: Instrumentation) {
        configFile = File(configPath)
        config = JSON.decodeFromString<Config>(File(configPath).readText())

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