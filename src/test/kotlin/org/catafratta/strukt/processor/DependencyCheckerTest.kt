package org.catafratta.strukt.processor

import org.catafratta.strukt.AllPrimitiveArraysStruct
import org.catafratta.strukt.fieldsOf
import org.junit.Test
import javax.lang.model.element.ElementKind

internal class DependencyCheckerTest {
    @Test
    fun testPrimitives() {
        val structs = listOf(
            StructDef(
                "test/StructB",
                fieldsOf(
                    "byteField" to "kotlin/Byte",
                    "shortField" to "kotlin/Short",
                    "charField" to "kotlin/Char",
                    "intField" to "kotlin/Int",
                    "longField" to "kotlin/Long",
                    "floatField" to "kotlin/Float",
                    "doubleField" to "kotlin/Double"
                ),
                mockElement(ElementKind.CLASS) {}
            )
        )

        DependencyChecker().check(structs)
    }

    @Test
    fun testPrimitiveArrays() {
        val structs = listOf(AllPrimitiveArraysStruct.PARSED)

        DependencyChecker().check(structs)
    }

    @Test
    fun testValidDependencies() {
        val structs = listOf(
            StructDef("test/StructA", fieldsOf("a" to "kotlin/Int"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructB",
                fieldsOf("a" to "test/StructA", "b" to "kotlin/Int"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructC", fieldsOf("a" to "test/StructD"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructD", fieldsOf("a" to "kotlin/Int"), mockElement(ElementKind.CLASS) {}),
        )

        DependencyChecker().check(structs)
    }

    @Test(expected = ProcessingException::class)
    fun testMissingDependencies() {
        val structs = listOf(
            StructDef("test/StructA", fieldsOf("a" to "kotlin/Int"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructB", fieldsOf("a" to "test/StructA"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructC", fieldsOf("a" to "test/StructD"), mockElement(ElementKind.CLASS) {}),
        )

        DependencyChecker().check(structs)
    }

    @Test(expected = ProcessingException::class)
    fun testCircularDependencies() {
        val structs = listOf(
            StructDef("test/StructA", fieldsOf("a" to "test/StructB"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructB", fieldsOf("a" to "test/StructC"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructC", fieldsOf("a" to "test/StructD"), mockElement(ElementKind.CLASS) {}),
            StructDef("test/StructD", fieldsOf("a" to "test/StructA"), mockElement(ElementKind.CLASS) {}),
        )

        DependencyChecker().check(structs)
    }
}
