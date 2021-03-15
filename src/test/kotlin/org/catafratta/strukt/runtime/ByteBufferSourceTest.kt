package org.catafratta.strukt.runtime

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class ByteBufferSourceTest : BinarySourceTest() {
    // This class tests the ByteBuffer wrapper, for BinaryReader tests see BinaryReaderTest

    override fun newConfiguration(input: ByteArray, order: ByteOrder): TestContext.Configuration {
        return object : TestContext.Configuration {
            override val source = ByteBuffer.wrap(input).order(order).binarySource()
            override val inputBuf = ByteBuffer.wrap(input).order(order)
        }
    }
}
