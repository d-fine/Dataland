package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.springframework.stereotype.Service

/**
 * A service class for managing the composition of datasets
 */
@Service
class DataPointCompositionService {
    /**
     * Checks if a dataset is a lego brick dataset
     */
    fun isLegoBrickDataset(dataId: String): Boolean {
        // TODO: REPLACE WITH ACTUAL LOGIC!!!!
        return false
    }

    /**
     * Gets the composition of a dataset as a map of DataPointId to DataPointDataId
     */
    fun getCompositionOfDataSet(dataId: String): Map<String, String> {
        // TODO: REPLACE WITH ACTUAL LOGIC!!!!
        return mapOf("extendedCurrencyEquity" to "3cca1283-2a75-4a23-93d6-2d2469cf139f")
    }
}
