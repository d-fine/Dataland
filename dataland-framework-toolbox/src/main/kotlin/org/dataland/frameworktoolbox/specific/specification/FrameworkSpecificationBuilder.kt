package org.dataland.frameworktoolbox.specific.specification

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandspecification.database.fs.FileSystemSpecificationDatabase
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getJsonPath
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.datalandspecification.specifications.Framework as FrameworkSpecification

/**
 * A builder for a framework specification
 */
class FrameworkSpecificationBuilder(
    val framework: Framework,
    datalandRepository: DatalandRepository,
) {
    val rootCategoryBuilder =
        CategoryBuilder(
            identifier = framework.label,
            parentCategory = null,
            builder = this,
        )

    val database: FileSystemSpecificationDatabase =
        FileSystemSpecificationDatabase(
            datalandRepository.specificationDatabasePath.toFile(),
            jacksonObjectMapper(),
        )

    init {
        database.dataPointTypes.values
            .filter {
                it.frameworkOwnership.contains(framework.identifier)
            }.forEach {
                if (it.frameworkOwnership.size == 1) {
                    database.dataPointTypes.remove(it.id)
                } else {
                    database.dataPointTypes[it.id] = it.copy(frameworkOwnership = it.frameworkOwnership - framework.identifier)
                }
            }
    }

    private fun buildFrameworkSpecification() {
        database.frameworks.remove(framework.identifier)

        val referencedReportPath =
            framework.root.nestedChildren.find { it is ReportPreuploadComponent }?.let {
                it.getJsonPath()
            }

        val frameworkSpecification =
            FrameworkSpecification(
                id = framework.identifier,
                name = framework.label,
                businessDefinition = framework.explanation,
                schema = rootCategoryBuilder.toJsonNode(),
                referencedReportJsonPath = referencedReportPath,
            )

        database.frameworks[this.framework.identifier] = frameworkSpecification
    }

    /**
     * Build the framework specification and save it to the repository
     */
    fun build() {
        buildFrameworkSpecification()
        database.saveToDisk()
    }
}
