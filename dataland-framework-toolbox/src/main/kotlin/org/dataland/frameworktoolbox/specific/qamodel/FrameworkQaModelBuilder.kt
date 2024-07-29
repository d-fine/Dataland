package org.dataland.frameworktoolbox.specific.qamodel

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.Naming.getNameFromLabel
import org.dataland.frameworktoolbox.utils.Naming.removeUnallowedJavaIdentifierCharacters
import org.dataland.frameworktoolbox.utils.capitalizeEn

/**
 * A FrameworkDataModelBuilder converts an Intermediate-Representation framework to a Kotlin-DataModel for QA
 * and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkQaModelBuilder(
    private val framework: Framework,
) {
    private val logger by LoggerDelegate()

    private val frameworkBasePackageQualifier =
        "org.dataland.datalandqaservice.frameworks.${removeUnallowedJavaIdentifierCharacters(framework.identifier)}"

    val rootPackageBuilder: PackageBuilder = PackageBuilder(
        "$frameworkBasePackageQualifier.model",
        null,
    )

    val rootDataModelClass: DataClassBuilder = rootPackageBuilder.addClass(
        "${getNameFromLabel(framework.identifier).capitalizeEn()}Data",
        "The root QA data-model for the ${framework.identifier.capitalizeEn()} Framework",
        mutableListOf(),
    )

    /**
     * Builds the QA data-model into the given Dataland Repository
     * @param into the Dataland Repository to build the QA data-model into
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build to QA data-model into the dataland-repository at ${into.path}")
        rootPackageBuilder.build(into.qaKotlinSrc)

        logger.info("Generation completed. Verifying generated files and updating OpenApi-Spec")
        into.gradleInterface.executeGradleTasks(listOf("assemble"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-qa-service:ktlintFormat"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-qa-service:generateOpenApiDocs"), force = true)
        into.gradleInterface.executeGradleTasks(listOf("generateClients"))

        logger.info("QA Data-Model generation was successful!")
    }
}
