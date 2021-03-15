package org.catafratta.strukt.runtime

import org.catafratta.strukt.runtime.impl.ByteBufferSink
import org.catafratta.strukt.runtime.impl.ByteBufferSource
import org.catafratta.strukt.runtime.impl.InputStreamBinarySource
import org.catafratta.strukt.runtime.impl.OutputStreamBinarySink
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Creates a [BinarySource] from the given [InputStream].
 *
 * @param order The resulting source's byte order, defaults to native byte order.
 */
fun InputStream.binarySource(order: ByteOrder = ByteOrder.nativeOrder()): BinarySource =
    InputStreamBinarySource(this, order)

/**
 * Creates a [BinarySink] from the given [OutputStream].
 *
 * @param order The resulting sink's byte order, defaults to native byte order.
 */
fun OutputStream.binarySink(order: ByteOrder = ByteOrder.nativeOrder()): BinarySink =
    OutputStreamBinarySink(this, order)

/**
 * Creates a [BinarySource] from the given [ByteBuffer], using the buffer's byte order.
 */
fun ByteBuffer.binarySource(): BinarySource = ByteBufferSource(this)

/**
 * Creates a [BinarySink] from the given [ByteBuffer], using the buffer's byte order.
 */
fun ByteBuffer.binarySink(): BinarySink = ByteBufferSink(this)
