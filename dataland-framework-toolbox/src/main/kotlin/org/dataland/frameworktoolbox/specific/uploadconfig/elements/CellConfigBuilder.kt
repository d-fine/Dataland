package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkDisplayValueLambda

/**
 * An In-Memory representation of a MLDTCellConfig.
 * @param label the displayed label of the row
 * @param explanation a detailed explanation of the content of the row (displayed as a tooltip)
 * @param shouldDisplay a lambda deciding if this function should be displayed or not
 * @param valueGetter a lambda deciding the content of this cell based on the framework dataset
 */
class CellConfigBuilder(
    override val parentSection: SectionUploadConfigBuilder?,
    var label: String,
    var name: String,
    var explanation: String?,
    var shouldDisplay: FrameworkBooleanLambda,
    var valueGetter: FrameworkDisplayValueLambda,
        var unit: String?,
) : UploadConfigElement {
    override val imports: Set<String>
        get() = valueGetter.imports
}
