package org.dataland.frameworktoolbox.specific.specification

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandspecification.database.fs.FileSystemSpecificationDatabase
import org.dataland.datalandspecification.specifications.FrameworkSpecification
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository

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
        database.dataPointSpecifications.values
            .filter {
                it.frameworkOwnership == framework.identifier
            }.forEach { database.dataPointSpecifications.remove(it.id) }
    }

    private fun buildFrameworkSpecification() {
        database.frameworkSpecifications.remove(framework.identifier)
        val frameworkSpecification =
            FrameworkSpecification(
                id = framework.identifier,
                name = framework.label,
                businessDefinition = framework.explanation,
                schema = rootCategoryBuilder.toJsonNode(),
            )

        database.frameworkSpecifications[framework.identifier] = frameworkSpecification
    }

    /**
     * Build the framework specification and save it to the repository
     */
    fun build() {
        buildFrameworkSpecification()
        database.saveToDisk()
    }
}
