package org.catafratta.strukt.runtime

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Alias for `read(T::class, source)`.
 *
 * @see Strukt.read
 */
inline fun <reified T : Any> Strukt.read(source: BinarySource): T = read(T::class, source)

/**
 * Alias for `read(T::class, stream.binarySource(order))`.
 *
 * @param stream The stream to read from.
 * @param order The byte order to use.
 * @return The decoded value.
 *
 * @see binarySource
 */
inline fun <reified T : Any> Strukt.read(stream: InputStream, order: ByteOrder): T =
    read(T::class, stream.binarySource(order))

/**
 * Alias for `read(T::class, buf.binarySource())`.
 *
 * @param buf The buffer to read from.
 * @return The decoded value.
 *
 * @see binarySource
 */
inline fun <reified T : Any> Strukt.read(buf: ByteBuffer): T = read(T::class, buf.binarySource())


/**
 * Alias for `write(value, stream.binarySink(order))`.
 *
 * @param stream The stream to write to.
 * @param order The byte order to use.
 * @see binarySink
 */
fun <T : Any> Strukt.write(value: T, stream: OutputStream, order: ByteOrder) {
    write(value, stream.binarySink(order))
}

/**
 * Alias for `write(value, buf.binarySink())`.
 *
 * @param value The value to write.
 * @param buf The buffer to write to.
 * @see binarySink
 */
fun <T : Any> Strukt.write(value: T, buf: ByteBuffer) {
    write(value, buf.binarySink())
}
