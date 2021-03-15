package org.catafratta.strukt.runtime.impl

import org.catafratta.strukt.runtime.BinarySink
import java.nio.ByteBuffer

internal class ByteBufferSink(private val buf: ByteBuffer) : BinarySink {
    override fun write(value: Byte) {
        buf.put(value)
    }

    override fun write(value: Short) {
        buf.putShort(value)
    }

    override fun write(value: Char) {
        buf.putChar(value)
    }

    override fun write(value: Int) {
        buf.putInt(value)
    }

    override fun write(value: Long) {
        buf.putLong(value)
    }

    override fun write(value: Float) {
        buf.putFloat(value)
    }

    override fun write(value: Double) {
        buf.putDouble(value)
    }
}
