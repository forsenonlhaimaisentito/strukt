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
import kotlin.random.Random


internal object TestStructs {
    val allDefinitions = listOf(
        AllPrimitivesStruct.PARSED,
        SimpleStruct.PARSED,
        SimpleStruct.MemberStruct.PARSED,
        NestedStruct.PARSED,
        WeirdNameStruct.PARSED,
        AllPrimitiveArraysStruct.PARSED,
        ObjectArrayStruct.PARSED,
        NestedObjectArrayStruct.PARSED
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

private val <T : TestStruct> Array<T>.encodedSize inline get() = fold(0) { acc, item -> acc + item.encodedSize }

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

@Struct
data class WeirdNameStruct(
    val `weird name`: Int
) : TestStruct {
    override val encodedSize: Int = 4

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .putInt(`weird name`)
            .position(0)
    }

    companion object {
        internal val PARSED = StructDef(
            WeirdNameStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.Primitive("weird name", "kotlin/Int")
            ),
            mockElement(ElementKind.CLASS) {}
        )
    }
}

@Struct
data class AllPrimitiveArraysStruct(
    @FixedSize(1)
    val bytes: ByteArray,
    @FixedSize(2)
    val shorts: ShortArray,
    @FixedSize(3)
    val chars: CharArray,
    @FixedSize(4)
    val ints: IntArray,
    @FixedSize(5)
    val longs: LongArray,
    @FixedSize(6)
    val floats: FloatArray,
    @FixedSize(7)
    val doubles: DoubleArray
) : TestStruct {
    override val encodedSize: Int = 147

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .apply {
                bytes.forEach { put(it) }
                shorts.forEach { putShort(it) }
                chars.forEach { putChar(it) }
                ints.forEach { putInt(it) }
                longs.forEach { putLong(it) }
                floats.forEach { putFloat(it) }
                doubles.forEach { putDouble(it) }
            }
            .position(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AllPrimitiveArraysStruct

        if (!bytes.contentEquals(other.bytes)) return false
        if (!shorts.contentEquals(other.shorts)) return false
        if (!chars.contentEquals(other.chars)) return false
        if (!ints.contentEquals(other.ints)) return false
        if (!longs.contentEquals(other.longs)) return false
        if (!floats.contentEquals(other.floats)) return false
        if (!doubles.contentEquals(other.doubles)) return false
        if (encodedSize != other.encodedSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + shorts.contentHashCode()
        result = 31 * result + chars.contentHashCode()
        result = 31 * result + ints.contentHashCode()
        result = 31 * result + longs.contentHashCode()
        result = 31 * result + floats.contentHashCode()
        result = 31 * result + doubles.contentHashCode()
        result = 31 * result + encodedSize
        return result
    }

    companion object {
        internal val PARSED = StructDef(
            AllPrimitiveArraysStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.PrimitiveArray("bytes", "kotlin/ByteArray", StructDef.Field.SizeModifier.Fixed(1)),
                StructDef.Field.PrimitiveArray("shorts", "kotlin/ShortArray", StructDef.Field.SizeModifier.Fixed(2)),
                StructDef.Field.PrimitiveArray("chars", "kotlin/CharArray", StructDef.Field.SizeModifier.Fixed(3)),
                StructDef.Field.PrimitiveArray("ints", "kotlin/IntArray", StructDef.Field.SizeModifier.Fixed(4)),
                StructDef.Field.PrimitiveArray("longs", "kotlin/LongArray", StructDef.Field.SizeModifier.Fixed(5)),
                StructDef.Field.PrimitiveArray("floats", "kotlin/FloatArray", StructDef.Field.SizeModifier.Fixed(6)),
                StructDef.Field.PrimitiveArray("doubles", "kotlin/DoubleArray", StructDef.Field.SizeModifier.Fixed(7)),
            ),
            mockElement(ElementKind.CLASS) {}
        )

        fun getPopulatedInstance(seed: Int): AllPrimitiveArraysStruct {
            val rng = Random(seed)

            return AllPrimitiveArraysStruct(
                ByteArray(1) { rng.nextBits(8).toByte() },
                ShortArray(2) { rng.nextBits(16).toShort() },
                CharArray(3) { rng.nextBits(16).toChar() },
                IntArray(4) { rng.nextInt() },
                LongArray(5) { rng.nextLong() },
                FloatArray(6) { rng.nextFloat() },
                DoubleArray(7) { rng.nextDouble() },
            )
        }
    }
}

@Struct
data class ObjectArrayStruct(
    @FixedSize(3)
    val simples: Array<SimpleStruct>,
    @FixedSize(4)
    val nesteds: Array<SimpleStruct.MemberStruct>
) : TestStruct {
    override val encodedSize: Int get() = simples.encodedSize + nesteds.encodedSize

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .apply {
                simples.forEach { put(it.binaryRepresentation(order)) }
                nesteds.forEach { put(it.binaryRepresentation(order)) }
            }
            .position(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjectArrayStruct

        if (!simples.contentEquals(other.simples)) return false
        if (!nesteds.contentEquals(other.nesteds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = simples.contentHashCode()
        result = 31 * result + nesteds.contentHashCode()
        return result
    }

    companion object {
        internal val PARSED = StructDef(
            ObjectArrayStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.ObjectArray(
                    "simples",
                    "kotlin/Array",
                    SimpleStruct.PARSED.name,
                    StructDef.Field.SizeModifier.Fixed(3)
                ),
                StructDef.Field.ObjectArray(
                    "nesteds",
                    "kotlin/Array",
                    SimpleStruct.MemberStruct.PARSED.name,
                    StructDef.Field.SizeModifier.Fixed(4)
                ),
            ),
            mockElement(ElementKind.CLASS) {}
        )

        fun getPopulatedInstance(seed: Int): ObjectArrayStruct {
            val rng = Random(seed)

            return ObjectArrayStruct(
                Array(3) { SimpleStruct(rng.nextInt()) },
                Array(4) { SimpleStruct.MemberStruct(SimpleStruct(rng.nextInt())) },
            )
        }
    }
}

@Struct
data class NestedObjectArrayStruct(
    @FixedSize(10)
    val structs: Array<ObjectArrayStruct>
) : TestStruct {
    override val encodedSize: Int = structs.encodedSize

    override fun binaryRepresentation(order: ByteOrder): ByteBuffer {
        return ByteBuffer.allocate(encodedSize)
            .order(order)
            .apply { structs.forEach { put(it.binaryRepresentation(order)) } }
            .position(0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NestedObjectArrayStruct

        if (!structs.contentEquals(other.structs)) return false
        if (encodedSize != other.encodedSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = structs.contentHashCode()
        result = 31 * result + encodedSize
        return result
    }

    companion object {
        internal val PARSED = StructDef(
            NestedObjectArrayStruct::class.qualifiedName!!.replace('.', '/'),
            listOf(
                StructDef.Field.ObjectArray(
                    "structs",
                    "kotlin/Array",
                    ObjectArrayStruct.PARSED.name,
                    StructDef.Field.SizeModifier.Fixed(10)
                ),
            ),
            mockElement(ElementKind.CLASS) {}
        )

        fun getPopulatedInstance(seed: Int): NestedObjectArrayStruct {
            val rng = Random(seed)

            return NestedObjectArrayStruct(
                Array(10) { ObjectArrayStruct.getPopulatedInstance(rng.nextInt()) },
            )
        }
    }
}