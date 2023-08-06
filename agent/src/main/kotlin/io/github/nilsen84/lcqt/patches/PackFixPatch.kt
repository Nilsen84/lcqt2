package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.hasCst
import org.objectweb.asm.tree.ClassNode

class PackFixPatch: Patch() {
    override fun transform(cn: ClassNode): Boolean {
        val mn = cn.methods.find {
            it.desc == "(Ljava/lang/String;)Z" && it.hasCst("assets/lunar/")
        } ?: return false

        mn.instructions.insert(asm {
            iconst_1
            ireturn
            f_same()
        })

        return true
    }
}