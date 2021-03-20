package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.Flag
import org.junit.Assert
import org.junit.Test
import javax.lang.model.element.ElementKind

@KotlinPoetMetadataPreview
class ClassParserTest {
    @Test
    fun testValid() {
        val elements = listOf(
            mockElement(ElementKind.CLASS) {
                annotations +=
                    buildKmClass("test/StructA") {
                        addFlags(Flag.Class.IS_CLASS)
                        addPrimaryConstructor {}
                    }.toMetadata()

                +mockElement(ElementKind.CONSTRUCTOR) {}
            },
            mockElement(ElementKind.CLASS) {
                annotations +=
                    buildKmClass("test/StructB") {
                        addFlags(Flag.Class.IS_CLASS)

                        addPrimaryConstructor {
                            addPropertyParam("a") { type = classType("test/SomeType1") }.withBackingField()
                            addPropertyParam("b") { type = classType("test/SomeType2") }.withBackingField()
                            addPropertyParam("c") { type = classType("test/SomeType3") }.withBackingField()
                        }
                    }.toMetadata()

                +mockElement(ElementKind.CONSTRUCTOR) {}
                +mockElement(ElementKind.FIELD) { simpleName = "a" }
                +mockElement(ElementKind.FIELD) { simpleName = "b" }
                +mockElement(ElementKind.FIELD) { simpleName = "c" }
            },
        )

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
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/StructA") {
                    addFlags(Flag.Class.IS_CLASS)
                    addPrimaryConstructor {}
                    addConstructor {}
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.CONSTRUCTOR) {}
        }

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
                val element = mockElement(ElementKind.CLASS) {
                    annotations +=
                        buildKmClass("test/BadStruct$i") {
                            addFlags(*f)
                            addConstructor {}
                        }.toMetadata()
                    +mockElement(ElementKind.CONSTRUCTOR) {}
                }

                ClassParser().parse(element)

                Assert.fail("Invalid class not detected at index $i")
            } catch (e: ProcessingException) {
                // OK
            }
        }
    }

    @Test(expected = ProcessingException::class)
    fun testVariadicFields() {
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/VariadicStruct") {
                    addFlags(Flag.Class.IS_CLASS)

                    addPrimaryConstructor {
                        addPropertyParam("badField") {
                            type = classType("test/SomeArrayType")
                            varargElementType = classType("test/SomeType")
                        }.withBackingField()
                    }
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.FIELD) { simpleName = "badField" }
        }

        ClassParser().parse(element)
    }

    @Test(expected = ProcessingException::class)
    fun testGenericFields() {
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/VariadicStruct") {
                    addFlags(Flag.Class.IS_CLASS)

                    addPrimaryConstructor {
                        addPropertyParam("badField") {
                            type = classType("test/SomeGenericType") withArguments {
                                invariant(classType("test/SomeTypeParameter"))
                            }
                        }.withBackingField()
                    }
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.FIELD) { simpleName = "badField" }
        }

        ClassParser().parse(element)
    }
}
