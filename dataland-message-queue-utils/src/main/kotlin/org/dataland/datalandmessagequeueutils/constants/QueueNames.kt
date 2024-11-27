package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as queue names in rabbitmq.
 */

object QueueNames {
    const val DATA_POINT_STORAGE = "internal-storage.storeDatapoints"
    const val DATASET_STORAGE = "internal-storage.storeDatasets"
    const val DATASET_DELETION = "internal-storage.deleteDatasets"
    const val DATASET_QA = "qa-service.qaDatasets"
    const val DATA_POINT_QA = "qa-service.qaDataPoints"
    const val DATA_PERSISTED = "backend.removeDataFromMemory"
    const val DATA_POINT_QA_CHANGED = "backend.dataPointQaStatusChange"
    const val DATASET_QA_DELETION = "qa-service.deleteDatasets"
}
