package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.hasCst
import io.github.nilsen84.lcqt.util.replaceCst
import org.objectweb.asm.tree.ClassNode

class FreelookPatch : Patch() {
    override fun transform(cn: ClassNode): Boolean {
        when {
            cn.hasCst("modSettings", "pinnedServers") -> {
                cn.replaceCst("modSettings", "")
                return true
            }
            "com/lunarclient/bukkitapi/nethandler/client/LCNetHandlerClient" in cn.interfaces -> {
                val handleModSettings = cn.methods.find { it.name == "handleModSettings" }!!
                handleModSettings.instructions = asm {
                    _return
                }
                return true
            }
        }

        return false
    }
}