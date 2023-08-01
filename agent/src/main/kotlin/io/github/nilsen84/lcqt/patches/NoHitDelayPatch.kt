package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.lcqt.Patch
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IntInsnNode

class NoHitDelayPatch : Patch() {
    override fun transform(cn: ClassNode): Boolean {
        if (cn.name != "net/minecraft/client/Minecraft") {
            return false
        }

        val clickMouse = cn.methods.find { it.name == "clickMouse" } ?: return false

        val instructions = clickMouse.instructions
            .filterIsInstance<IntInsnNode>()
            .filter {
                it.opcode == Opcodes.BIPUSH && it.operand == 10 && it.next.let {
                    it is FieldInsnNode && it.name == "leftClickCounter"
                }
            }
            .onEach { it.operand = 0 }

        return instructions.isNotEmpty()
    }
}