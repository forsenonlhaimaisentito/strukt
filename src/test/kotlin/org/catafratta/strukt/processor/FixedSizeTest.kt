package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.Flag
import org.junit.Assert
import org.junit.Test
import javax.lang.model.element.ElementKind

@KotlinPoetMetadataPreview
class FixedSizeTest {
    @Test
    fun testPrimitiveArrays() {
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/PrimitiveArraysStruct") {
                    addFlags(Flag.Class.IS_CLASS)

                    addPrimaryConstructor {
                        addPropertyParam("bytes") { type = classType("kotlin/ByteArray") }.withBackingField()
                        addPropertyParam("ints") { type = classType("kotlin/IntArray") }.withBackingField()
                    }
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.FIELD) {
                simpleName = "bytes"
                annotations += mockFixedSize(13)
            }
            +mockElement(ElementKind.FIELD) {
                simpleName = "ints"
                annotations += mockFixedSize(37)
            }
        }

        val expected = StructDef(
            "test/PrimitiveArraysStruct",
            listOf(
                StructDef.Field.PrimitiveArray("bytes", "kotlin/ByteArray", StructDef.Field.SizeModifier.Fixed(13)),
                StructDef.Field.PrimitiveArray("ints", "kotlin/IntArray", StructDef.Field.SizeModifier.Fixed(37)),
            ),
            element
        )

        val struct = ClassParser().parse(element)

        Assert.assertEquals(expected, struct)
    }

    @Test(expected = ProcessingException::class)
    fun testMissingSizeModifier() {
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/MissingModifierStruct") {
                    addFlags(Flag.Class.IS_CLASS)

                    addPrimaryConstructor {
                        addPropertyParam("bytes") { type = classType("kotlin/ByteArray") }.withBackingField()
                        addPropertyParam("ints") { type = classType("kotlin/IntArray") }.withBackingField()
                    }
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.FIELD) {
                simpleName = "bytes"
                annotations += mockFixedSize(13)
            }
            +mockElement(ElementKind.FIELD) { simpleName = "ints" }
        }

        ClassParser().parse(element)
    }

    @Test(expected = ProcessingException::class)
    fun testNegativeSize() {
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/PrimitiveArraysStruct") {
                    addFlags(Flag.Class.IS_CLASS)

                    addPrimaryConstructor {
                        addPropertyParam("bytes") { type = classType("kotlin/ByteArray") }.withBackingField()
                        addPropertyParam("ints") { type = classType("kotlin/IntArray") }.withBackingField()
                    }
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.FIELD) {
                simpleName = "bytes"
                annotations += mockFixedSize(13)
            }
            +mockElement(ElementKind.FIELD) {
                simpleName = "ints"
                annotations += mockFixedSize(-1)
            }
        }

        ClassParser().parse(element)
    }

    @Test(expected = ProcessingException::class)
    fun testMultiDimensional() {
        val element = mockElement(ElementKind.CLASS) {
            annotations +=
                buildKmClass("test/MultiDimensionalStruct") {
                    addFlags(Flag.Class.IS_CLASS)

                    addPrimaryConstructor {
                        addPropertyParam("matrix") {
                            type = classType("kotlin/Array") withArguments {
                                invariant(classType("kotlin/Array") withArguments {
                                    invariant(classType("kotlin/Int"))
                                })
                            }
                        }.withBackingField()
                    }
                }.toMetadata()

            +mockElement(ElementKind.CONSTRUCTOR) {}
            +mockElement(ElementKind.FIELD) {
                simpleName = "matrix"
                annotations += mockFixedSize(13)
            }
        }

        ClassParser().parse(element)
    }
}