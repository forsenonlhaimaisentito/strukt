package org.catafratta.strukt

import org.catafratta.strukt.processor.StructDef
import org.catafratta.strukt.processor.mockElement
import org.catafratta.strukt.runtime.Codec
import org.catafratta.strukt.runtime.binarySink
import org.catafratta.strukt.runtime.binarySource
import org.junit.Assert
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.lang.model.element.ElementKind


internal object TestStructs {
    val allDefinitions = listOf(
        AllPrimitivesStruct.PARSED,
        SimpleStruct.PARSED,
        SimpleStruct.MemberStruct.PARSED,
        NestedStruct.PARSED
    )
}

fun <T : TestStruct> testCodec(codec: Codec<T>, value: T) {
    testCodec(codec, value, ByteOrder.BIG_ENDIAN)
    testCodec(codec, value, ByteOrder.LITTLE_ENDIAN)
}

fun <T : TestStruct> testCodec(codec: Codec<T>, value: T, order: ByteOrder) {
    val expectedBuf = value.binaryRepresentation(order)
    val codecBuf = ByteBuffer.allocate(value.encodedSize).order(order)

    codec.write(value, codecBuf.binarySink())
    Assert.assertArrayEquals(expectedBuf.array(), codecBuf.array())

    val decoded = codec.read(expectedBuf.binarySource())
    Assert.assertEquals(value, decoded)
}

interface TestStruct {
    val encodedSize: Int

    fun binaryRepresentation(order: ByteOrder): ByteBuffer
}

@Struct
data class AllPrimitivesStruct(
    val byte: Byte,
    val short: Short,
    val char: Char,
    val int: Int,
    val long: Long,
    val float: Float,
    val double: Double
) : TestStruct {
    override val encodedSize: Int = 29

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .put(byte)
            .putShort(short)
            .putChar(char)
            .putInt(int)
            .putLong(long)
            .putFloat(float)
            .putDouble(double)
            .position(0)
    }

    companion object {
        internal val PARSED = StructDef(
            AllPrimitivesStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.Primitive("byte", "kotlin/Byte"),
                StructDef.Field.Primitive("short", "kotlin/Short"),
                StructDef.Field.Primitive("char", "kotlin/Char"),
                StructDef.Field.Primitive("int", "kotlin/Int"),
                StructDef.Field.Primitive("long", "kotlin/Long"),
                StructDef.Field.Primitive("float", "kotlin/Float"),
                StructDef.Field.Primitive("double", "kotlin/Double"),
            ),
            mockElement(ElementKind.CLASS) {}
        )
    }
}

@Struct
data class SimpleStruct(val field: Int) : TestStruct {
    override val encodedSize: Int = 4

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .putInt(field)
            .position(0)
    }

    @Struct
    data class MemberStruct(val field: SimpleStruct) : TestStruct {
        override val encodedSize: Int = field.encodedSize

        override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
            return field.binaryRepresentation(order)
        }

        companion object {
            internal val PARSED = StructDef(
                MemberStruct::class.qualifiedName!!.replace('.', '/'),
                listOf(
                    StructDef.Field.Object("field", SimpleStruct.PARSED.name)
                ),
                mockElement(ElementKind.CLASS) {}
            )
        }
    }

    companion object {
        internal val PARSED = StructDef(
            SimpleStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.Primitive("field", "kotlin/Int")
            ),
            mockElement(ElementKind.CLASS) {}
        )
    }
}

@Struct
data class NestedStruct(
    val field: Long,
    val child: SimpleStruct.MemberStruct
) : TestStruct {
    override val encodedSize: Int = 8 + child.encodedSize

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .putLong(field)
            .put(child.binaryRepresentation(order))
            .position(0)
    }

    companion object {
        internal val PARSED = StructDef(
            NestedStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.Primitive("field", "kotlin/Long"),
                StructDef.Field.Object("child", SimpleStruct.PARSED.name)
            ),
            mockElement(ElementKind.CLASS) {}
        )
    }
}
