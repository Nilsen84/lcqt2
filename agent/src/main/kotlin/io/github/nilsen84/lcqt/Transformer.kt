package io.github.nilsen84.lcqt

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import kotlin.system.exitProcess

class Transformer(private val patches: List<Patch>): ClassFileTransformer {
    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray
    ): ByteArray? = runCatching {
        val cn = ClassNode()
        val cr = ClassReader(classfileBuffer)
        cr.accept(cn, 0)

        if(patches.map { it.transform(cn) }.none { it }) {
            return null
        }

        val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        cn.accept(cw)
        return cw.toByteArray()
    }.getOrElse {
        it.printStackTrace()
        exitProcess(1)
    }
}