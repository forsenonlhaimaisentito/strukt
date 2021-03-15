package org.catafratta.strukt.runtime

import org.catafratta.strukt.runtime.impl.DefaultStrukt
import kotlin.reflect.KClass

/**
 * A Strukt instance allows reading and writing structs.
 *
 * @see Builder
 */
interface Strukt {
    /**
     * Reads a struct of type `T` from the given source.
     *
     * @param klass The type of struct to read.
     * @param source The source to read from.
     * @return The decoded struct.
     */
    fun <T : Any> read(klass: KClass<T>, source: BinarySource): T

    /**
     * Write the struct `value` to the given sink.
     *
     * @param value The struct to write.
     * @param sink The sink to write to.
     */
    fun <T : Any> write(value: T, sink: BinarySink)

    /**
     * A builder of [Strukt].
     */
    class Builder {
        private var classLoader: ClassLoader = Thread.currentThread().contextClassLoader

        /**
         * Sets the class loader used to load generated code.
         */
        fun classLoader(loader: ClassLoader) = apply { classLoader = loader }

        /**
         * Creates the instance.
         */
        fun build(): Strukt = DefaultStrukt(classLoader)
    }
}
