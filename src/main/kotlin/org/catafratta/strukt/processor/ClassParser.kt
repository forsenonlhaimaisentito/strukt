package org.catafratta.strukt.processor

import com.squareup.kotlinpoet.metadata.*
import kotlinx.metadata.KmClassifier
import org.catafratta.strukt.FixedSize
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
        val properties = kmClass.properties.map { it.name to it }.toMap()

        ctor.run {
            when {
                isPrivate || isProtected -> "Struct class constructor must be public"

                valueParameters.any { it.varargElementType != null } ->
                    "Variadic constructor arguments are not supported"

                else -> null
            }?.let { return@run it }

            valueParameters.forEach {
                val property = properties[it.name]
                    ?: return@run "Constructor argument `${it.name}` must correspond to a property"

                if (property.returnType != it.type)
                    return@run "Property ${kmClass.name}.${it.name}'s type doesn't match its constructor argument's type"

                if (property.fieldSignature == null)
                    return@run "Property ${kmClass.name}.${it.name} doesn't have a backing field"

                val classifier = it.type!!.classClassifier

                if (classifier.name.isGenericArray) {
                    val itemTypeArg = it.type!!.arguments.first()
                    val itemType = itemTypeArg.type ?: return@run "Array fields must declare a specific item type"

                    if (itemType.qualifiedName.isPrimitiveArray || itemType.qualifiedName.isGenericArray)
                        return@run "Multi-dimensional array fields are not supported"

                } else if (it.type!!.arguments.isNotEmpty()) {
                    return@run "Generic non-array constructor arguments are not supported"
                }
            }

            null
        }?.let { msg -> throw ProcessingException(msg, element) }
    }

    private fun parseFields(element: Element, kmClass: ImmutableKmClass): List<StructDef.Field> {
        val ctor = kmClass.constructors.first()
        val properties = kmClass.properties.map { it.name to it }.toMap()

        return ctor.valueParameters.map { param ->
            // Guaranteed existing by verifyConstructor()
            val fieldName = properties.getValue(param.name).fieldSignature!!.name
            val fieldElement = element.enclosedElements.first {
                it.kind.isField && it.simpleName.contentEquals(fieldName)
            }

            parseField(fieldElement, param)
        }
    }

    private fun parseField(fieldElement: Element, param: ImmutableKmValueParameter): StructDef.Field {
        val typeName = param.type!!.qualifiedName

        return when {
            typeName.isPrimitive -> StructDef.Field.Primitive(param.name, typeName)
            typeName.isPrimitiveArray ->
                StructDef.Field.PrimitiveArray(param.name, typeName, findSizeModifier(fieldElement))
            typeName.isGenericArray ->
                StructDef.Field.ObjectArray(
                    param.name,
                    typeName,
                    param.type!!.arguments.first().type!!.qualifiedName, // Guaranteed existing by verifyConstructor()
                    findSizeModifier(fieldElement)
                )
            else -> StructDef.Field.Object(param.name, typeName)
        }
    }

    private fun findSizeModifier(fieldElement: Element): StructDef.Field.SizeModifier {
        fieldElement.getAnnotation(FixedSize::class.java)?.let {
            if (it.size < 0) throw ProcessingException("Array size must be non-negative", fieldElement)

            return StructDef.Field.SizeModifier.Fixed(it.size)
        }

        throw ProcessingException("A size modifier is required on array types", fieldElement)
    }

    companion object {
        private val Element.kotlinMetadata: Metadata
            get() = getAnnotation(Metadata::class.java)
                ?: throw ProcessingException("$this is not a Kotlin class", this)

        private val ImmutableKmType.classClassifier: KmClassifier.Class inline get() = classifier as KmClassifier.Class
        private val ImmutableKmType.qualifiedName: QualifiedName inline get() = classClassifier.name
    }
}
