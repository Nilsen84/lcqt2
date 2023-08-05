package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.dump
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class RawInputPatch: Patch() {
    override fun transform(cn: ClassNode): Boolean {
        if(cn.name != "net/minecraft/client/Minecraft") return false
        if(cn.fields.none { it.name == "mouseHelper" && it.desc == "Lnet/minecraft/util/MouseHelper;" }) return false
        val startGame = cn.methods.find { it.name == "startGame" } ?: return false

        startGame.instructions.insertBefore(
            startGame.instructions.findLast { it.opcode == Opcodes.RETURN },
            asm {
                aload(0)
                new("io/github/nilsen84/lcqt/minecraft/RawMouseHelper")
                dup
                invokespecial("io/github/nilsen84/lcqt/minecraft/RawMouseHelper", "<init>", "()V")
                putfield(cn.name, "mouseHelper", "Lnet/minecraft/util/MouseHelper;")
            }
        )

        return true
    }
}