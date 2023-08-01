package io.github.nilsen84.lcqt.cosmetics

class CosmeticIndexEntry(val id: Int, val name: String) {
    companion object {
        fun fromLine(line: String): CosmeticIndexEntry {
            val split = line.split(',')
            return CosmeticIndexEntry(split[0].toInt(), split[3])
        }
    }
}