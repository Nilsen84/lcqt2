package io.github.nilsen84.lcqt.cosmetics

import kotlinx.serialization.json.Json
import lcqt.cosmetic.CosmeticService
import lcqt.cosmetic.copy
import lcqt.emote.EmoteService
import lcqt.emote.EmoteService.UseEmoteResponse
import lcqt.emote.copy
import lcqt.util.color
import java.io.File

@Suppress("unused")
object Proxy {
    private val assetDir = File(System.getProperty("user.home"), ".lunarclient/textures/assets/lunar")
    private val json = Json { ignoreUnknownKeys = true }

    private var purchasedCosmetics = listOf<Int>()

    private val cosmeticsIndex = assetDir.resolve("cosmetics/index").readLines().map(CosmeticIndexEntry::fromLine)
    private val emotesIndex = json.decodeFromString<EmoteIndex>(assetDir.resolve("emotes/emotes.json").readText()).emotes

    @JvmStatic
    fun onSend(method: String, contents: ByteArray): ByteArray {
        println("SENT: $method")
        return contents
    }

    @JvmStatic
    fun onReceive(method: String, contents: ByteArray): ByteArray {
        println("RECEIVED: $method")

        when(method) {
            "lunarclient.websocket.cosmetic.v1.CosmeticService.Login" -> {
                return CosmeticService.LoginResponse.parseFrom(contents).copy {
                    purchasedCosmetics = ownedCosmeticIds

                    ownedCosmeticIds.clear()
                    ownedCosmeticIds += cosmeticsIndex.map { it.id }

                    logoColor = color { color = 0xFF55FF }

                    availablePlusColors.clear()
                    availablePlusColors += colorPallete

                    settings = settings.copy {
                        plusColor = colorPallete.elementAt(2)
                    }
                }.toByteArray()
            }
            "lunarclient.websocket.emote.v1.EmoteService.Login" -> {
                return EmoteService.LoginResponse.parseFrom(contents).copy {
                    ownedEmoteIds.clear()
                    ownedEmoteIds += emotesIndex.map { it.id }
                }.toByteArray()
            }
            "lunarclient.websocket.emote.v1.EmoteService.UseEmote" -> {
                val parsed = UseEmoteResponse.parseFrom(contents)
                if(parsed.status == UseEmoteResponse.Status.STATUS_EMOTE_NOT_OWNED) {
                    return parsed.copy {
                        status = UseEmoteResponse.Status.STATUS_OK
                    }.toByteArray()
                }
                return contents
            }
            else -> return contents
        }
    }
}