package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.datapoints.addPropertyWithDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.specific.qamodel.addQaPropertyWithDocumentSupport

/**
 * The CustomComponent class is used in conjunction with the CustomComponentFactory.
 * The CustomComponent is a meta custom component that is able to represent any custom component, generated
 * by the CustomComponentFactory.
 * Importantly, the qualifiedNameRelativeToFrameworkRoot needs to be set after initializing the component such
 * that the correct class name is used.
 *
 * At the moment we assume that the custom component is non-nullable. This is inline with the CustomComponentFactory
 * that generates non-nullable properties of the data.
 * Thus, the fixture generator assumes that everything is non-nullable.
 */
class CustomComponent(
    identifier: String,
    parent: FieldNodeParent,
) : ComponentBase(identifier, parent) {

    companion object {
        /**
         * This function retrieves the fully qualified name of the root package for a given [packageBuilder].
         */
        private fun getRootFullyQualifiedName(packageBuilder: PackageBuilder): String =
            packageBuilder.parentPackage?.let { getRootFullyQualifiedName(it) } ?: packageBuilder.fullyQualifiedName
    }

    lateinit var qualifiedNameRelativeToFrameworkRoot: String

    private fun fullyQualifiedName(dataClassBuilder: DataClassBuilder) =
        getRootFullyQualifiedName(dataClassBuilder.parentPackage) + "." + qualifiedNameRelativeToFrameworkRoot

    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(
                fullyQualifiedName(dataClassBuilder),
                isNullable,
            ),
            emptyList(),
        )
    }

    override fun generateDefaultQaModel(dataClassBuilder: DataClassBuilder) {
        dataClassBuilder.addQaPropertyWithDocumentSupport(
            documentSupport,
            identifier,
            TypeReference(fullyQualifiedName(dataClassBuilder), isNullable),
        )
    }

    override fun generateDefaultFixtureGenerator(sectionBuilder: FixtureSectionBuilder) {
        val className = qualifiedNameRelativeToFrameworkRoot.split(".").last()
        sectionBuilder.addAtomicExpression(
            identifier,
            documentSupport.getFixtureExpression(
                "dataGenerator.${CustomComponentFactory.NULLABLE_GENERATOR_PREFIX}$className()",
                "",
                this.isNullable,
            ),
        )
    }
}
