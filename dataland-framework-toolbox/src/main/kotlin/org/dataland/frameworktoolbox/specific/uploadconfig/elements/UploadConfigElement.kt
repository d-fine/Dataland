package org.dataland.frameworktoolbox.specific.uploadconfig.elements

/**
 * A single element of a MLDT View-Configuration (either Cell or Section)
 */
sealed interface UploadConfigElement {
    val parentSection: SectionUploadConfigBuilder?

    /**
     * Returns true iff this is an instance of a section (required for FreeMarker)
     */
    fun isSection() = this is SectionUploadConfigBuilder

    /**
     * Returns true iff this is an instance of a cell (required for FreeMarker)
     */
    fun isCell() = this is CellConfigBuilder

    /**
     * Returns true iff this is subcategory (required for FreeMarker)
     */
    fun isSubcategory(): Boolean {
        return parentSection?.subcategory ?: false
    }
}
