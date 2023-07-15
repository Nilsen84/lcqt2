package io.github.nilsen84.lcqt

import io.github.nilsen84.lcqt.patches.CosmeticsPatch
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain

object Transformer: ClassFileTransformer {
    private val patches = arrayOf(
        CosmeticsPatch(),
    )

    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray
    ): ByteArray? {
        val cn = ClassNode()
        val cr = ClassReader(classfileBuffer)
        cr.accept(cn, 0)

        if(patches.none { it.transform(cn) }) {
            return null
        }

        val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        cn.accept(cw)
        return cw.toByteArray()
    }
}