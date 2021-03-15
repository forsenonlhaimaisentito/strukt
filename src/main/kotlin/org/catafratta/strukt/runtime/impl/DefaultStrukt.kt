package org.catafratta.strukt.runtime.impl

import org.catafratta.strukt.common.CodecNamingStrategy
import org.catafratta.strukt.runtime.BinarySink
import org.catafratta.strukt.runtime.BinarySource
import org.catafratta.strukt.runtime.Codec
import org.catafratta.strukt.runtime.Strukt
import kotlin.reflect.KClass

internal class DefaultStrukt(
    private val classLoader: ClassLoader
) : Strukt {
    private val codecs = mutableMapOf<KClass<*>, Codec<*>>()


    override fun <T : Any> read(klass: KClass<T>, source: BinarySource): T {
        return findCodec(klass).read(source)
    }

    override fun <T : Any> write(value: T, sink: BinarySink) {
        findCodec(value::class).write(value, sink)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> findCodec(klass: KClass<out T>): Codec<T> {
        return codecs.getOrPut(klass) { loadCodec(klass) } as Codec<T>
    }

    // TODO(design): Should this be thread-safe?
    private fun loadCodec(klass: KClass<*>): Codec<*> {
        val clazz = classLoader.loadClass(CodecNamingStrategy.fullNameFor(klass))
        val ctor = clazz.getDeclaredConstructor(Strukt::class.java)
        ctor.isAccessible = true
        val instance = ctor.newInstance(this)

        return instance as Codec<*>
    }
}
