package io.github.nilsen84.lcqt.util

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

fun ClassNode.hasAccess(acc: Int) = access and acc != 0
fun MethodNode.hasAccess(acc: Int) = access and acc != 0
fun FieldNode.hasAccess(acc: Int) = access and acc != 0

fun ClassNode.addAccess(acc: Int) { access = access or acc }
fun MethodNode.addAccess(acc: Int) { access = access or acc }
fun FieldNode.addAccess(acc: Int) { access = access or acc }

fun ClassNode.removeAccess(acc: Int) { access = access and acc.inv() }
fun MethodNode.removeAccess(acc: Int) { access = access and acc.inv() }
fun FieldNode.removeAccess(acc: Int) { access = access and acc.inv() }