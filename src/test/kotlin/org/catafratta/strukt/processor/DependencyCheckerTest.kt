package org.catafratta.strukt.processor

import org.catafratta.strukt.fieldsOf
import org.junit.Test

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
                MockElement()
            )
        )

        DependencyChecker().check(structs)
    }

    @Test
    fun testValidDependencies() {
        val structs = listOf(
            StructDef("test/StructA", fieldsOf("a" to "kotlin/Int"), MockElement()),
            StructDef("test/StructB", fieldsOf("a" to "test/StructA", "b" to "kotlin/Int"), MockElement()),
            StructDef("test/StructC", fieldsOf("a" to "test/StructD"), MockElement()),
            StructDef("test/StructD", fieldsOf("a" to "kotlin/Int"), MockElement()),
        )

        DependencyChecker().check(structs)
    }

    @Test(expected = ProcessingException::class)
    fun testMissingDependencies() {
        val structs = listOf(
            StructDef("test/StructA", fieldsOf("a" to "kotlin/Int"), MockElement()),
            StructDef("test/StructB", fieldsOf("a" to "test/StructA"), MockElement()),
            StructDef("test/StructC", fieldsOf("a" to "test/StructD"), MockElement()),
        )

        DependencyChecker().check(structs)
    }

    @Test(expected = ProcessingException::class)
    fun testCircularDependencies() {
        val structs = listOf(
            StructDef("test/StructA", fieldsOf("a" to "test/StructB"), MockElement()),
            StructDef("test/StructB", fieldsOf("a" to "test/StructC"), MockElement()),
            StructDef("test/StructC", fieldsOf("a" to "test/StructD"), MockElement()),
            StructDef("test/StructD", fieldsOf("a" to "test/StructA"), MockElement()),
        )

        DependencyChecker().check(structs)
    }
}
