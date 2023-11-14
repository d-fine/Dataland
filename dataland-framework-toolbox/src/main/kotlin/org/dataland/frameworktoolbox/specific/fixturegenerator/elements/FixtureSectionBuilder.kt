package org.dataland.frameworktoolbox.specific.fixturegenerator.elements

class FixtureSectionBuilder(
    override val identifier: String,
    val parentSection: FixtureSectionBuilder?,
    val elements: MutableList<FixtureGeneratorElement>,
) : FixtureGeneratorElement {

    override val imports: Set<String> =
        elements.foldRight(emptySet()) {
                element, imports ->
            imports + element.imports
        }

    private fun <T : FixtureGeneratorElement> addElement(element: T): T {
        require(elements.none { it.identifier == element.identifier }) {
            "The identifier ${element.identifier} is already in use"
        }
        elements.add(element)
        return element
    }
    fun addSection(identifier: String): FixtureSectionBuilder {
        return addElement(FixtureSectionBuilder(identifier, this, mutableListOf()))
    }

    fun addAtomicExpression(identifier: String, typescriptExpression: String, imports: Set<String> = emptySet()): FixtureAtomicExpression {
        return addElement(
            FixtureAtomicExpression(
                identifier = identifier,
                typescriptExpression = typescriptExpression,
                imports = imports,
            ),
        )
    }
}
