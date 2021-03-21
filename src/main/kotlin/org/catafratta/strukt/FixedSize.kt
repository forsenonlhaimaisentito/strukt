package org.catafratta.strukt

/**
 * Declares that the annotated array field has a fixed size.
 *
 * It is the caller's responsibility to ensure that the array
 * size is valid when writing the struct.
 *
 * @param size The array's size, must be positive.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class FixedSize(val size: Int)
