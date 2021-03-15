package org.catafratta.strukt.runtime.impl

import org.catafratta.strukt.runtime.BinarySink
import java.io.OutputStream
import java.nio.ByteOrder

internal class OutputStreamBinarySink(
    private val stream: OutputStream,
    private val order: ByteOrder = ByteOrder.nativeOrder()
) : BinarySink {
    override fun write(value: Byte) {
        stream.write(value.toInt())
    }

    override fun write(value: Short) {
        val intValue = value.toInt()
        val lsb = ByteArray(Short.SIZE_BYTES) { intValue.ushr(8 * it).and(0xFF).toByte() }
        writeLsbInteger(lsb)
    }

    override fun write(value: Char) {
        write(value.toShort())
    }

    override fun write(value: Int) {
        val lsb = ByteArray(Int.SIZE_BYTES) { value.ushr(8 * it).and(0xFF).toByte() }
        writeLsbInteger(lsb)
    }

    override fun write(value: Long) {
        val lsb = ByteArray(Long.SIZE_BYTES) { value.ushr(8 * it).and(0xFF).toByte() }
        writeLsbInteger(lsb)
    }

    override fun write(value: Float) {
        val bits = value.toRawBits()
        val lsb = ByteArray(Float.SIZE_BYTES) { bits.ushr(8 * it).and(0xFF).toByte() }
        writeLsbInteger(lsb)
    }

    override fun write(value: Double) {
        val bits = value.toRawBits()
        val lsb = ByteArray(Double.SIZE_BYTES) { bits.ushr(8 * it).and(0xFF).toByte() }
        writeLsbInteger(lsb)
    }

    /**
     * Writes the input LSB bytes according to this writer's byte order.
     */
    private fun writeLsbInteger(bytes: ByteArray) {
        val indices = if (order == ByteOrder.LITTLE_ENDIAN) bytes.indices else bytes.indices.reversed()

        indices.forEach { stream.write(bytes[it].toInt()) }
    }
}
