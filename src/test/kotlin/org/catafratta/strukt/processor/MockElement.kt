package org.catafratta.strukt.processor

import com.nhaarman.mockitokotlin2.*
import javax.lang.model.element.*


@DslMarker
internal annotation class MockElementDsl

@MockElementDsl
internal inline fun mockElement(kind: ElementKind, init: MockElementBuilder.() -> Unit): Element =
    MockElementBuilder(kind).apply(init).build()

@MockElementDsl
internal open class MockElementBuilder(
    var kind: ElementKind,
    var simpleName: String = "",
    val annotations: MutableList<Annotation> = mutableListOf(),
    val modifiers: MutableSet<Modifier> = mutableSetOf(),
    var parent: Element? = null,
    val children: MutableList<Element> = mutableListOf()
) {
    open fun build(): Element = mock { addElementMocks() }

    protected inline fun <reified T : Element> extendMock(init: KStubbing<T>.() -> Unit): T {
        return mock {
            addElementMocks()
            init()
        }
    }

    protected fun <T : Element> KStubbing<T>.addElementMocks() {
        on { kind } doReturn this@MockElementBuilder.kind
        on { simpleName } doReturn this@MockElementBuilder.simpleName.toName()
        on { modifiers } doReturn this@MockElementBuilder.modifiers.toMutableSet()
        on { enclosingElement } doReturn parent
        on { enclosedElements } doReturn children.toMutableList()

        on { getAnnotation<Annotation>(any()) } doAnswer { invocation ->
            val annotationType = invocation.getArgument<Class<*>>(0)
            annotations.firstOrNull { annotationType.isInstance(it) }
        }
    }

    operator fun Element.unaryPlus() {
        children += this
    }
}

internal fun String.toName(): Name = object : Name, CharSequence by this {
    override fun contentEquals(cs: CharSequence): Boolean {
        return this@toName.contentEquals(cs)
    }
}
