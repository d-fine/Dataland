package org.dataland.datalandbackend.services.datapoints

import org.springframework.stereotype.Service

/**
 * Manages the migration of data from stored datasets to assembled datasets
 */
@Service
class AssembledDataMigrationManager {
    /**
     * Migrates the data from stored datasets to assembled datasets
     * @param dataId to migrate
     */
    fun migrateStoredDatasetToAssembledDataset(dataId: String) {
    }
}
