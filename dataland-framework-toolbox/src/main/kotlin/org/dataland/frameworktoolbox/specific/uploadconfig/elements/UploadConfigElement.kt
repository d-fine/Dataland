package org.dataland.frameworktoolbox.specific.uploadconfig.elements

/**
 * A single element of a MLDT View-Configuration (either Cell or Section)
 */
sealed interface UploadConfigElement {
    val parentSection: UploadCategoryBuilder?

    fun assertComplianceWithLegacyUploadPage()
}
