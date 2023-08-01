package io.github.nilsen84.lcqt

import org.objectweb.asm.tree.ClassNode

abstract class Patch {
    abstract fun transform(cn: ClassNode): Boolean
}