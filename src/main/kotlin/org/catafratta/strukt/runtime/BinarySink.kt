package org.catafratta.strukt.runtime

/**
 * Writes primitives to a buffer or stream of bytes. In case of errors on the underlying resource, exceptions are
 * propagated as-is.
 *
 * This interface is used at run-time to simplify generated code and facilitate future extension.
 */
interface BinarySink {
    fun write(value: Byte)

    fun write(value: Short)

    fun write(value: Char)

    fun write(value: Int)

    fun write(value: Long)

    fun write(value: Float)

    fun write(value: Double)
}
