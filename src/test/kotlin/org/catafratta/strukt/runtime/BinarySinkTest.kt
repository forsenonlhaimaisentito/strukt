package org.catafratta.strukt.runtime

import org.catafratta.strukt.parseHex
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class BinarySinkTest {
    protected fun newContext(input: ByteArray): TestContext {
        return object : TestContext {
            override val input: ByteArray = input
            override val big: TestContext.Configuration = newConfiguration(input, ByteOrder.BIG_ENDIAN)
            override val little: TestContext.Configuration = newConfiguration(input, ByteOrder.LITTLE_ENDIAN)
        }
    }

    protected abstract fun newConfiguration(input: ByteArray, order: ByteOrder): TestContext.Configuration

    @Test
    fun testByte() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Byte.SIZE_BYTES) { writer, buf -> writer.write(buf.get()) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    @Test
    fun testShort() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Short.SIZE_BYTES) { writer, buf -> writer.write(buf.short) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    @Test
    fun testChar() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Char.SIZE_BYTES) { writer, buf -> writer.write(buf.char) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    @Test
    fun testInt() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Int.SIZE_BYTES) { writer, buf -> writer.write(buf.int) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    @Test
    fun testLong() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Long.SIZE_BYTES) { writer, buf -> writer.write(buf.long) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    @Test
    fun testFloat() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Float.SIZE_BYTES) { writer, buf -> writer.write(buf.float) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    @Test
    fun testDouble() {
        val input = parseHex("ff 00 ff 00  ff 00 ff ff  aa bb cc dd  ff ee dd cc")

        subTest(input) {
            forEachSample(Double.SIZE_BYTES) { writer, buf -> writer.write(buf.double) }

            Assert.assertArrayEquals(big.out, input)
            Assert.assertArrayEquals(little.out, input)
        }
    }

    protected inline fun subTest(input: ByteArray, block: TestContext.() -> Unit) = newContext(input).run(block)

    protected inline fun TestContext.forEachSample(
        sampleSize: Int,
        block: (sink: BinarySink, buf: ByteBuffer) -> Unit
    ) {
        repeat(input.size / sampleSize) {
            block(big.sink, big.inputBuf)
            block(little.sink, little.inputBuf)
        }
    }

    protected interface TestContext {
        val input: ByteArray

        val big: Configuration
        val little: Configuration

        interface Configuration {
            val sink: BinarySink
            val inputBuf: ByteBuffer
            val out: ByteArray
        }
    }
}
