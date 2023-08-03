package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.hasAccess
import io.github.nilsen84.lcqt.util.hasCst
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode

class DebugModsPatch: Patch() {
    override fun transform(cn: ClassNode): Boolean {
        when {
            cn.hasCst("mods.json") -> {
                val getMods = cn.methods.find { it.desc == "()Ljava/util/Set;" } ?: return false
                return getMods.instructions
                    .filter { it.opcode == Opcodes.IFNE }
                    .onEach { getMods.instructions.set(it, InsnNode(Opcodes.POP)) }
                    .isNotEmpty()
            }
            else -> {
                val isStaffModClass = cn.methods.any {
                    it.hasAccess(Opcodes.ACC_SYNTHETIC) &&
                            it.desc == "(L${cn.name};)V" &&
                            it.instructions
                                .filterIsInstance<MethodInsnNode>()
                                .any { it.name == "bridge\$reloadChunks" }
                }

                if(!isStaffModClass) return false

                cn.methods.find { it.desc == "()Z" }!!.instructions = asm {
                    iconst_1
                    ireturn
                }

                return true
            }
        }
    }
}