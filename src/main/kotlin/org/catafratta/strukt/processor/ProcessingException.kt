package org.catafratta.strukt.processor

import javax.lang.model.element.Element

internal class ProcessingException(override val message: String, val element: Element) : Exception()
