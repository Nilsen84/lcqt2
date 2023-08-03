package io.github.nilsen84.lcqt

import kotlinx.serialization.Serializable

@Serializable
class Config {
    val cosmeticsEnabled: Boolean = false
    val freelookEnabled: Boolean = false
    val crackedEnabled: Boolean = false
    val noHitDelayEnabled: Boolean = false
    val debugModsEnabled: Boolean = false

    val fpsSpoofEnabled: Boolean = false
    val fpsSpoofMultiplier: Float = 1.0f
}