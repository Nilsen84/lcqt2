package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.internalNameOf
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LabelNode

class ClassloaderPatch: Patch() {
    companion object {
        @JvmStatic
        fun shouldLoadInSCL(name: String): Boolean {
            return name.startsWith("io.github.nilsen84.lcqt.") &&
                    !name.startsWith("io.github.nilsen84.lcqt.minecraft.")
        }
    }

    override fun transform(cn: ClassNode): Boolean {
        if(cn.superName !in arrayOf("java/lang/ClassLoader", "java/net/URLClassLoader")) return false
        val loadClass = cn.methods.find { it.name == "loadClass" && it.desc == "(Ljava/lang/String;Z)Ljava/lang/Class;" } ?: return false

        loadClass.instructions.insert(asm {
            val end = LabelNode()

            aload(1)
            invokestatic(internalNameOf<ClassloaderPatch>(), "shouldLoadInSCL", "(Ljava/lang/String;)Z")
            ifeq(end)

            aload(0)
            aload(1)
            iload(2)
            invokespecial(
                cn.superName,
                "loadClass",
                "(Ljava/lang/String;Z)Ljava/lang/Class;"
            )
            areturn

            +end
            f_same()
        })

        return true
    }
}