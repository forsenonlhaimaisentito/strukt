package org.catafratta.strukt.runtime.impl

import org.catafratta.strukt.runtime.BinarySource
import org.catafratta.strukt.runtime.IncompleteReadException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer

internal class ByteBufferSource(private val buf: ByteBuffer) : BinarySource {
    override fun readByte(): Byte = trapUnderflow { buf.get() }

    override fun readShort(): Short = trapUnderflow { buf.short }

    override fun readChar(): Char = trapUnderflow { buf.char }

    override fun readInt(): Int = trapUnderflow { buf.int }

    override fun readLong(): Long = trapUnderflow { buf.long }

    override fun readFloat(): Float = trapUnderflow { buf.float }

    override fun readDouble(): Double = trapUnderflow { buf.double }

    private fun <T> trapUnderflow(block: () -> T): T {
        try {
            return block()
        } catch (e: BufferUnderflowException) {
            throw IncompleteReadException(e)
        }
    }
}
