package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.hasCst
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

class FPSSpoofPatch: Patch() {
    override fun transform(cn: ClassNode): Boolean {
        if(!cn.hasCst("[1466 FPS]")) return false

        for(mn in cn.methods) {
            for (insn in mn.instructions) {
                if (insn is MethodInsnNode && insn.name == "bridge\$getDebugFPS") {
                    mn.instructions.insert(insn, asm {
                        i2f
                        invokestatic("io/github/nilsen84/lcqt/LcqtPatcher", "getConfig", "()Lio/github/nilsen84/lcqt/Config;")
                        invokevirtual("io/github/nilsen84/lcqt/Config", "getFpsSpoofMultiplier", "()F")
                        fmul
                        f2i
                    })
                }
            }
        }

        return true
    }
}