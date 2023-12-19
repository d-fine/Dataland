package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * An In-Memory representation of a MLDTCellConfig.
 * @param label the displayed label of the row
 * @param explanation a detailed explanation of the content of the row (displayed as a tooltip)
 * @param shouldDisplay a lambda deciding if this function should be displayed or not
 */

@Suppress("LongParameterList")
class CellConfigBuilder(
    override val parentSection: UploadCategoryBuilder,
    var label: String,
    var name: String,
    var explanation: String?,
    var shouldDisplay: FrameworkBooleanLambda,
    var unit: String?,
    var required: Boolean,
    var uploadComponentName: String,
    var options: MutableSet<SelectionOption>?,
    var frameworkUploadOptions: FrameworkUploadOptions?,
) : UploadConfigElement {
    override fun assertComplianceWithLegacyUploadPage() { /* BLANK */ }
}
