package org.catafratta.strukt.common

import kotlin.reflect.KClass

internal object CodecNamingStrategy {
    /**
     * The base package for all generated Codec classes.
     */
    const val BASE_PACKAGE = "org.catafratta.strukt.generated"

    /**
     * Returns the package and name of the generated Codec class for the given Struct class.
     */
    fun nameFor(structClass: KClass<*>): Pair<String, String> =
        nameFor(structClass.java.packageName, structClass.java.hierarchy())

    /**
     * Returns the package and name of the generated Codec class for the given Struct package and class hierarchy.
     */
    fun nameFor(packageName: String, classNames: Iterable<String>): Pair<String, String> =
        "$BASE_PACKAGE.$packageName" to "${classNames.joinToString("_")}_Codec"

    /**
     * Just like `nameFor`, but concatenates package and class name for convenience.
     *
     * @see nameFor
     */
    fun fullNameFor(structClass: KClass<*>): String = nameFor(structClass).run { "$first.$second" }

    /**
     * Just like `nameFor`, but concatenates package and class name for convenience.
     *
     * @see nameFor
     */
    fun fullNameFor(packageName: String, classNames: Iterable<String>): String = nameFor(packageName, classNames).run {
        "$first.$second"
    }

    /**
     * Returns the simple names of this class and all its enclosing classes, from outer to inner.
     */
    private fun Class<*>.hierarchy(): List<String> {
        val out = mutableListOf<String>()
        var cls: Class<*>? = this

        while (cls != null) {
            out += cls.simpleName
            cls = cls.declaringClass
        }

        return out.reversed()
    }
}
