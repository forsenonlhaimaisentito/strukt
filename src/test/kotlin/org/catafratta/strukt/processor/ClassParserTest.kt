package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.Flag
import org.junit.Assert
import org.junit.Test

@KotlinPoetMetadataPreview
class ClassParserTest {
    @Test
    fun testValid() {
        val elements = listOf(
            buildKmClass("test/StructA") {
                addFlags(Flag.Class.IS_CLASS)
                addPrimaryConstructor {}
            },
            buildKmClass("test/StructB") {
                addFlags(Flag.Class.IS_CLASS)

                addPrimaryConstructor {
                    addPropertyParam("a") { type = classType("test/SomeType1") }
                    addPropertyParam("b") { type = classType("test/SomeType2") }
                    addPropertyParam("c") { type = classType("test/SomeType3") }
                }
            }
        ).map { MockElement(annotations = listOf(it.toMetadata())) }

        val expected = listOf(
            StructDef("test/StructA", emptyList(), elements[0]),
            StructDef(
                "test/StructB",
                listOf(
                    StructDef.Field.Object("a", "test/SomeType1"),
                    StructDef.Field.Object("b", "test/SomeType2"),
                    StructDef.Field.Object("c", "test/SomeType3"),
                ),
                elements[1]
            ),
        )

        val result = elements.map { ClassParser().parse(it) }

        Assert.assertArrayEquals(expected.toTypedArray(), result.toTypedArray())
    }

    @Test(expected = ProcessingException::class)
    fun testMultipleConstructors() {
        val element = buildKmClass("test/StructA") {
            addFlags(Flag.Class.IS_CLASS)
            addPrimaryConstructor {}
            addConstructor {}
        }.toMetadata().let { MockElement(annotations = listOf(it)) }

        ClassParser().parse(element)
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
                val element = buildKmClass("test/BadStruct$i") {
                    addFlags(*f)
                    addConstructor {}
                }.toMetadata().let { MockElement(annotations = listOf(it)) }

                ClassParser().parse(element)

                Assert.fail("Invalid class not detected at index $i")
            } catch (e: ProcessingException) {
                // OK
            }
        }
    }

    @Test(expected = ProcessingException::class)
    fun testVariadicFields() {
        val element = buildKmClass("test/VariadicStruct") {
            addFlags(Flag.Class.IS_CLASS)

            addPrimaryConstructor {
                addPropertyParam("badField") {
                    type = classType("test/SomeArrayType")
                    varargElementType = classType("test/SomeType")
                }
            }
        }.toMetadata().let { MockElement(annotations = listOf(it)) }

        ClassParser().parse(element)
    }

    @Test(expected = ProcessingException::class)
    fun testGenericFields() {
        val element = buildKmClass("test/VariadicStruct") {
            addFlags(Flag.Class.IS_CLASS)

            addPrimaryConstructor {
                addPropertyParam("badField") {
                    type = classType("test/SomeGenericType") withArguments {
                        invariant(classType("test/SomeTypeParameter"))
                    }
                }
            }
        }.toMetadata().let { MockElement(annotations = listOf(it)) }

        ClassParser().parse(element)
    }
}
