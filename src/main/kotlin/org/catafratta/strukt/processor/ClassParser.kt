package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.*
import kotlinx.metadata.KmClassifier
import javax.lang.model.element.Element

@KotlinPoetMetadataPreview
internal class ClassParser {
    /**
     * Verifies that the class is valid and then transforms it into a [StructDef].
     */
    fun parse(element: Element): StructDef {
        val kmClass = element.kotlinMetadata.toImmutableKmClass()
        return parseClass(element, kmClass)
    }

    private fun parseClass(element: Element, kmClass: ImmutableKmClass): StructDef {
        verifyClass(element, kmClass)

        return StructDef(kmClass.name, parseFields(element, kmClass), element)
    }

    private fun verifyClass(element: Element, kmClass: ImmutableKmClass) {
        kmClass.run {
            when {
                isSealed -> "Sealed classes are not supported"
                isEnum || isEnumEntry -> "Enum classes are not supported"
                isInterface -> "Interfaces are not supported"
                isAbstract -> "Abstract classes are not supported"
                isObject -> "Objects are not supported"
                isInner -> "Inner classes are not supported"
                isInline -> "Inline classes are not supported"
                isExternal -> "External classes are not supported"
                isExpect -> "Expect classes are not supported"
                !isClass -> "${kmClass.name} is not a class"

                isPrivate || isProtected -> "Struct classes must be public or internal"

                constructors.size != 1 ->
                    "Struct classes must have exactly one constructor"

                else -> null
            }
        }?.let { msg -> throw ProcessingException(msg, element) }

        verifyConstructor(element, kmClass)
    }

    private fun verifyConstructor(element: Element, kmClass: ImmutableKmClass) {
        val ctor = kmClass.constructors.first()

        ctor.run {
            when {
                isPrivate || isProtected -> "Struct class constructor must be public"

                valueParameters.any { it.varargElementType != null } ->
                    "Variadic constructor arguments are not supported"

                valueParameters.any { it.type?.arguments?.isNotEmpty() ?: false } ->
                    "Generic constructor arguments are not supported"

                else -> null
            }
        }?.let { msg -> throw ProcessingException(msg, element) }
    }

    private fun parseFields(element: Element, kmClass: ImmutableKmClass): List<StructDef.Field> {
        val ctor = kmClass.constructors.first()

        return ctor.valueParameters.map { parseField(it) }
    }

    private fun parseField(param: ImmutableKmValueParameter): StructDef.Field {
        val typeName: QualifiedName = (param.type!!.classifier as KmClassifier.Class).name

        return when {
            typeName.isPrimitive -> StructDef.Field.Primitive(param.name, typeName)
            else -> StructDef.Field.Object(param.name, typeName)
        }
    }

    companion object {
        private val Element.kotlinMetadata: Metadata
            get() = getAnnotation(Metadata::class.java)
                ?: throw ProcessingException("$this is not a Kotlin class", this)
    }
}
