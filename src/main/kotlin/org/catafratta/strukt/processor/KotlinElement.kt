package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import kotlinx.metadata.KmClassifier
import javax.lang.model.element.Element

@KotlinPoetMetadataPreview
internal data class KotlinElement(val element: Element, val klass: ImmutableKmClass) {
    fun toDeclaredStruct(): DeclaredStruct =
        DeclaredStruct(
            klass.name,
            klass.extractFields(),
            element
        )

    companion object {
        private fun ImmutableKmClass.extractFields(): List<DeclaredStruct.Field> =
            constructors.first()
                .valueParameters.map {
                    DeclaredStruct.Field(
                        it.name,
                        (it.type!!.classifier as KmClassifier.Class).name
                    )
                }
    }
}
