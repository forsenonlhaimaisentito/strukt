package org.catafratta.strukt.runtime

/**
 * A source of primitives that reads from a buffer or stream of bytes. In case of unexpected EOF or underflow,
 * its methods throw [IncompleteReadException].
 *
 * This interface is used at run-time to simplify generated code and facilitate future extension.
 */
interface BinarySource {
    fun readByte(): Byte

    fun readShort(): Short

    fun readChar(): Char

    fun readInt(): Int

    fun readLong(): Long

    fun readFloat(): Float

    fun readDouble(): Double
}
