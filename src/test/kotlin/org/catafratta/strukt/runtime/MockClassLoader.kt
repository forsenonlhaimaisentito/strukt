package org.catafratta.strukt.runtime

class MockClassLoader : ClassLoader() {
    val classes = mutableMapOf<String, Class<*>>()

    override fun loadClass(name: String): Class<*> {
        println("loadClass($name) = ${classes.getValue(name)}")
        return classes.getValue(name)
    }
}
