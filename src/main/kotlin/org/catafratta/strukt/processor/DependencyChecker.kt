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
    fun check(structs: List<DeclaredStruct>) {
        val graph = structs.buildGraph() // This will automatically check for missing dependencies
        graph.checkForCycles()
    }

    private fun List<DeclaredStruct>.buildGraph(): List<Node> = map { it.toNode() }.populateDependencies()

    private fun DeclaredStruct.toNode() = Node(this, emptyList())

    private fun List<Node>.populateDependencies(): List<Node> = onEach { it.populateDependencies(this) }

    private fun Node.populateDependencies(available: List<Node>) {
        dependencies = struct.fields
            .filterNot { it.typeName.isPrimitive }
            .map {
                available.findByName(it.typeName)
                    ?: throw ProcessingException("${it.name}: ${it.typeName} is not a struct", struct.source)
            }
    }

    private fun List<Node>.findByName(name: String): Node? = find { it.struct.name == name }

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
                    val cycleText = (exploring + this).joinToString(" → ") { it.struct.name }
                    throw ProcessingException("Dependency cycle detected: $cycleText", struct.source)
                }
                else -> dep.depthFirstSearch(explored, exploring)
            }
        }
    }

    /**
     * Represents a node in a dependency graph.
     */
    private data class Node(val struct: DeclaredStruct, var dependencies: List<Node>) {
        override fun equals(other: Any?): Boolean = struct == (other as? Node)?.struct
        override fun hashCode(): Int = struct.hashCode()
    }
}
