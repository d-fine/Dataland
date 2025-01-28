package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as queue names in rabbitmq.
 */

object QueueNames {
    const val INTERNAL_STORAGE_DATA_POINT_STORAGE = "internal-storage.storeDatapoints"
    const val INTERNAL_STORAGE_DATASET_STORAGE = "internal-storage.storeDatasets"
    const val INTERNAL_STORAGE_DATASET_DELETION = "internal-storage.deleteDatasets"
    const val QA_SERVICE_DATASET_QA = "qa-service.qaDatasets"
    const val QA_SERVICE_QA_ENTRY_PATCH = "qa-service.patchQaEntry"
    const val QA_SERVICE_DATA_POINT_QA = "qa-service.qaDataPoints"
    const val QA_SERVICE_DATASET_QA_DELETION = "qa-service.deleteDatasets"
    const val BACKEND_DATA_PERSISTED = "backend.removeDataFromMemory"
    const val BACKEND_DATA_POINT_QA_STATUS_UPDATED = "backend.updateDataPointQaStatus"
}
