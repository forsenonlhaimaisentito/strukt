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
        structDef.fields.forEach {
            add("val %N: %T = ", fieldTempName(it.name), it.poetTypeName)
            addReadStatement(it)
            add("\n")
        }
        add("\n")

        add("return %T(\n", structClass)
        indent()
        structDef.fields.forEach { add("%N,\n", fieldTempName(it.name)) }
        unindent()
        add(")")
    }.build()

    private fun CodeBlock.Builder.addReadStatement(field: StructDef.Field) {
        when (field) {
            is StructDef.Field.Primitive -> add("source.read%L()", field.typeName.classPart)
            is StructDef.Field.Object -> add("strukt.read(source)")
            is StructDef.Field.PrimitiveArray -> {
                add("%L(%L) { ", field.typeName.classPart, sizeExpressionFor(field.sizeModifier))
                add("source.read%L()", field.itemTypeName.classPart)
                add(" }")
            }
            is StructDef.Field.ObjectArray -> {
                add(
                    "Array<%T>(%L) { ",
                    field.itemTypeName.poetClassName,
                    sizeExpressionFor(field.sizeModifier)
                )
                add("strukt.read(source)")
                add(" }")
            }
        }
    }

    private fun fieldTempName(fieldName: String) = "_field_$fieldName"

    private val QualifiedName.poetClassName: ClassName inline get() = ClassName(packageName, classNames)

    private val StructDef.Field.poetTypeName: TypeName
        inline get() = when (this) {
            is StructDef.Field.ObjectArray -> typeName.poetClassName.parameterizedBy(itemTypeName.poetClassName)
            else -> typeName.poetClassName
        }

    private fun sizeExpressionFor(sizeModifier: StructDef.Field.SizeModifier): String {
        return when (sizeModifier) {
            is StructDef.Field.SizeModifier.Fixed -> "${sizeModifier.size}"
            is StructDef.Field.SizeModifier.FieldBased -> {
                val builder = CodeBlock.builder()

                builder.add("%N", fieldTempName(sizeModifier.field.name))

                when (sizeModifier.field.typeName) {
                    "kotlin/Byte" -> builder.add(".toInt().and(0xFF)")
                    "kotlin/Short", "kotlin/Char" -> builder.add(".toInt().and(0xFFFF)")
                }

                builder.build().toString()
            }
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
        structDef.fields.forEach {
            addWriteStatement(it)
            add("\n")
        }
        unindent()
        add("}")
    }.build()

    private fun CodeBlock.Builder.addWriteStatement(field: StructDef.Field) {
        when (field) {
            is StructDef.Field.Primitive -> add("sink.write(%N)", field.name)
            is StructDef.Field.Object -> add("strukt.write(%N, sink)", field.name)
            is StructDef.Field.PrimitiveArray -> add("%N.forEach { sink.write(it) }", field.name)
            is StructDef.Field.ObjectArray -> add("%N.forEach { strukt.write(it, sink) }", field.name)
        }
    }

    data class SourceFile(val path: Path, val code: String)
}
