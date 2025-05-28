package org.dataland.frameworktoolbox.specific.specification

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getJsonPath
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.datalandspecification.specifications.Framework as FrameworkSpecification

/**
 * A builder for a framework specification
 */
class FrameworkSpecificationBuilder(
    framework: Framework,
    datalandRepository: DatalandRepository,
) : FrameworkBuilder(framework, datalandRepository) {
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
