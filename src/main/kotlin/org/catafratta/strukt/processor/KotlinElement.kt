package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.lang.model.element.Element

@KotlinPoetMetadataPreview
internal data class KotlinElement(val element: Element, val klass: ImmutableKmClass)
