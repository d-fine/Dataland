package org.dataland.frameworktoolbox.specific.datamodel

import org.dataland.frameworktoolbox.intermediate.Framework
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
 * A FrameworkDataModelBuilder converts an Intermediate-Representation framework to a Kotlin-DataModel
 * and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkDataModelBuilder(
    private val framework: Framework,
) {
    private val logger by LoggerDelegate()

    private val frameworkBasePackageQualifier =
        "org.dataland.datalandbackend.frameworks.${removeUnallowedJavaIdentifierCharacters(framework.identifier)}"

    val rootPackageBuilder: PackageBuilder = PackageBuilder(
        "$frameworkBasePackageQualifier.model",
        null,
    )

    val rootDataModelClass: DataClassBuilder = rootPackageBuilder.addClass(
        "${getNameFromLabel(framework.identifier).capitalizeEn()}Data",
        "The root data-model for the ${framework.identifier.capitalizeEn()} Framework",
        mutableListOf(
            Annotation(
                "Suppress",
                "\"MagicNumber\"",
            ),
            Annotation(
                "org.dataland.datalandbackend.annotations.DataType",
                "\"${framework.identifier}\", ${framework.order}",
            ),
        ),
    )

    private fun buildFrameworkSpecificApiController(into: DatalandRepository, privateFrameworkBoolean: Boolean) {
        logger.trace("Building the framework-specific API Controller")
        val targetPath = into.backendKotlinSrc /
            frameworkBasePackageQualifier.replace(".", "/") /
            "${rootDataModelClass.name}Controller.kt"
        logger.trace("Building framework API controller for '{}' into '{}'", framework.identifier, targetPath)
        val nameOfDataApiController = if (privateFrameworkBoolean) {
            "/specific/datamodel/PrivateFrameworkDataController.kt.ftl"
        } else {
            "/specific/datamodel/PublicFrameworkDataController.kt.ftl"
        }
        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate(nameOfDataApiController)

        val writer = FileWriter(targetPath.toFile())
        freemarkerTemplate.process(
            mapOf(
                "frameworkIdentifier" to framework.identifier,
                "frameworkPackageName" to removeUnallowedJavaIdentifierCharacters(framework.identifier),
                "frameworkDataType" to rootDataModelClass.getTypeReference(false),
            ),
            writer,
        )
        writer.close()
    }

    /**
     * Generate the code for the DataModel and integrates it into the Dataland Repository.
     * Check if compilation succeeds and re-generates the OpenApi definition.
     */
    fun build(into: DatalandRepository, buildApiController: Boolean, privateFrameworkBoolean: Boolean) {
        logger.info("Starting to build to backend data-model into the dataland-repository at ${into.path}")
        rootPackageBuilder.build(into.backendKotlinSrc)

        if (buildApiController) {
            buildFrameworkSpecificApiController(into, privateFrameworkBoolean)
        }

        logger.info("Generation completed. Verifying generated files and updating OpenApi-Spec")
        into.gradleInterface.executeGradleTasks(listOf("assemble"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-backend:ktlintFormat"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-backend:generateOpenApiDocs"), force = true)
        into.gradleInterface.executeGradleTasks(listOf("generateClients"), force = true)

        logger.info("Backend Data-Model generation was successful!")
    }
}
