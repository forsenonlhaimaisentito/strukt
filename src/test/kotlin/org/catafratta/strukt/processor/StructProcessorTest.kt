package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.catafratta.strukt.common.CodecNamingStrategy
import org.junit.Assert
import org.junit.Test
import java.io.File

@KotlinPoetMetadataPreview
class StructProcessorTest {
    @Test
    fun testValid() {
        val compiler = KotlinCompilation().apply {
            sources = listOf(testSource)

            annotationProcessors = listOf(StructProcessor())

            jvmTarget = "1.8"
            inheritClassPath = true
            messageOutputStream = System.out
        }

        val result = compiler.compile()
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val pkgDir = File("${CodecNamingStrategy.BASE_PACKAGE}.test".replace('.', '/'))
        val outBaseDir = compiler.kaptKotlinGeneratedDir.resolve(pkgDir)

        Assert.assertTrue(outBaseDir.resolve("SomeStruct_Codec.kt").exists())
        Assert.assertFalse(outBaseDir.resolve("NotAStruct_Codec.kt").exists())

        compiler.workingDir.deleteRecursively()
    }

    @Test
    fun testInvalid() {
        val compiler = KotlinCompilation().apply {
            sources = listOf(testSource, badSource)
            annotationProcessors = listOf(StructProcessor())

            jvmTarget = "1.8"
            inheritClassPath = true
            messageOutputStream = System.out
        }

        val result = compiler.compile()
        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)

        compiler.workingDir.deleteRecursively()
    }


    companion object {
        val testSource = SourceFile.kotlin(
            "TestSource.kt", """
            package test

            import org.catafratta.strukt.Struct
            import org.catafratta.strukt.FixedSize
            
            @Struct
            data class SomeStruct(
                val someField: Int,
                @FixedSize(1337)
                val someArray: IntArray
            )
            
            data class NotAStruct(
                val someField: Int
            )
        """
        )

        val badSource = SourceFile.kotlin(
            "BadSource.kt", """
            package test

            import org.catafratta.strukt.Struct
            
            @Struct
            data class CyclicStructA(
                val someField: CyclicStructB
            )
            
            @Struct
            data class CyclicStructB(
                val someField: CyclicStructA
            )
        """
        )
    }
}
