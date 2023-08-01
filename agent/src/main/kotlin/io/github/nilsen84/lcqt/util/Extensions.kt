package io.github.nilsen84.lcqt.util

import io.github.nilsen84.bytecode_dsl.InsnBuilder
import io.github.nilsen84.bytecode_dsl.visitAsm
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.File

inline fun <reified T> Any.cast() = this as T

fun ClassNode.generateMethod(
    name: String,
    desc: String,
    acc: Int = Opcodes.ACC_PUBLIC,
    bytecode: InsnBuilder.() -> Unit
) {
    visitMethod(acc, name, desc, null, null).visitAsm(bytecode)
}

fun ClassNode.hasCst(vararg cst: Any): Boolean {
    return methods.flatMap { it.instructions }
        .filterIsInstance<LdcInsnNode>()
        .map { it.cst }
        .containsAll(cst.toList())
}

fun MethodNode.hasCst(vararg cst: Any): Boolean {
    return instructions
        .filterIsInstance<LdcInsnNode>()
        .map { it.cst }
        .containsAll(cst.toList())
}

fun ClassNode.replaceCst(old: Any, new: Any) {
    methods.forEach { it.replaceCst(old, new) }
}

fun MethodNode.replaceCst(old: Any, new: Any) {
    instructions
        .filterIsInstance<LdcInsnNode>()
        .forEach { if(it.cst == old) it.cst = new }
}

fun MethodNode.findCst(cst: Any): AbstractInsnNode? =
    instructions.find { it is LdcInsnNode && it.cst == cst }

internal inline fun <reified T : AbstractInsnNode> AbstractInsnNode.next(p: (T) -> Boolean = { true }): T? {
    return generateSequence(next) { it.next }.filterIsInstance<T>().find(p)
}

internal inline fun <reified T : AbstractInsnNode> AbstractInsnNode.prev(p: (T) -> Boolean = { true }): T? {
    return generateSequence(previous) { it.previous }.filterIsInstance<T>().find(p)
}

fun ClassNode.dump(fileName: String) {
    val cw = ClassWriter(0)
    accept(cw)
    File(fileName).writeBytes(cw.toByteArray())
}

fun InsnBuilder.makeConcatWithConstants(
    desc: String,
    format: String
) = invokedynamic(
    "makeConcatWithConstants",
    desc,
    Handle(
        Opcodes.H_INVOKESTATIC,
        "java/lang/invoke/StringConcatFactory",
        "makeConcatWithConstants",
        "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
        false
    ),
    format
)