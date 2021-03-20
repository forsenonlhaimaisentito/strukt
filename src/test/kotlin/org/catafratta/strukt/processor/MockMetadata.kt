package org.catafratta.strukt.processor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.*
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.fieldSignature


@KotlinPoetMetadataPreview
internal fun ImmutableKmClass.toMetadata() = toMutableWithJvmFieldSignatures().toMetadata()

/**
 * Workaround for KotlinPoet #1044.
 *
 * See https://github.com/square/kotlinpoet/issues/1044
 */
@KotlinPoetMetadataPreview
private fun ImmutableKmClass.toMutableWithJvmFieldSignatures(): KmClass = toMutable().also {
    properties.forEachIndexed { i, property -> it.properties[i].fieldSignature = property.fieldSignature }
}

internal fun KmClass.toMetadata(): Metadata {
    val classHeader = KotlinClassMetadata.Class.Writer().apply { accept(this) }.write().header

    return mock {
        on { kind } doReturn classHeader.kind
        on { metadataVersion } doReturn classHeader.metadataVersion
        on { bytecodeVersion } doReturn classHeader.bytecodeVersion
        on { data1 } doReturn classHeader.data1
        on { data2 } doReturn classHeader.data2
        on { extraString } doReturn classHeader.extraString
        on { packageName } doReturn classHeader.packageName
        on { extraInt } doReturn classHeader.extraInt
    }
}

@DslMarker
internal annotation class KmClassBuilderDsl

@KmClassBuilderDsl
internal inline fun buildKmClass(name: ClassName, init: KmClassBuilder.() -> Unit): KmClass {
    return KmClassBuilder(name).apply(init).build()
}

@KmClassBuilderDsl
internal class KmClassBuilder(var name: ClassName) {
    val kmClass: KmClass = KmClass().apply { name = this@KmClassBuilder.name }

    fun addFlags(vararg flags: Flag) {
        kmClass.flags = kmClass.flags or flagsOf(*flags)
    }

    @KmClassBuilderDsl
    inline fun addConstructor(init: KmConstructor.() -> Unit) {
        kmClass.constructors += KmConstructor(0).apply(init)
    }

    @KmClassBuilderDsl
    inline fun addPrimaryConstructor(init: KmConstructor.() -> Unit) {
        kmClass.constructors += KmConstructor(flagsOf(Flag.Constructor.IS_PRIMARY)).apply(init)
    }

    @KmClassBuilderDsl
    inline fun KmConstructor.addParameter(name: String, init: KmValueParameter.() -> Unit) {
        valueParameters += KmValueParameter(0, name).apply(init)
    }

    @KmClassBuilderDsl
    inline fun KmConstructor.addPropertyParam(name: String, init: KmValueParameter.() -> Unit): KmProperty {
        val param = KmValueParameter(0, name).apply(init)
        val prop = KmProperty(0, name, 0, 0).apply { returnType = param.type!! }

        kmClass.properties += prop
        valueParameters += param

        return prop
    }

    fun classType(name: ClassName, flags: Flags = 0): KmType = KmType(flags).apply {
        classifier = KmClassifier.Class(name)
    }

    @KmClassBuilderDsl
    infix fun KmType.withArguments(init: TypeArgumentsBuilder.() -> Unit) = apply {
        arguments += TypeArgumentsBuilder().apply(init).toList()
    }

    class TypeArgumentsBuilder {
        private val args = mutableListOf<KmTypeProjection>()

        fun invariant(type: KmType) {
            args += KmTypeProjection(KmVariance.INVARIANT, type)
        }

        fun contravariant(type: KmType) {
            args += KmTypeProjection(KmVariance.IN, type)
        }

        fun covariant(type: KmType) {
            args += KmTypeProjection(KmVariance.OUT, type)
        }

        fun toList(): List<KmTypeProjection> = args.toList()
    }

    fun build(): KmClass = KmClass().also(kmClass::accept)
}
