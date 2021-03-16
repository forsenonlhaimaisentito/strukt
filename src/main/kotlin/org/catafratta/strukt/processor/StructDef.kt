package org.catafratta.strukt.processor

import javax.lang.model.element.Element

/**
 * This class represents an annotated Struct class in the input code.
 *
 * @property name The class full name
 * @property fields The fields defined by the class constructor arguments
 * @property source The source [Element] from which the data was derived
 */
internal data class StructDef(
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
    internal sealed class Field {
        abstract val name: String
        abstract val typeName: QualifiedName

        /**
         * A struct field of primtive type.
         */
        data class Primitive(
            override val name: String,
            override val typeName: QualifiedName
        ) : Field()

        /**
         * A struct field of object reference type.
         */
        data class Object(
            override val name: String,
            override val typeName: QualifiedName
        ) : Field()
    }
}
