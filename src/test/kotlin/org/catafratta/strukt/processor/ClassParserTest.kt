package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toImmutable
import kotlinx.metadata.*
import org.junit.Assert
import org.junit.Test

@KotlinPoetMetadataPreview
class ClassParserTest {
    @Test
    fun testValid() {
        val classes = listOf(
            KotlinElement(MockElement(), buildKlass {
                name = "test/StructA"
                flags = flagsOf(Flag.Class.IS_CLASS)

                addConstructor(Flag.Constructor.IS_PRIMARY) {}
            }),
            KotlinElement(MockElement(), buildKlass {
                name = "test/StructB"
                flags = flagsOf(Flag.Class.IS_CLASS)

                addConstructor(Flag.Constructor.IS_PRIMARY) {
                    addParameter("a") { type = simpleType("test/SomeType1") }
                    addParameter("b") { type = simpleType("test/SomeType2") }
                    addParameter("c") { type = simpleType("test/SomeType3") }
                }
            })
        )

        val expected = listOf(
            StructDef("test/StructA", emptyList(), classes[0].element),
            StructDef(
                "test/StructB",
                listOf(
                    StructDef.Field("a", "test/SomeType1"),
                    StructDef.Field("b", "test/SomeType2"),
                    StructDef.Field("c", "test/SomeType3"),
                ),
                classes[1].element
            ),
        )

        val result = ClassParser().parse(classes)

        Assert.assertArrayEquals(expected.toTypedArray(), result.toTypedArray())
    }

    @Test(expected = ProcessingException::class)
    fun testMultipleConstructors() {
        val classes = listOf(
            KotlinElement(MockElement(), buildKlass {
                name = "test/StructA"
                flags = flagsOf(Flag.Class.IS_CLASS)
                addConstructor {}
                addConstructor {}
            })
        )

        ClassParser().parse(classes)
    }

    @Test
    fun testUnsupportedClassTypes() {
        val unsupportedFlags = listOf(
            arrayOf(Flag.Class.IS_ENUM_CLASS),
            arrayOf(Flag.Class.IS_ENUM_ENTRY),
            arrayOf(Flag.Class.IS_INTERFACE),
            arrayOf(Flag.IS_ABSTRACT),
            arrayOf(Flag.Class.IS_OBJECT),
            arrayOf(Flag.Class.IS_INNER),
            arrayOf(Flag.Class.IS_INLINE),
            arrayOf(Flag.Class.IS_EXTERNAL),
            arrayOf(Flag.Class.IS_EXPECT),
            arrayOf(Flag.IS_SEALED),
        )

        unsupportedFlags.forEachIndexed { i, f ->
            try {
                val input = KotlinElement(MockElement(), buildKlass {
                    name = "test/BadStruct$i"
                    flags = flagsOf(*f)
                    addConstructor {}
                })

                ClassParser().parse(listOf(input))

                Assert.fail("Invalid class not detected at index $i")
            } catch (e: ProcessingException) {
                // OK
            }
        }
    }

    @Test(expected = ProcessingException::class)
    fun testVariadicFields() {
        val classes = listOf(
            KotlinElement(MockElement(), buildKlass {
                name = "test/VariadicStruct"
                flags = flagsOf(Flag.Class.IS_CLASS)

                addConstructor {
                    addParameter("badField") {
                        varargElementType = simpleType("test/SomeType")
                    }
                }
            })
        )

        ClassParser().parse(classes)
    }

    @Test(expected = ProcessingException::class)
    fun testGenericFields() {
        val classes = listOf(
            KotlinElement(MockElement(), buildKlass {
                name = "test/VariadicStruct"
                flags = flagsOf(Flag.Class.IS_CLASS)

                addConstructor {
                    addParameter("badField") {
                        type = KmType(0).apply {
                            classifier = KmClassifier.Class("test/SomeGenericType")
                            arguments += KmTypeProjection(KmVariance.INVARIANT, simpleType("test/SomeTypeParameter"))
                        }
                    }
                }
            })
        )

        ClassParser().parse(classes)
    }

    companion object {
        @DslMarker
        private annotation class KlassDsl

        @KlassDsl
        private inline fun buildKlass(init: KmClass.() -> Unit): ImmutableKmClass =
            KmClass().apply(init).toImmutable()

        @KlassDsl
        private inline fun KmClass.addConstructor(vararg flags: Flag, init: KmConstructor.() -> Unit) {
            constructors += KmConstructor(flagsOf(*flags)).apply(init)
        }

        @KlassDsl
        private inline fun KmConstructor.addParameter(
            name: String,
            vararg flags: Flag,
            init: KmValueParameter.() -> Unit
        ) {
            valueParameters += KmValueParameter(flagsOf(*flags), name).apply(init)
        }

        private fun simpleType(name: String): KmType {
            return KmType(0).apply { classifier = KmClassifier.Class(name) }
        }
    }
}
