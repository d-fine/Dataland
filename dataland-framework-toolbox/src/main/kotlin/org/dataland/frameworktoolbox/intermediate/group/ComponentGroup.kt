package org.dataland.frameworktoolbox.intermediate.group

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
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
) : ComponentBase(identifier, parent),
    FieldNodeParent,
    ComponentGroupApi by componentGroupApi {
    var viewPageLabelBadgeColor: LabelBadgeColor? = null
    var uploadPageLabelBadgeColor: LabelBadgeColor? = null
    var viewPageExpandOnPageLoad: Boolean = true

    override val children: Sequence<ComponentBase> by componentGroupApi::children

    override var isNullable: Boolean
        get() = super.isNullable && nestedChildren.all { it.isNullable }
        set(value) {
            super.isNullable = value
        }

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        val groupPackage = dataClassBuilder.parentPackage.addPackage(identifier)
        val groupClass =
            groupPackage.addClass(
                camelCaseComponentIdentifier,
                "The data-model for the ${identifier.capitalizeEn()} section",
            )

        children.forEach {
            it.generateDataModel(groupClass)
        }

        dataClassBuilder.addProperty(
            identifier,
            groupClass.getTypeReference(isNullable),
            listOf(ValidAnnotation),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        val groupPackage = dataClassBuilder.parentPackage.addPackage(identifier)
        val groupClass =
            groupPackage.addClass(
                camelCaseComponentIdentifier,
                "The QA-model for the ${identifier.capitalizeEn()} section",
            )

        children.forEach {
            it.generateQaModel(groupClass)
        }

        if (!groupClass.empty) {
            dataClassBuilder.addProperty(
                identifier,
                groupClass.getTypeReference(groupClass.allNullable),
                listOf(ValidAnnotation),
            )
        }

        if (groupPackage.empty) {
            dataClassBuilder.parentPackage.childElements.remove(groupPackage)
        }
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        val localLabel = label
        require(!localLabel.isNullOrBlank()) {
            "You must specify a label for the group $identifier to generate a view configuration"
        }
        val containerSection =
            sectionConfigBuilder.addSection(
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
        val containerSection =
            uploadCategoryBuilder.addSubcategory(
                identifier = identifier,
                label = localLabel,
                labelBadgeColor = uploadPageLabelBadgeColor,
                shouldDisplay = FrameworkBooleanLambda.TRUE,
            )

        children.forEach {
            it.generateUploadConfig(containerSection)
        }
    }

    override fun generateDefaultSpecification(specificationCategoryBuilder: CategoryBuilder) {
        val containerCategory =
            specificationCategoryBuilder.addCategory(
                identifier = identifier,
            )

        children.forEach {
            it.generateSpecification(containerCategory)
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
