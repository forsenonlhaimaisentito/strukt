package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.catafratta.strukt.common.CodecNamingStrategy
import org.catafratta.strukt.runtime.BinarySink
import org.catafratta.strukt.runtime.BinarySource
import org.catafratta.strukt.runtime.Codec
import org.catafratta.strukt.runtime.Strukt
import java.nio.file.Path


internal class CodeGenerator(private val structDef: StructDef) {
    private val structClass = ClassName(structDef.name.packageName, structDef.name.classNames)

    private val codecClass =
        CodecNamingStrategy.nameFor(structClass.packageName, structClass.simpleNames).let { (pkg, cls) ->
            ClassName(pkg, cls)
        }

    fun generate(): SourceFile {
        val path = Path.of(codecClass.packageName.replace('.', '/'), "${codecClass.simpleName}.kt")
        val code = FileSpec.builder(codecClass.packageName, codecClass.simpleName)
            .addImport("org.catafratta.strukt.runtime", "read")
            .addType(
                TypeSpec.classBuilder(codecClass)
                    .addModifiers(KModifier.PRIVATE)
                    .addSuperinterface(Codec::class.asClassName().parameterizedBy(structClass))
                    .addCodecConstructor()
                    .addFunction(buildReadMethod())
                    .addFunction(buildWriteMethod())
                    .build()
            )
            .build()
            .toString()

        return SourceFile(path, code)
    }

    private fun TypeSpec.Builder.addCodecConstructor() = apply {
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("strukt", Strukt::class)
                .build()
        ).addProperty(
            PropertySpec.builder("strukt", Strukt::class)
                .addModifiers(KModifier.PRIVATE)
                .initializer("strukt")
                .build()
        )
    }

    private fun buildReadMethod() = FunSpec.builder("read")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("source", BinarySource::class)
        .returns(structClass)
        .addCode(buildReadCode())
        .build()

    private fun buildReadCode() = CodeBlock.builder().apply {
        add("return %T(\n", structClass)
        indent()
        structDef.fields.forEach { field ->
            add("${field.name} = ${readStatementFor(field)},\n")
        }
        unindent()
        add(")")
    }.build()

    private fun readStatementFor(field: StructDef.Field) = field.run {
        when {
            typeName.isPrimitive -> "source.read${typeName.classNames.last()}()"
            else -> "strukt.read(source)"
        }
    }

    private fun buildWriteMethod() = FunSpec.builder("write")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("value", structClass)
        .addParameter("sink", BinarySink::class)
        .addCode(buildWriteCode())
        .build()

    private fun buildWriteCode() = CodeBlock.builder().apply {
        add("value.run {\n")
        indent()
        structDef.fields.forEach { field ->
            addStatement("${writeStatementFor(field)}\n")
        }
        unindent()
        add("}")
    }.build()

    private fun writeStatementFor(field: StructDef.Field) = field.run {
        when {
            typeName.isPrimitive -> "sink.write($name)"
            else -> "strukt.write($name, sink)"
        }
    }

    data class SourceFile(val path: Path, val code: String)
}
