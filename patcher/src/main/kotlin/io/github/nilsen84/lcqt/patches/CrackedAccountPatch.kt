package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.hasCst
import org.objectweb.asm.tree.ClassNode

class CrackedAccountPatch: Patch() {
    override fun transform(cn: ClassNode): Boolean {
        if(!cn.hasCst("launcher_accounts.json")) return false

        val canPlayOnline = cn.methods.reversed()
            .dropWhile { !it.hasCst("Accounts") }
            .first { it.desc == "()Z" }

        canPlayOnline.instructions = asm {
            iconst_1
            ireturn
        }

        return true
    }
}