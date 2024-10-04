package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder

class CustomComponent (
    identifier: String,
    parent: FieldNodeParent,
    ) : ComponentBase(identifier, parent) {

    companion object {
        fun frameworkRoot(packageBuilder: PackageBuilder): String =
            packageBuilder.parentPackage?.let { frameworkRoot(it) } ?: packageBuilder.fullyQualifiedName
    }

    lateinit var qualifiedNameRelativeToFrameworkRoot: String

    private fun fullyQualifiedName(dataClassBuilder: DataClassBuilder) =
        frameworkRoot(dataClassBuilder.parentPackage) + "." + qualifiedNameRelativeToFrameworkRoot

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                fullyQualifiedName(dataClassBuilder),
                isNullable,
            ),
            emptyList()
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedName(dataClassBuilder), isNullable),
        )
    }

    override fun generateDefaultUploadConfig(uploadCategoryBuilder: UploadCategoryBuilder) {
        // TODO
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        // TODO
    }

    override fun generateDefaultViewConfig(sectionConfigBuilder: SectionConfigBuilder) {
        // TODO
    }
}