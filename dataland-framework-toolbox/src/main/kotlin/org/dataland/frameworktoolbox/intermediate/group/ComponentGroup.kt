package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.LabelBadgeColor
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * A collection of components (i.e., a section or subsection).
 */
class ComponentGroup(
    identifier: String,
    parent: FieldNodeParent,
    private val componentGroupApi: ComponentGroupApiImpl = ComponentGroupApiImpl(),
) : ComponentBase(identifier, parent), FieldNodeParent, ComponentGroupApi by componentGroupApi {

    var viewPageLabelBadgeColor: LabelBadgeColor? = null
    var uploadPageLabelBadgeColor: LabelBadgeColor? = null
    var viewPageExpandOnPageLoad: Boolean = false

    override val children: Sequence<ComponentBase> by componentGroupApi::children

    val camelCaseComponentIdentifier: String
        get() {
            return parents()
                .toList()
                .reversed()
                .mapNotNull {
                    when (it) {
                        is ComponentGroup -> it.identifier.capitalizeEn()
                        is TopLevelComponentGroup -> it.parent.identifier.capitalizeEn()
                        else -> null
                    }
                }.joinToString("") + identifier.capitalizeEn()
        }

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val groupPackage = dataClassBuilder.parentPackage.addPackage(identifier)
        val groupClass = groupPackage.addClass(
            camelCaseComponentIdentifier,
            "The data-model for the ${identifier.capitalizeEn()} section",
        )

        children.forEach {
            it.generateDataModel(groupClass)
        }

        dataClassBuilder.addProperty(
            identifier,
            groupClass.getTypeReference(nullable = isNullable),
        )
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        val localLabel = label
        require(!localLabel.isNullOrBlank()) {
            "You must specify a label for the group $identifier to generate a view configuration"
        }
        val containerSection = sectionConfigBuilder.addSection(
            label = localLabel,
            labelBadgeColor = viewPageLabelBadgeColor,
            expandOnPageLoad = viewPageExpandOnPageLoad,
            shouldDisplay = FrameworkBooleanLambda.TRUE,
        )

        children.forEach {
            it.generateViewConfig(containerSection)
        }
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        val localLabel = label
        require(!localLabel.isNullOrBlank()) {
            "You must specify a label for the group $identifier to generate a view configuration"
        }
        val containerSection = uploadCategoryBuilder.addSubcategory(
            identifier = identifier,
            label = localLabel,
            labelBadgeColor = uploadPageLabelBadgeColor,
            shouldDisplay = FrameworkBooleanLambda.TRUE,
        )

        children.forEach {
            it.generateUploadConfig(containerSection)
        }
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val groupSection = sectionBuilder.addSection(identifier)
        children.forEach {
            it.generateFixtureGenerator(groupSection)
        }
    }

    init {
        componentGroupApi.parent = this
    }
}
