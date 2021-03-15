package org.catafratta.strukt.runtime

/**
 * A Codec reads and writes structs of a specific type.
 */
interface Codec<T : Any> {
    /**
     * Reads an instance of `T` from `source`.
     */
    fun read(source: BinarySource): T

    /**
     * Writes `value` to `sink`.
     */
    fun write(value: T, sink: BinarySink)
}
