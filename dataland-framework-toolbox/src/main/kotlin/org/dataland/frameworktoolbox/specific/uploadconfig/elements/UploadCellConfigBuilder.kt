package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import java.util.function.Consumer

/**
 * An In-Memory representation of a MLDTCellConfig.
 * @param label the displayed label of the row
 * @param explanation a detailed explanation of the content of the row (displayed as a tooltip)
 * @param shouldDisplay a lambda deciding if this function should be displayed or not
 */

@Suppress("LongParameterList")
class UploadCellConfigBuilder(
    override val parentSection: UploadCategoryBuilder,
    var label: String,
    var name: String,
    var explanation: String?,
    var shouldDisplay: FrameworkBooleanLambda,
    var unit: String?,
    var required: Boolean,
    var uploadComponentName: String,
    var frameworkUploadOptions: FrameworkUploadOptions?,
    var validation: String?,
) : UploadConfigElement {
    override fun traverse(lambda: Consumer<UploadConfigElement>) {
        lambda.accept(this)
    }

    override fun assertComplianceWithLegacyUploadPage() { /* BLANK */ }

    override val imports: Set<TypeScriptImport>
        get() = frameworkUploadOptions?.imports ?: emptySet()
}
