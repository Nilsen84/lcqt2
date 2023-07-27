package io.github.nilsen84.lcqt.cosmetics

import io.github.nilsen84.lcqt.LcqtPatcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import lunarapi.cosmetic.CosmeticService
import lunarapi.cosmetic.CosmeticService.UpdateCosmeticSettings
import lunarapi.emote.EmoteService
import lunarapi.emote.EmoteService.UpdateEquippedEmotes
import lunarapi.emote.EmoteService.UseEmoteResponse
import lunarapi.util.Util.Color
import java.io.File
import kotlin.io.encoding.Base64

@Suppress("unused")
object Proxy {
    private val assetDir = File(System.getProperty("user.home"), ".lunarclient/textures/assets/lunar")

    private val cosmeticsIndex = assetDir.resolve("cosmetics/index").readLines().map(CosmeticIndexEntry::fromLine)
    private val emotesIndex = LcqtPatcher.JSON.decodeFromString<EmoteIndex>(
        assetDir.resolve("emotes/emotes.json").readText()
    ).emotes

    private val configFile: File = LcqtPatcher.configFile.resolveSibling("cosmetics.json")
    private val config = runCatching {
        LcqtPatcher.JSON.decodeFromString<Config>(configFile.readText())
    }.getOrDefault(Config())

    @JvmStatic
    fun onSend(method: String, contents: ByteArray): ByteArray {
        println("SENT: $method")
        when(method) {
            "lunarclient.websocket.cosmetic.v1.CosmeticService.UpdateCosmeticSettings" -> {
                val packet = UpdateCosmeticSettings.parseFrom(contents).toBuilder()

                config.equippedCosmetics = packet.settings.activeCosmeticIdsList
                if(packet.settings.hasPlusColor() && packet.settings.plusColor.color > 0) {
                    config.equippedPlusColor = packet.settings.plusColor.color
                }
                config.save()

                packet.settingsBuilder.clearActiveCosmeticIds()
                packet.settingsBuilder.clearPlusColor()

                return packet.build().toByteArray()
            }
            "lunarclient.websocket.emote.v1.EmoteService.UpdateEquippedEmotes" -> {
                val packet = UpdateEquippedEmotes.parseFrom(contents).toBuilder()

                config.equippedEmotes = packet.equippedEmoteIdsList
                config.save()

                packet.clearEquippedEmoteIds()

                return packet.build().toByteArray()
            }
        }
        return contents
    }

    @JvmStatic
    fun onReceive(method: String, contents: ByteArray): ByteArray {
        println("RECEIVED: $method")

        when(method) {
            "lunarclient.websocket.cosmetic.v1.CosmeticService.Login" -> {
                val packet = CosmeticService.LoginResponse.parseFrom(contents).toBuilder()

                packet.clearOwnedCosmeticIds()
                packet.addAllOwnedCosmeticIds(cosmeticsIndex.map { it.id })

                packet.setLogoColor(Color.newBuilder().setColor(0xFF55FF))

                packet.clearAvailablePlusColors()
                packet.addAllAvailablePlusColors(colorPallete)

                val settings = packet.settingsBuilder
                settings.clearActiveCosmeticIds()
                settings.addAllActiveCosmeticIds(config.equippedCosmetics)

                settings.setPlusColor(
                    Color.newBuilder().setColor(
                        config.equippedPlusColor ?: colorPallete[0].color
                    )
                )

                return packet.build().toByteArray()
            }
            "lunarclient.websocket.emote.v1.EmoteService.Login" -> {
                return EmoteService.LoginResponse.parseFrom(contents).toBuilder().apply {
                    clearOwnedEmoteIds()
                    addAllOwnedEmoteIds(emotesIndex.map { it.id })

                    clearEquippedEmoteIds()
                    addAllEquippedEmoteIds(config.equippedEmotes)
                }.build().toByteArray()
            }
            "lunarclient.websocket.emote.v1.EmoteService.UseEmote" -> {
                val packet = UseEmoteResponse.parseFrom(contents)
                if(packet.status == EmoteService.UseEmoteResponse.Status.STATUS_EMOTE_NOT_OWNED) {
                    return packet.toBuilder()
                        .setStatus(UseEmoteResponse.Status.STATUS_OK)
                        .build()
                        .toByteArray()
                }
                return contents;
            }
            else -> return contents
        }
    }

    @Serializable
    private data class Config(
        var equippedEmotes: List<Int> = listOf(),
        var equippedCosmetics: List<Int> = listOf(),
        var equippedPlusColor: Int? = null
    )

    private fun Config.save() {
        configFile.writeText(LcqtPatcher.JSON.encodeToString(this))
    }
}