package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.*

/**
 * This class is responsible for validating Struct classes and processing Kotlin metadata.
 */
@KotlinPoetMetadataPreview
internal class ClassParser {
    /**
     * Verifies that all the classes are valid and then transforms them into [DeclaredStruct]s.
     */
    fun parse(classes: List<KotlinElement>): List<DeclaredStruct> {
        return classes
            .filterNot { it.klass.isAnnotation }          // Exclude annotations
            .onEach { it.verifyInputClass() }             // Validate classes
            .map { it.toDeclaredStruct() }                // Extract struct info
    }

    private fun KotlinElement.verifyInputClass() {
        klass.run {
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
                !isClass -> "${klass.name} is not a class"

                isPrivate || isProtected -> "Struct classes must be public or internal"

                constructors.size != 1 ->
                    "Struct classes must have exactly one constructor"

                else -> null
            }
        }?.let { msg -> throw ProcessingException(msg, element) }

        verifyConstructor()
    }

    private fun KotlinElement.verifyConstructor() {
        val ctor = klass.constructors.first()

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

}
