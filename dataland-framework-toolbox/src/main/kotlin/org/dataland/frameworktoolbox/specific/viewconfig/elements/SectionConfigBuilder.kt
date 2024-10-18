package org.dataland.frameworktoolbox.specific.viewconfig.elements

import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * An In-Memory representation of a MLDTSectionConfig
 * @param label the displayed label of the section
 * @param expandOnPageLoad if true, the section is expanded when it is first rendered
 * @param shouldDisplay a lambda deciding if this section should be displayed or not
 * @param children the elements contained in this section
 * @param labelBadgeColor the color of the badge in which the label is contained
 */
data class SectionConfigBuilder(
    override val parentSection: SectionConfigBuilder?,
    var label: String,
    var expandOnPageLoad: Boolean,
    var shouldDisplay: FrameworkBooleanLambda,
    var children: MutableList<ViewConfigElement> = mutableListOf(),
    var labelBadgeColor: LabelBadgeColor? = null,
) : ViewConfigElement {
    override val imports: Set<TypeScriptImport>
        get() = children.foldRight(setOf()) { it, acc -> acc + it.imports }

    /**
     * Adds a new subsection to this section
     */
    fun addSection(
        label: String,
        labelBadgeColor: LabelBadgeColor?,
        expandOnPageLoad: Boolean,
        shouldDisplay: FrameworkBooleanLambda,
    ): SectionConfigBuilder {
        val newSection =
            SectionConfigBuilder(
                parentSection = this,
                label = label,
                labelBadgeColor = labelBadgeColor,
                expandOnPageLoad = expandOnPageLoad,
                shouldDisplay = shouldDisplay,
            )
        children.add(newSection)
        return newSection
    }

    /**
     * Adds a new cell to this section
     */
    fun addCell(
        label: String,
        explanation: String?,
        shouldDisplay: FrameworkBooleanLambda,
        valueGetter: FrameworkDisplayValueLambda,
    ): CellConfigBuilder {
        val newCell =
            CellConfigBuilder(
                parentSection = this,
                label = label,
                explanation = explanation,
                shouldDisplay = shouldDisplay,
                valueGetter = valueGetter,
            )
        children.add(newCell)
        return newCell
    }
}
