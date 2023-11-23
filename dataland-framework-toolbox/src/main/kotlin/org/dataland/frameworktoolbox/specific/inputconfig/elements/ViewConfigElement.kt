package org.dataland.frameworktoolbox.specific.inputconfig.elements

/**
 * A single element of a MLDT View-Configuration (either Cell or Section)
 */
sealed interface ViewConfigElement {
    val parentSection: SectionInputConfigBuilder?
    val imports: Set<String>

    /**
     * Returns true iff this is an instance of a section (required for FreeMarker)
     */
    fun isSection() = this is SectionInputConfigBuilder

    /**
     * Returns true iff this is an instance of a cell (required for FreeMarker)
     */
    fun isCell() = this is CellConfigBuilder
}
