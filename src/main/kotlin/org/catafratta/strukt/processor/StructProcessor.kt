package org.catafratta.strukt.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import org.catafratta.strukt.Struct
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@KotlinPoetMetadataPreview
@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_11)
internal class StructProcessor : KaptProcessor() {
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Struct::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            processStructs(roundEnv)
        } catch (e: ProcessingException) {
            failWith(e)
        }

        return true
    }

    private fun processStructs(roundEnv: RoundEnvironment) {
        roundEnv
            .getElementsAnnotatedWith(Struct::class.java) // Find all @Struct classes
            .map { ClassParser().parse(it) }              // Extract struct info
            .also { DependencyChecker().check(it) }       // Check dependencies
            .map { CodeGenerator(it).generate() }         // Generate source filed
            .forEach {
                val outFile = kaptOutputPath.toPath().resolve(it.path).toFile()
                outFile.parentFile.mkdirs()
                outFile.writeText(it.code)
            }
    }

    private fun failWith(e: ProcessingException) = failWithMessage(e.message, e.element)

    private fun failWithMessage(message: CharSequence, element: Element) {
        printError(message, element)
    }

    private fun printError(msg: CharSequence, element: Element) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg, element)
    }
}
