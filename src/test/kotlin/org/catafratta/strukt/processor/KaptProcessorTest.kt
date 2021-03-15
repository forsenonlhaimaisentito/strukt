package org.catafratta.strukt.processor

import org.junit.Assert
import org.junit.Test
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

class KaptProcessorTest {
    @Test
    fun testOutPath() {
        MockKaptProcessor {
            put("kapt.kotlin.generated", "it works")
        }.checkPath("it works")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNoPath() {
        MockKaptProcessor {}.checkPath("")
    }

    internal class MockKaptProcessor(initOptions: MutableMap<String, String>.() -> Unit) : KaptProcessor() {
        init {
            processingEnv = object : ProcessingEnvironment {
                private val options = mutableMapOf<String, String>()

                override fun getOptions(): MutableMap<String, String> = options
                override fun getMessager() = throw NotImplementedError()
                override fun getFiler() = throw NotImplementedError()
                override fun getElementUtils() = throw NotImplementedError()
                override fun getTypeUtils() = throw NotImplementedError()
                override fun getSourceVersion() = throw NotImplementedError()
                override fun getLocale() = throw NotImplementedError()
            }

            processingEnv.options.initOptions()
        }

        fun checkPath(expected: String) {
            Assert.assertEquals(File(expected), kaptOutputPath)
        }

        override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
            throw NotImplementedError()
        }
    }
}
