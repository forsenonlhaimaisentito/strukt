package org.catafratta.strukt.runtime

import org.catafratta.strukt.SimpleStruct
import org.catafratta.strukt.TestStruct
import org.catafratta.strukt.common.CodecNamingStrategy
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass

class DefaultStruktTest {
    @Test
    fun testReadWrite() {
        val loader = MockClassLoader()
        loader.addMockCodec(SimpleStructCodec::class)
        loader.addMockCodec(MemberStructCodec::class)
        loader.classes[SimpleStructCodec::class.java.name] = SimpleStructCodec::class.java

        val strukt = Strukt.Builder().classLoader(loader).build()

        val data = SimpleStruct.MemberStruct(SimpleStruct(1234))
        teststrukt(strukt, data)
    }

    private inline fun <reified T : TestStruct> teststrukt(strukt: Strukt, data: T) {
        teststruktBuffers(strukt, data)
        teststruktStreams(strukt, data)
    }

    private inline fun <reified T : TestStruct> teststruktBuffers(strukt: Strukt, data: T) {
        val expectedBuf = data.binaryRepresentation(ByteOrder.LITTLE_ENDIAN)

        val decoded = strukt.read<T>(expectedBuf)
        Assert.assertEquals(data, decoded)

        val buf = ByteBuffer.allocate(data.encodedSize).order(expectedBuf.order())
        strukt.write(data, buf)
        Assert.assertArrayEquals(expectedBuf.array(), buf.array())
    }

    private inline fun <reified T : TestStruct> teststruktStreams(strukt: Strukt, data: T) {
        val expected = data.binaryRepresentation(ByteOrder.LITTLE_ENDIAN).array()

        val decoded = strukt.read<T>(expected.inputStream(), ByteOrder.LITTLE_ENDIAN)
        Assert.assertEquals(data, decoded)

        val encoded = ByteArrayOutputStream(expected.size).use {
            strukt.write(data, it, ByteOrder.LITTLE_ENDIAN)
            it.toByteArray()
        }
        Assert.assertArrayEquals(expected, encoded)
    }

    private inline fun <reified T : Any> MockClassLoader.addMockCodec(codec: KClass<out Codec<T>>) {
        classes[CodecNamingStrategy.fullNameFor(T::class)] = codec.java
    }

    private class SimpleStructCodec(@Suppress("unused") private val strukt: Strukt) : Codec<SimpleStruct> {
        override fun read(source: BinarySource) = SimpleStruct(source.readInt())

        override fun write(value: SimpleStruct, sink: BinarySink) {
            sink.write(value.field)
        }
    }

    private class MemberStructCodec(private val strukt: Strukt) : Codec<SimpleStruct.MemberStruct> {
        override fun read(source: BinarySource) = SimpleStruct.MemberStruct(strukt.read(source))

        override fun write(value: SimpleStruct.MemberStruct, sink: BinarySink) {
            strukt.write(value.field, sink)
        }
    }
}
