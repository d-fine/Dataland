package org.dataland.datalandbackend

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
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
    fun assertActualAndExpectedDataTypeForIdMatch(
        dataId: String,
        dataType: DataType,
        dataMetaInformation: DataMetaInformationEntity,
        correlationId: String,
    ) {
        if (DataType.valueOf(dataMetaInformation.dataType) != dataType) {
            throw InvalidInputApiException(
                "Requested data $dataId not of type $dataType",
                "The data with the id: $dataId is registered as type" +
                        " ${dataMetaInformation.dataType} by Dataland instead of your requested" +
                        " type $dataType.",
            )
        }
        logger.info(
            "Requesting Data with ID $dataId and expected type $dataType from framework data storage. " +
                    "Correlation ID: $correlationId",
        )
    }
}
