package io.github.nilsen84.lcqt.patches

import io.github.nilsen84.bytecode_dsl.asm
import io.github.nilsen84.lcqt.Patch
import io.github.nilsen84.lcqt.util.*
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class CosmeticsPatch : Patch() {
    private lateinit var channelClassName: String

    override fun transform(cn: ClassNode): Boolean {
        when {
            "com/google/protobuf/RpcChannel" in cn.interfaces -> {
                channelClassName = cn.name

                val respMethod = cn.methods.find { it.hasCst("Failed to decode response") }!!
                hookResponseMethod(cn, respMethod)

                val sendMethod = cn.methods.find { it.name == "callMethod" }!!
                hookSendMethod(cn, sendMethod)
                return true
            }

            ::channelClassName.isInitialized && cn.name.startsWith("$channelClassName$") -> {
                val nameField = cn.fields.find { it.desc == "Ljava/lang/String;" }!!
                cn.generateMethod("lcqt_getMethod", "()Ljava/lang/String;") {
                    aload(0)
                    getfield(cn.name, nameField.name, "Ljava/lang/String;")
                    areturn
                }
                return true
            }
        }

        return false
    }

    private fun hookSendMethod(cn: ClassNode, mn: MethodNode) {
        val toByteString = mn.instructions.first.next<MethodInsnNode> {
            it.name == "toByteString"
        }!!

        mn.instructions.insert(toByteString, asm {
            invokevirtual("com/google/protobuf/ByteString", "toByteArray", "()[B")

            aload(1)
            invokevirtual("com/google/protobuf/Descriptors\$MethodDescriptor", "getService", "()Lcom/google/protobuf/Descriptors\$ServiceDescriptor;")
            invokevirtual("com/google/protobuf/Descriptors\$ServiceDescriptor", "getFullName", "()Ljava/lang/String;")

            aload(1)
            invokevirtual("com/google/protobuf/Descriptors\$MethodDescriptor", "getName", "()Ljava/lang/String;")

            makeConcatWithConstants("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", "\u0001.\u0001")

            swap

            invokestatic("io/github/nilsen84/lcqt/cosmetics/Proxy", "onSend", "(Ljava/lang/String;[B)[B")
            invokestatic("com/google/protobuf/ByteString", "copyFrom", "([B)Lcom/google/protobuf/ByteString;")
        })
    }

    private fun hookResponseMethod(cn: ClassNode, mn: MethodNode) {
        val (requestClass, requestIdx) = mn.instructions.windowed(2).find { (a, b) ->
            a.opcode == CHECKCAST && (a as TypeInsnNode).desc.startsWith("${cn.name}$") &&
                    b.opcode == ASTORE
        }!!.let {
            it.first().cast<TypeInsnNode>().desc to it.last().cast<VarInsnNode>().`var`
        }

        val mergeFrom = mn.instructions.find { it is MethodInsnNode && it.name == "mergeFrom" }!!

        mn.instructions.insertBefore(mergeFrom, asm {
            invokevirtual("com/google/protobuf/ByteString", "toByteArray", "()[B")

            aload(requestIdx)
            invokevirtual(requestClass, "lcqt_getMethod", "()Ljava/lang/String;")

            swap

            invokestatic("io/github/nilsen84/lcqt/cosmetics/Proxy", "onReceive", "(Ljava/lang/String;[B)[B")

            invokestatic("com/google/protobuf/ByteString", "copyFrom", "([B)Lcom/google/protobuf/ByteString;")
        })
    }
}