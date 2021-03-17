package org.catafratta.strukt.processor

import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror

class MockElement(
    private val annotations: List<Annotation>? = null
) : Element {
    override fun getAnnotationMirrors(): MutableList<out AnnotationMirror> {
        throw NotImplementedError()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotation(annotationType: Class<A>): A? {
        return checkNotNull(annotations).firstOrNull { annotationType.isInstance(it) } as A?
    }

    override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>?): Array<A> {
        throw NotImplementedError()
    }

    override fun asType(): TypeMirror {
        throw NotImplementedError()
    }

    override fun getKind(): ElementKind {
        throw NotImplementedError()
    }

    override fun getModifiers(): MutableSet<Modifier> {
        throw NotImplementedError()
    }

    override fun getSimpleName(): Name {
        throw NotImplementedError()
    }

    override fun getEnclosingElement(): Element {
        throw NotImplementedError()
    }

    override fun getEnclosedElements(): MutableList<out Element> {
        throw NotImplementedError()
    }

    override fun <R : Any?, P : Any?> accept(v: ElementVisitor<R, P>?, p: P): R {
        throw NotImplementedError()
    }
}
