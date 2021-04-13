package org.catafratta.strukt.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.catafratta.strukt.*
import org.catafratta.strukt.common.CodecNamingStrategy
import org.catafratta.strukt.runtime.Codec
import org.catafratta.strukt.runtime.Strukt
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Path
import kotlin.reflect.KClass

class CodeGeneratorTest {
    @Test
    fun testCodeCompiles() {
        Assert.assertEquals(KotlinCompilation.ExitCode.OK, compilerResult.exitCode)
    }

    @Test
    fun testOutputPath() {
        val (path, _) = CodeGenerator(SimpleStruct.PARSED).generate()

        val expectedPath = Path.of(
            "${CodecNamingStrategy.BASE_PACKAGE}.${SimpleStruct.PARSED.name.packageName}".replace('.', '/'),
            "${SimpleStruct.PARSED.name.classNames.first()}_Codec.kt"
        )

        Assert.assertEquals(path, expectedPath)
    }

    @Test
    fun testSimpleStruct() {
        val strukt = MockStrukt()
        val codec = getCodecInstance<AllPrimitivesStruct>(strukt)

        val data = AllPrimitivesStruct(-1, 1337, '\u25b2', 0x313370, -4548993244655141137L, 13.37f, 13e37)
        testCodec(codec, data)
    }

    @Test
    fun testNestedStruct() {
        val strukt = MockStrukt()
        val outerCodec = getCodecInstance<NestedStruct>(strukt)
        getCodecInstance<SimpleStruct>(strukt)  // For side-effects
        getCodecInstance<SimpleStruct.MemberStruct>(strukt)  // For side-effects

        val data = NestedStruct(123456, SimpleStruct.MemberStruct(SimpleStruct(54321)))
        testCodec(outerCodec, data)
    }

    @Test
    fun testPrimitiveArrays() {
        val strukt = MockStrukt()
        val codec = getCodecInstance<AllPrimitiveArraysStruct>(strukt)

        val data = AllPrimitiveArraysStruct.getPopulatedInstance(0x31337)
        testCodec(codec, data)
    }

    @Test
    fun testObjectArrays() {
        val strukt = MockStrukt()
        val outerCodec = getCodecInstance<NestedObjectArrayStruct>(strukt)
        getCodecInstance<SimpleStruct>(strukt)  // For side-effects
        getCodecInstance<SimpleStruct.MemberStruct>(strukt)  // For side-effects
        getCodecInstance<ObjectArrayStruct>(strukt)  // For side-effects

        val data = NestedObjectArrayStruct.getPopulatedInstance(0x31337)
        testCodec(outerCodec, data)
    }

    @Test
    fun testWeirdName() {
        val strukt = MockStrukt()
        val codec = getCodecInstance<WeirdNameStruct>(strukt)

        val data = WeirdNameStruct(1234)
        testCodec(codec, data)
    }

    @Test
    fun testSizeField() {
        val strukt = MockStrukt()
        val codec = getCodecInstance<DynamicallySizedStruct>(strukt)

        val data = DynamicallySizedStruct.getPopulatedInstance(0x31337)
        testCodec(codec, data)
    }

    private inline fun <reified T : Any> getCodecInstance(strukt: Strukt): Codec<T> {
        val codecClass = loadCompiledCodec(T::class)
        val ctor = codecClass.getDeclaredConstructor(Strukt::class.java)
        Assert.assertFalse("Generated class should be private", ctor.isAccessible)
        ctor.isAccessible = true
        val instance = ctor.newInstance(strukt)
        (strukt as? MockStrukt)?.apply { codecs[T::class] = instance }
        return instance
    }

    private fun <T : Any> loadCompiledCodec(klass: KClass<out T>): Class<Codec<T>> {
        val parsed = try {
            klass.java
                .getDeclaredField("PARSED")
                .apply { isAccessible = true }
                .get(null) as StructDef
        } catch (e: Exception) {
            throw RuntimeException("PARSED constant not found on test struct", e)
        }

        @Suppress("UNCHECKED_CAST")
        return loadCompiledCodec(parsed) as Class<Codec<T>>
    }

    private fun loadCompiledCodec(structDef: StructDef): Class<*> {
        val codecClass = compilerResult.classLoader.loadClass(structDef.codecClassName())
        Assert.assertTrue("Generated class should implement Codec", Codec::class.java.isAssignableFrom(codecClass))
        return codecClass
    }

    private fun StructDef.codecClassName(): String =
        CodecNamingStrategy.fullNameFor(name.packageName, name.classNames)

    companion object {
        private lateinit var compilerResult: KotlinCompilation.Result

        @BeforeClass
        @JvmStatic
        fun compileCodecs() {
            compilerResult = KotlinCompilation().apply {
                sources = TestStructs.allDefinitions
                    .map { CodeGenerator(it).generate() }
                    .map { (path, code) -> path.fileName.toString() to code }
                    .map { (path, code) -> SourceFile.kotlin(path, code) }

                inheritClassPath = true
                jvmTarget = "1.8" // Because we use inline functions and this project's jvmTarget is 1.8
                messageOutputStream = System.out
            }.compile()
        }

        @AfterClass
        @JvmStatic
        fun cleanupCompilerOutput() {
            val outputDir = compilerResult.outputDirectory.parentFile
            println("Removing $outputDir recursively")
            outputDir.deleteRecursively()
        }
    }
}
