package org.catafratta.strukt.processor

import javax.lang.model.element.Element

/**
 * This class represents an annotated Struct class in the input code.
 *
 * @property name The class full name
 * @property fields The fields defined by the class constructor arguments
 * @property source The source [Element] from which the data was derived
 */
internal data class DeclaredStruct(
    val name: QualifiedName,
    val fields: List<Field>,
    val source: Element
) {
    /**
     * Represents a struct field.
     *
     * @property name The field name
     * @property typeName The field type name
     */
    internal data class Field(
        val name: String,
        val typeName: QualifiedName
    )
}
