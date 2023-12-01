package org.dataland.frameworktoolbox.specific.viewconfig.elements

import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * An In-Memory representation of a MLDTCellConfig.
 * @param label the displayed label of the row
 * @param explanation a detailed explanation of the content of the row (displayed as a tooltip)
 * @param shouldDisplay a lambda deciding if this function should be displayed or not
 * @param valueGetter a lambda deciding the content of this cell based on the framework dataset
 */
class CellConfigBuilder(
    override val parentSection: SectionConfigBuilder?,
    var label: String,
    var explanation: String?,
    var shouldDisplay: FrameworkBooleanLambda,
    var valueGetter: FrameworkDisplayValueLambda,
) : ViewConfigElement {
    override val imports: Set<String>
        get() = valueGetter.imports
}
