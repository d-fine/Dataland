package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as queue names in rabbitmq.
 */

object QueueNames {
    const val INTERNAL_STORAGE_DATA_POINT_STORAGE = "internal-storage.storeDatapoints"
    const val INTERNAL_STORAGE_DATASET_STORAGE = "internal-storage.storeDatasets"
    const val INTERNAL_STORAGE_DATASET_DELETION = "internal-storage.deleteDatasets"
    const val QA_SERVICE_DATASET_QA = "qa-service.qaDatasets"
    const val QA_SERVICE_DATA_POINT_QA = "qa-service.qaDataPoints"
    const val QA_SERVICE_DATASET_QA_DELETION = "qa-service.deleteDatasets"
    const val QA_SERVICE_DATASET_MIGRATION = "qa-service.migrateDatasets"
    const val BACKEND_DATA_PERSISTED = "backend.removeDataFromMemory"
    const val BACKEND_DATA_POINT_QA_STATUS_UPDATED = "backend.updateDataPointQaStatus"
    const val USER_SERVICE_PORTFOLIO_UPDATE = "user-service.updatePortfolio"
    const val DATA_SOURCING_SERVICE_DATASET_QA_STATUS_UPDATE = "data-sourcing-service.datasetQaStatusUpdate"
    const val USER_SERVICE_NON_SOURCABLE_EVENT = "user-service.processMessageForDataReportedAsNonSourceable"
    const val USER_SERVICE_QA_STATUS_UPDATE_EVENT = "user-service.processMessageForAvailableDataAndUpdates"
    const val ACCOUNTING_SERVICE_REQUEST_PROCESSING = "accounting-service.requestProcessing"
    const val ACCOUNTING_SERVICE_REQUEST_WITHDRAWN = "accounting-service.requestWithdrawn"
    const val QA_SERVICE_NON_SOURCEABILITY_CREATED = "qa-service.processNonSourceabilityCreated"
    const val DATA_SOURCING_SERVICE_NON_SOURCEABILITY_CREATED = "data-sourcing-service.processNonSourceabilityCreated"
    const val DATA_SOURCING_SERVICE_NON_SOURCEABILITY_AUTO_ACCEPTED = "data-sourcing-service.processNonSourceabilityAutoAccepted"
    const val BACKEND_NON_SOURCEABILITY_QA_DECISION = "backend.processNonSourceabilityQaDecision"
    const val DATA_SOURCING_SERVICE_NON_SOURCEABILITY_QA_DECISION = "data-sourcing-service.processNonSourceabilityQaDecision"
}
