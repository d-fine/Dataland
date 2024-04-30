package org.dataland.datalandbackend

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * Enables a centralized generation of log messages for all Dataland backend operations.
 */

@Component("ExceptionHandlingUtils")
class DatamanagerUtils {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles exceptions thrown by the data manager process while retrieving data by id
     * @param e the thrown client exception
     * @param dataId the dataId for which a exception was thrown
     * @param correlationId the correlationId of the request which caused the exception to be thrown
     */
    fun handleStorageClientException(e: ClientException, dataId: String, correlationId: String) {
        if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
            logger.info("Dataset with id $dataId could not be found. Correlation ID: $correlationId")
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
                e,
            )
        } else {
            throw e
        }
    }

    /**
     * This method asserts that the expected data type for a dataset corresponds with the data type stored in the meta
     * information for this dataset
     * @param dataId the dataId of the dataset
     * @param dataType the expected data type of the dataset
     * @param dataMetaInformationEntity the dataMetaInformationEntity of the dataset
     * @param correlationId the correlationId of the corresponding process
     */
    fun assertActualAndExpectedDataTypeForIdMatch(
        dataId: String,
        dataType: DataType,
        dataMetaInformationEntity: DataMetaInformationEntity,
        correlationId: String,
    ) {
        if (DataType.valueOf(dataMetaInformationEntity.dataType) != dataType) {
            throw InvalidInputApiException(
                "Requested data $dataId not of type $dataType",
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaInformationEntity.dataType} by Dataland instead of your requested" +
                    " type $dataType.",
            )
        }
        logger.info(
            "Requesting Data with ID $dataId and expected type $dataType from framework data storage. " +
                "Correlation ID: $correlationId",
        )
    }
}
