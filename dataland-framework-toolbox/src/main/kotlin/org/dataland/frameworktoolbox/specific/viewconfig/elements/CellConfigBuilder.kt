package org.dataland.frameworktoolbox.specific.viewconfig.elements

import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * An In-Memory representation of a MLDTCellConfig.
 * @param label the displayed label of the row
 * @param explanation a detailed explanation of the content of the row (displayed as a tooltip)
 * @param shouldDisplay a lambda deciding if this function should be displayed or not
 * @param valueGetter a lambda deciding the content of this cell based on the framework dataset
 * @param uploadComponentName Name of the component used for uploading/editing this cell's value.
 * @param dataPointTypeId Identifier for the data point type associated with this cell.
 */
@Suppress("LongParameterList")
class CellConfigBuilder(
    override val parentSection: SectionConfigBuilder?,
    var label: String,
    var explanation: String?,
    var shouldDisplay: FrameworkBooleanLambda,
    var valueGetter: FrameworkDisplayValueLambda,
    var uploadComponentName: String,
    var dataPointTypeId: String,
) : ViewConfigElement {
    override val imports: Set<TypeScriptImport>
        get() = valueGetter.imports
}
