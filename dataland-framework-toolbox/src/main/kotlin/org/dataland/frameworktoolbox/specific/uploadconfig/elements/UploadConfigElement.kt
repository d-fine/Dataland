package org.dataland.frameworktoolbox.specific.uploadconfig.elements

/**
 * A single element of a MLDT View-Configuration (either Cell or Section)
 */
sealed interface UploadConfigElement {
    val parentSection: UploadCategoryBuilder?

    /**
     * When this function passes without errors, it is guaranteed that this config element
     * is compliant with the limitations of the legacy upload page and code generation can proceed
     */
    fun assertComplianceWithLegacyUploadPage()
}
