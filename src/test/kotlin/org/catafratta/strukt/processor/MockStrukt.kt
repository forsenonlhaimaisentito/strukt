package org.catafratta.strukt.processor

import org.catafratta.strukt.runtime.BinarySink
import org.catafratta.strukt.runtime.BinarySource
import org.catafratta.strukt.runtime.Codec
import org.catafratta.strukt.runtime.Strukt
import kotlin.reflect.KClass

class MockStrukt : Strukt {
    val codecs = mutableMapOf<KClass<*>, Codec<*>>()

    override fun <T : Any> read(klass: KClass<T>, source: BinarySource): T = findCodec(klass).read(source)

    override fun <T : Any> write(value: T, sink: BinarySink) = findCodec(value::class).write(value, sink)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> findCodec(klass: KClass<out T>): Codec<T> = codecs.getValue(klass) as Codec<T>
}
