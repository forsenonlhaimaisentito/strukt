package org.catafratta.strukt.processor

/**
 * A fully qualified type name in the form `com/example/package/Outer.Inner`
 */
internal typealias QualifiedName = String

internal val QualifiedName.packageName: String
    get() =
        split('/').let { if (it.size > 1) it.subList(0, it.size - 1).joinToString(".") else "" }

internal val QualifiedName.classNames: List<String>
    get() =
        split('/').last().split('.')

internal val QualifiedName.isPrimitive: Boolean get() = this in primitiveTypes

/**
 * Kotlin's primitive types.
 */
internal val primitiveTypes = setOf(
    "kotlin/Byte",
    "kotlin/Short", "kotlin/Char",
    "kotlin/Int", "kotlin/Long",
    "kotlin/Float", "kotlin/Double"
)
