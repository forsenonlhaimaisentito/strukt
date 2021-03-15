package org.catafratta.strukt.runtime

import org.catafratta.strukt.parseHex
import org.catafratta.strukt.runtime.impl.InputStreamBinarySource
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class InputStreamBinarySourceTest : BinarySourceTest() {
    // This class tests the InputStream wrapper, for BinaryReader tests see BinaryReaderTest

    override fun newConfiguration(input: ByteArray, order: ByteOrder): TestContext.Configuration {
        return object : TestContext.Configuration {
            override val source = input.inputStream().binarySource(order)
            override val inputBuf = ByteBuffer.wrap(input).order(order)
        }
    }

    @Test
    fun testDefaultByteOrder() {
        val input = parseHex("aa bb cc dd aa bb cc dd")

        val native = input.inputStream().binarySource(ByteOrder.nativeOrder())
        val default = InputStreamBinarySource(input.inputStream())
        val defaultExt = input.inputStream().binarySource()

        Assert.assertEquals(native.readInt(), default.readInt())
        Assert.assertEquals(native.readInt(), defaultExt.readInt())
    }
}
