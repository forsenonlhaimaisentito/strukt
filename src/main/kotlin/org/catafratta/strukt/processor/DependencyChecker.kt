package org.catafratta.strukt.processor

/**
 * This class is responsible for checking if the dependencies between structs can be satisfied
 * (e.g. there are no circular dependencies).
 */
internal class DependencyChecker {
    /**
     * Checks for missing or circular dependencies, throws [ProcessingException] if there is any.
     *
     * @throws ProcessingException in case of broken dependencies
     */
    fun check(structDefs: List<StructDef>) {
        val graph = structDefs.buildGraph() // This will automatically check for missing dependencies
        graph.checkForCycles()
    }

    private fun List<StructDef>.buildGraph(): List<Node> = map { it.toNode() }.populateDependencies()

    private fun StructDef.toNode() = Node(this, emptyList())

    private fun List<Node>.populateDependencies(): List<Node> = onEach { it.populateDependencies(this) }

    private fun Node.populateDependencies(available: List<Node>) {
        dependencies = structDef.fields
            .filterNot { it.typeName.isPrimitive || it.typeName.isPrimitiveArray }
            .map {
                available.findByName(it.typeName)
                    ?: throw ProcessingException("${it.name}: ${it.typeName} is not a struct", structDef.source)
            }
    }

    private fun List<Node>.findByName(name: String): Node? = find { it.structDef.name == name }

    private fun List<Node>.checkForCycles() {
        val explored = mutableSetOf<Node>()

        forEach {
            it.depthFirstSearch(explored)
            explored.add(it)
        }
    }

    private fun Node.depthFirstSearch(explored: MutableSet<Node>, exploring: MutableList<Node> = mutableListOf()) {
        exploring.add(this)

        dependencies.forEach { dep ->
            when (dep) {
                in explored -> return@forEach
                in exploring -> {
                    val cycleText = (exploring + this).joinToString(" → ") { it.structDef.name }
                    throw ProcessingException("Dependency cycle detected: $cycleText", structDef.source)
                }
                else -> dep.depthFirstSearch(explored, exploring)
            }
        }
    }

    /**
     * Represents a node in a dependency graph.
     */
    private data class Node(val structDef: StructDef, var dependencies: List<Node>) {
        override fun equals(other: Any?): Boolean = structDef == (other as? Node)?.structDef
        override fun hashCode(): Int = structDef.hashCode()
    }
}
