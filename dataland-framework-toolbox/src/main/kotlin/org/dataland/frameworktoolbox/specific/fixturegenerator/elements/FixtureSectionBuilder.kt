package org.dataland.frameworktoolbox.specific.fixturegenerator.elements

import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A FixtureSectionBuilder is an in-memory Representation of fixture generator for a
 * JS Object
 * @param identifier the name of the object
 * @param parentSection the containing object
 * @param elements the contained elements
 */
class FixtureSectionBuilder(
    override val identifier: String,
    val parentSection: FixtureSectionBuilder?,
    val elements: MutableList<FixtureGeneratorElement>,
) : FixtureGeneratorElement {
    override val imports: Set<TypeScriptImport>
        get() =
            elements.foldRight(emptySet()) { element, imports ->
                imports + element.imports
            }

    private fun <T : FixtureGeneratorElement> addElement(element: T): T {
        require(elements.none { it.identifier == element.identifier }) {
            "The identifier ${element.identifier} is already in use"
        }
        elements.add(element)
        return element
    }

    /**
     * Add a new section to this object
     */
    fun addSection(identifier: String): FixtureSectionBuilder = addElement(FixtureSectionBuilder(identifier, this, mutableListOf()))

    /**
     * Add a new atomic TS expression to this object
     */
    fun addAtomicExpression(
        identifier: String,
        typescriptExpression: String,
        imports: Set<TypeScriptImport> = emptySet(),
    ): FixtureAtomicExpression =
        addElement(
            FixtureAtomicExpression(
                identifier = identifier,
                typescriptExpression = typescriptExpression,
                imports = imports,
            ),
        )
}
