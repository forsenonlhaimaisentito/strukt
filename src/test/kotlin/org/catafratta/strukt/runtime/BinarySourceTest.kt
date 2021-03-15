package org.catafratta.strukt.runtime

import org.catafratta.strukt.parseHex
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class BinarySourceTest {
    protected fun newContext(input: ByteArray): TestContext {
        return object : TestContext {
            override val input = input
            override val big = newConfiguration(input, ByteOrder.BIG_ENDIAN)
            override val little = newConfiguration(input, ByteOrder.LITTLE_ENDIAN)
        }
    }

    protected abstract fun newConfiguration(input: ByteArray, order: ByteOrder): TestContext.Configuration

    @Test
    fun testByte() {
        val input = parseHex("00 ff ab cd ef")

        subTest(input) {
            forEachSample(Byte.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.get(), reader.readByte())
            }
        }
    }

    @Test
    fun testShort() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Short.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.short, reader.readShort())
            }
        }
    }

    @Test
    fun testChar() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Char.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.char, reader.readChar())
            }
        }
    }

    @Test
    fun testInt() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Int.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.int, reader.readInt())
            }
        }
    }

    @Test
    fun testLong() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Long.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.long, reader.readLong())
            }
        }
    }

    @Test
    fun testFloat() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Float.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.float, reader.readFloat(), 0f)
            }
        }
    }

    @Test
    fun testDouble() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Double.SIZE_BYTES) { reader, buf ->
                Assert.assertEquals(buf.double, reader.readDouble(), 0.0)
            }
        }
    }

    @Test(expected = IncompleteReadException::class)
    fun testUnexpectedEOF() {
        val input = parseHex("aa bb cc dd")

        subTest(input) {
            big.source.readLong()
        }
    }

    protected inline fun subTest(input: ByteArray, block: TestContext.() -> Unit) = newContext(input).run(block)

    protected inline fun TestContext.forEachSample(
        sampleSize: Int,
        block: (source: BinarySource, buf: ByteBuffer) -> Unit
    ) {
        repeat(input.size / sampleSize) {
            block(big.source, big.inputBuf)
            block(little.source, little.inputBuf)
        }
    }

    protected interface TestContext {
        val input: ByteArray

        val big: Configuration
        val little: Configuration

        interface Configuration {
            val source: BinarySource
            val inputBuf: ByteBuffer
        }
    }
}
