package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.specific.uploadconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * An In-Memory representation of a MLDTSectionConfig
 * @param label the displayed label of the section
 * @param expandOnPageLoad if true, the section is expanded when it is first rendered
 * @param shouldDisplay a lambda deciding if this section should be displayed or not
 * @param children the elements contained in this section
 * @param labelBadgeColor the color of the badge in which the label is contained
 */
data class SectionUploadConfigBuilder(
    override val parentSection: SectionUploadConfigBuilder?,
    val name: String,
    var label: String,
    var expandOnPageLoad: Boolean,
    var shouldDisplay: FrameworkBooleanLambda,
    var children: MutableList<UploadConfigElement> = mutableListOf(),
    var labelBadgeColor: LabelBadgeColor? = null,
) : UploadConfigElement {

    override val imports: Set<String>
        get() = children.foldRight(setOf()) { it, acc -> acc + it.imports }

    /**
     * Adds a new subsection to this section
     */
    fun addSection(
        label: String,
        labelBadgeColor: LabelBadgeColor?,
        expandOnPageLoad: Boolean,
        shouldDisplay: FrameworkBooleanLambda,
    ): SectionUploadConfigBuilder {
        val newSection = SectionUploadConfigBuilder(
            parentSection = this,
            name = camelCaseSify(label),
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
        unit: String?,
    ): CellConfigBuilder {
        val newCell = CellConfigBuilder(
            parentSection = this,
            label = label,
            name = camelCaseSify(label),
            unit = unit,
            explanation = explanation,
            shouldDisplay = shouldDisplay,
            valueGetter = valueGetter,
        )
        children.add(newCell)
        return newCell
    }

    private fun camelCaseSify(sentence: String): String {
        val words = sentence.trim().split(" ")
        val camelCasedList = mutableListOf(words[0].toLowerCase())
        val wordsReduced = words.subList(1, words.size)
        wordsReduced.forEach { entry ->
            run { camelCasedList.add(entry.capitalizeEn()) }
        }
        return camelCasedList.joinToString("")
    }
}
// todo 3 categories to add, not only these 2
