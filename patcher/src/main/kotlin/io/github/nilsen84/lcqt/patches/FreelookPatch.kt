package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.hasCst
import io.github.nilsen84.lcqt.util.replaceCst
import org.objectweb.asm.tree.ClassNode

class FreelookPatch : Patch() {
    override fun transform(cn: ClassNode): Boolean {
        if(!cn.hasCst("modSettings", "pinnedServers")) return false
        cn.replaceCst("modSettings", "")
        return true
    }
}