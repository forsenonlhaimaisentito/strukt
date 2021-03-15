package org.catafratta.strukt.runtime

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class ByteBufferSinkTest : BinarySinkTest() {
    override fun newConfiguration(input: ByteArray, order: ByteOrder): TestContext.Configuration {
        return object : TestContext.Configuration {
            override val out: ByteArray = ByteArray(input.size)
            override val sink: BinarySink = ByteBuffer.wrap(out).order(order).binarySink()
            override val inputBuf: ByteBuffer = ByteBuffer.wrap(input).order(order)
        }
    }
}
