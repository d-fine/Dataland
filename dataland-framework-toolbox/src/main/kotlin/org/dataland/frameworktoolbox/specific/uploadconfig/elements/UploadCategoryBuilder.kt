package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkUploadOptions
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import java.util.function.Consumer

/**
 * An In-Memory representation of a upload-configuration section
 * @param label the displayed label of the section
 * @param shouldDisplay a lambda deciding if this section should be displayed or not
 * @param children the elements contained in this section
 * @param labelBadgeColor the color of the badge in which the label is contained
 */

@Suppress("LongParameterList")
data class UploadCategoryBuilder(
    override val parentSection: UploadCategoryBuilder?,
    val name: String,
    var label: String,
    var shouldDisplay: FrameworkBooleanLambda,
    var children: MutableList<UploadConfigElement> = mutableListOf(),
    var labelBadgeColor: LabelBadgeColor? = null,
) : UploadConfigElement {
    override val imports: Set<TypeScriptImport>
        get() = children.foldRight(setOf()) { it, acc -> acc + it.imports }

    override fun traverse(lambda: Consumer<UploadConfigElement>) {
        lambda.accept(this)
        children.forEach {
            it.traverse(lambda)
        }
    }

    override fun assertComplianceWithLegacyUploadPage() {
        require(children.isNotEmpty()) {
            "It does not make sense to generate an empty upload-page category."
        }
        val firstChild = children[0]

        if (firstChild is UploadCategoryBuilder) {
            require(children.all { it is UploadCategoryBuilder }) {
                "You cannot mix and match sections and cells for the legacy upload page. "
            }
        } else {
            require(children.all { it is UploadCellConfigBuilder }) {
                "You cannot mix and match sections and cells for the legacy upload page. "
            }
            require(
                parentSection?.parentSection != null &&
                    parentSection.parentSection.parentSection == null,
            ) { "You must comply with the structure Section -> Subsection -> Cell " }
        }
    }

    /**
     * Adds a new subsection to this section
     */
    fun addSubcategory(
        identifier: String,
        label: String,
        labelBadgeColor: LabelBadgeColor?,
        shouldDisplay: FrameworkBooleanLambda,
    ): UploadCategoryBuilder {
        val newSection =
            UploadCategoryBuilder(
                parentSection = this,
                name = identifier,
                label = label,
                labelBadgeColor = labelBadgeColor,
                shouldDisplay = shouldDisplay,
            )
        children.add(newSection)
        return newSection
    }

    /**
     * Adds a new cell to this section
     */
    fun addUploadCell(
        identifier: String,
        label: String,
        explanation: String?,
        shouldDisplay: FrameworkBooleanLambda,
        unit: String?,
        required: Boolean,
        uploadComponentName: String,
        frameworkUploadOptions: FrameworkUploadOptions?,
        validation: String?,
    ): UploadCellConfigBuilder {
        val newCell =
            UploadCellConfigBuilder(
                parentSection = this,
                label = label,
                name = identifier,
                explanation = explanation,
                shouldDisplay = shouldDisplay,
                unit = unit,
                required = required,
                uploadComponentName = uploadComponentName,
                frameworkUploadOptions = frameworkUploadOptions,
                validation = validation,
            )
        children.add(newCell)
        return newCell
    }
}
