package org.dataland.datalandspecification.database

import org.dataland.datalandspecification.specifications.DataPointSpecification
import org.dataland.datalandspecification.specifications.DataPointTypeSpecification
import org.dataland.datalandspecification.specifications.FrameworkSpecification

/**
 * A specification database containing all specifications.
 */
abstract class SpecificationDatabase {
    val dataPointTypeSpecifications: MutableMap<String, DataPointTypeSpecification> = mutableMapOf()
    val dataPointSpecifications: MutableMap<String, DataPointSpecification> = mutableMapOf()
    val frameworkSpecifications: MutableMap<String, FrameworkSpecification> = mutableMapOf()
}
