package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda

/**
 * An In-Memory representation of a upload-configuration section
 * @param label the displayed label of the section
 * @param shouldDisplay a lambda deciding if this section should be displayed or not
 * @param children the elements contained in this section
 * @param labelBadgeColor the color of the badge in which the label is contained
 */

@Suppress("LongParameterList")
data class SectionUploadConfigBuilder(
    override val parentSection: SectionUploadConfigBuilder?,
    val name: String,
    var label: String,
    var shouldDisplay: FrameworkBooleanLambda,
    var children: MutableList<UploadConfigElement> = mutableListOf(),
    var labelBadgeColor: LabelBadgeColor? = null,
    val subcategory: Boolean,
) : UploadConfigElement {

    /**
     * Adds a new subsection to this section
     */
    fun addSection(
        identifier: String,
        label: String,
        labelBadgeColor: LabelBadgeColor?,
        shouldDisplay: FrameworkBooleanLambda,
        subcategory: Boolean,
    ): SectionUploadConfigBuilder {
        val newSection = SectionUploadConfigBuilder(
            parentSection = this,
            name = identifier,
            label = label,
            labelBadgeColor = labelBadgeColor,
            shouldDisplay = shouldDisplay,
            subcategory = subcategory,
        )
        children.add(newSection)
        return newSection
    }

    /**
     * Adds a new cell to this section
     */
    fun addCell(
        identifier: String,
        label: String,
        explanation: String?,
        shouldDisplay: FrameworkBooleanLambda,
        unit: String?,
        required: Boolean,
        uploadComponentName: String?,
        options: MutableSet<SelectionOption>?,
    ): CellConfigBuilder {
        val newCell = CellConfigBuilder(
            parentSection = this,
            label = label,
            name = identifier,
            explanation = explanation,
            shouldDisplay = shouldDisplay,
            unit = unit,
            required = required,
            uploadComponentName = uploadComponentName,
            options = options,
        )
        children.add(newCell)
        return newCell
    }
}
