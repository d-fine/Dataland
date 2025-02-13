package org.dataland.datalandmessagequeueutils.constants

/**
 * Constants to be used as routing keys in rabbitmq.
 */

object RoutingKeyNames {
    const val DOCUMENT = "document"
    const val DOCUMENT_QA = "document.qa"
    const val DATA = "data"
    const val EMAIL = "email"
    const val META_DATA_PERSISTED = "metaDataPersisted"
    const val METAINFORMATION_PATCH = "metainformation.change"
    const val PRIVATE_DATA_AND_DOCUMENT = "privateDataAndDocument"
    const val DATA_POINT_UPLOAD = "dataPoint.upload"
    const val DATA_POINT_QA = "dataPoint.qa"
    const val DATASET_UPLOAD = "dataset.upload"
    const val DATASET_QA = "dataset.qa"
    const val DATASET_DELETION = "dataset.deletion"
    const val DATASET_STORED_TO_ASSEMBLED_MIGRATION = "dataset.storedDatasetMigratedToAssembledDataset"
    const val DATA_NONSOURCEABLE = "dataNonSourceable"
}
