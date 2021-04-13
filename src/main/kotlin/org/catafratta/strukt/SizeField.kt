package org.catafratta.strukt

/**
 * Declares that the annotated array field's size is specified
 * by the value of a preceding field.
 *
 * Due to JVM limitations, only Byte, Short, Char and Int fields
 * are supported. The generated code zero-extends shorter types
 * to Int. For Int fields, the field's value should not go
 * above [Int.MAX_VALUE].
 *
 * It is the caller's responsibility to ensure that the size
 * field is valid when writing the struct.
 *
 * @param fieldName The name of the field indicating the size.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class SizeField(val fieldName: String)
