package org.dataland.frameworktoolbox.specific.qamodel

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.Naming.getNameFromLabel
import org.dataland.frameworktoolbox.utils.Naming.removeUnallowedJavaIdentifierCharacters
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import kotlin.io.path.div

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

    val rootPackageBuilder: PackageBuilder =
        PackageBuilder(
            "$frameworkBasePackageQualifier.model",
            null,
        )

    val rootDataModelClass: DataClassBuilder =
        rootPackageBuilder.addClass(
            "${getNameFromLabel(framework.identifier).capitalizeEn()}Data",
            "The root QA data-model for the ${framework.identifier.capitalizeEn()} Framework",
            mutableListOf(),
        )

    private val standardQaReportManagerTypeReference =
        TypeReference(
            "org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportManager",
            false,
        )

    private val assembledDatasetQaReportManagerTypeReference =
        TypeReference(
            "org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.AssembledDatasetQaReportManager",
            false,
        )

    private fun buildFrameworkSpecificApiController(
        into: DatalandRepository,
        assembledDataset: Boolean,
    ) {
        logger.trace("Building the framework-specific QA Controller")
        val targetPath =
            into.qaKotlinSrc /
                frameworkBasePackageQualifier.replace(".", "/") /
                "${rootDataModelClass.name}QaReportController.kt"
        logger.trace("Building framework QA controller for '{}' into '{}'", framework.identifier, targetPath)
        val freemarkerTemplate =
            FreeMarker.configuration
                .getTemplate("/specific/qamodel/FrameworkQaController.kt.ftl")

        val reportManager = if (assembledDataset) assembledDatasetQaReportManagerTypeReference else standardQaReportManagerTypeReference

        val writer = FileWriter(targetPath.toFile())
        freemarkerTemplate.process(
            mapOf(
                "frameworkIdentifier" to framework.identifier,
                "frameworkPackageName" to removeUnallowedJavaIdentifierCharacters(framework.identifier),
                "frameworkDataType" to rootDataModelClass.getTypeReference(false),
                "frameworkQaReportManagerDataType" to reportManager,
            ),
            writer,
        )
        writer.close()
    }

    /**
     * Builds the QA data-model into the given Dataland Repository
     * @param into the Dataland Repository to build the QA data-model into
     */
    fun build(
        into: DatalandRepository,
        assembledDataset: Boolean,
    ) {
        logger.info("Starting to build to QA data-model into the dataland-repository at ${into.path}")
        rootPackageBuilder.build(into.qaKotlinSrc)

        buildFrameworkSpecificApiController(into, assembledDataset)

        logger.info("Generation completed. Verifying generated files and updating OpenApi-Spec")
        into.gradleInterface.executeGradleTasks(listOf("assemble"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-qa-service:ktlintFormat"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-qa-service:generateOpenApiDocs"), force = true)
        into.gradleInterface.executeGradleTasks(listOf("generateClients"), force = true)

        logger.info("QA Data-Model generation was successful!")
    }
}
