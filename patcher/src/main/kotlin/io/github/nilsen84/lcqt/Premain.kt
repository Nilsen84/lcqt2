package io.github.nilsen84.lcqt

import java.lang.instrument.Instrumentation

fun premain(opt: String?, inst: Instrumentation) {
    println("INITIALIZING LCQT PATCHER")
    inst.addTransformer(Transformer)
}