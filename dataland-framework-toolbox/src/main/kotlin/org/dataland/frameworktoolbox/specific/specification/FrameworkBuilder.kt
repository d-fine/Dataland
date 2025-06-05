package org.dataland.frameworktoolbox.specific.specification

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandspecification.database.fs.FileSystemSpecificationDatabase
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.specification.elements.CategoryBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository

/**
 * A builder for a framework specification
 */
open class FrameworkBuilder(
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
}
