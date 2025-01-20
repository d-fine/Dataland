package org.dataland.datalandspecification.database

import org.dataland.datalandspecification.specifications.DataPointBaseType
import org.dataland.datalandspecification.specifications.DataPointType
import org.dataland.datalandspecification.specifications.Framework

/**
 * A specification database containing all specifications.
 */
abstract class SpecificationDatabase {
    val dataPointBaseTypes: MutableMap<String, DataPointBaseType> = mutableMapOf()
    val dataPointTypes: MutableMap<String, DataPointType> = mutableMapOf()
    val frameworks: MutableMap<String, Framework> = mutableMapOf()
}
