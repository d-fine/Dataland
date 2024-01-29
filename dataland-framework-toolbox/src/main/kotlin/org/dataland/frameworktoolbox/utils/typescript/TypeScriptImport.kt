package org.dataland.frameworktoolbox.utils.typescript

/**
 * A TypeScriptImport is a representation of a member-import statement
 */
data class TypeScriptImport(
    val members: Set<String>,
    val file: String,
) {
    companion object {
        fun mergeImports(imports: Set<TypeScriptImport>): Set<TypeScriptImport> {
            val perFileImports = mutableMapOf<String, MutableSet<String>>()
            imports.forEach {
                val perFileImportSet = perFileImports.getOrPut(it.file) { mutableSetOf() }
                perFileImportSet.addAll(it.members)
            }
            return perFileImports.map { TypeScriptImport(it.value, it.key) }.toSet()
        }
    }
    constructor(member: String, file: String) : this(setOf(member), file)
}
