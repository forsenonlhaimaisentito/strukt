package org.catafratta.strukt.runtime

import org.catafratta.strukt.runtime.impl.OutputStreamBinarySink
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class OutputStreamBinarySinkTest : BinarySinkTest() {
    override fun newConfiguration(input: ByteArray, order: ByteOrder): TestContext.Configuration {
        val stream = ByteArrayOutputStream(input.size)

        return object : TestContext.Configuration {
            override val out: ByteArray get() = stream.toByteArray()
            override val sink: BinarySink = stream.binarySink(order)
            override val inputBuf: ByteBuffer = ByteBuffer.wrap(input).order(order)
        }
    }

    @Test
    fun testDefaultByteOrder() {
        val nativeStream = ByteArrayOutputStream()
        val defaultStream = ByteArrayOutputStream()
        val defaultExtStream = ByteArrayOutputStream()

        val native = nativeStream.binarySink(ByteOrder.nativeOrder())
        val default = OutputStreamBinarySink(defaultStream)
        val defaultExt = defaultExtStream.binarySink()

        native.write(1337L)
        default.write(1337L)
        defaultExt.write(1337L)

        Assert.assertArrayEquals(nativeStream.toByteArray(), defaultStream.toByteArray())
        Assert.assertArrayEquals(nativeStream.toByteArray(), defaultExtStream.toByteArray())
    }
}
