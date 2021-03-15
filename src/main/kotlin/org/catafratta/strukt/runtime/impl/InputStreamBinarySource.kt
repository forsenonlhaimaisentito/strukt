package org.catafratta.strukt.runtime.impl

import org.catafratta.strukt.runtime.BinarySource
import org.catafratta.strukt.runtime.IncompleteReadException
import java.io.InputStream
import java.nio.ByteOrder

internal class InputStreamBinarySource(
    private val stream: InputStream,
    private val order: ByteOrder = ByteOrder.nativeOrder()
) : BinarySource {
    override fun readByte(): Byte = readOne().toByte()

    override fun readShort(): Short {
        val b = readBytesInOrder(Short.SIZE_BYTES)
        return ((b[1] shl 8) or b[0]).toShort()
    }

    override fun readChar(): Char = readShort().toChar()

    override fun readInt(): Int = readBytesInOrder(Int.SIZE_BYTES)
        .foldIndexed(0) { i, acc, b -> acc or (b shl i * 8) }

    override fun readLong(): Long = readBytesInOrder(Long.SIZE_BYTES)
        .foldIndexed(0L) { i, acc, b -> acc or b.toLong().shl(i * 8) }

    override fun readFloat(): Float = Float.fromBits(readInt())

    override fun readDouble(): Double = Double.fromBits(readLong())

    /**
     * Reads `n` bytes from the underlying stream and returns them in LSB order.
     */
    private fun readBytesInOrder(n: Int): IntArray {
        val out = IntArray(n)

        val indices = if (order == ByteOrder.LITTLE_ENDIAN) out.indices else out.indices.reversed()

        indices.forEach { out[it] = readOne() }

        return out
    }

    private fun readOne(): Int = stream.read().also { if (it == -1) throw IncompleteReadException() }
}
