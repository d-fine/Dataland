package org.dataland.frameworktoolbox.specific.fixturegenerator.elements

/**
 * A FixtureGeneratorElement is part of the fixture-generation hierarchy
 */
sealed interface FixtureGeneratorElement {
    val identifier: String
    val imports: Set<String>

    /**
     * Returns true iff this is an instance of a section (required for FreeMarker)
     */
    fun isSection() = this is FixtureSectionBuilder

    /**
     * Returns true iff this is an instance of an Atomic Expression (required for FreeMarker)
     */
    fun isAtomicExpression() = this is FixtureAtomicExpression
}
