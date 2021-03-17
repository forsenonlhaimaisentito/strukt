package org.catafratta.strukt.processor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.KotlinClassMetadata


@KotlinPoetMetadataPreview
internal fun mockClassMetadata(kmClass: ImmutableKmClass) = mockClassMetadata(kmClass.toMutable())


internal fun mockClassMetadata(kmClass: KmClass): Metadata {
    val classHeader = KotlinClassMetadata.Class.Writer().apply { kmClass.accept(this) }.write().header

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
