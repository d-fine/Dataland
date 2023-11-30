package org.dataland.frameworktoolbox.specific.datamodel

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.PackageBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
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

    private val frameworkBasePackageQualifier = "org.dataland.datalandbackend.frameworks.${framework.identifier}"

    val rootPackageBuilder: PackageBuilder = PackageBuilder(
        "$frameworkBasePackageQualifier.model",
        null,
    )

    val rootDataModelClass: DataClassBuilder = rootPackageBuilder.addClass(
        "${framework.identifier.capitalizeEn()}Data",
        "The root data-model for the ${framework.identifier.capitalizeEn()} Framework",
        mutableListOf(
            Annotation("org.dataland.datalandbackend.annotations.DataType", "\"${framework.identifier}\""),
        ),
    )

    private fun buildFrameworkSpecificApiController(into: DatalandRepository) {
        logger.trace("Building the framework-specific API Controller")
        val targetPath = into.backendKotlinSrc /
            frameworkBasePackageQualifier.replace(".", "/") /
            "${rootDataModelClass.name}Controller.kt"

        logger.trace("Building framework API controller for '{}' into '{}'", framework.identifier, targetPath)

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/datamodel/FrameworkDataController.kt.ftl")

        val writer = FileWriter(targetPath.toFile())
        freemarkerTemplate.process(
            mapOf(
                "frameworkDataType" to rootDataModelClass.getTypeReference(false),
                "frameworkCapitalizedName" to framework.identifier.capitalizeEn(),
                "frameworkIdentifier" to framework.identifier,
            ),
            writer,
        )
        writer.close()
    }

    /**
     * Generate the code for the DataModel and integrates it into the Dataland Repository.
     * Check if compilation succeeds and re-generates the OpenApi definition.
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build to backend data-model into the dataland-repository at ${into.path}")
        rootPackageBuilder.build(into)
        buildFrameworkSpecificApiController(into)

        logger.info("Generation completed. Verifying generated files and updating OpenApi-Spec")
        into.gradleInterface.executeGradleTasks(listOf("assemble"))
        into.gradleInterface.executeGradleTasks(listOf("dataland-backend:generateOpenApiDocs"), force = true)
        into.gradleInterface.executeGradleTasks(listOf("generateClients"))

        logger.info("Backend Data-Model generation was successful!")
    }
}
