package org.catafratta.strukt.runtime

/**
 * Thrown by read methods in case of unexpected EOF or buffer underflow.
 */
class IncompleteReadException(cause: Throwable? = null) : Exception(cause)
