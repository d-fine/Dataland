package org.dataland.datalandbackend

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * Enables a centralized generation of log messages for all Dataland backend operations.
 */

@Component("ExceptionHandlingUtils")
class DatamanagerUtils(
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val objectMapper: ObjectMapper,
) {
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

    /**
     * This function retrieves a dataset from one of the connected services
     * @param dataId the dataId of the requested dataset
     * @param correlationId the correlationId of the request
     * @param storageClientFunction the function which specifies from which storage the dataset should be retrieved
     */
    fun getDataFromStorageService(dataId: String, correlationId: String, storageClientFunction: (String, String) -> String): String {
        val dataAsString: String
        logger.info("Retrieve data from internal storage. Correlation ID: $correlationId")
        try {
            dataAsString = storageClientFunction(dataId, correlationId)
        } catch (e: ServerException) {
            logger.error(
                "Error requesting data. Received ServerException with Message:" +
                    " ${e.message}. Correlation ID: $correlationId",
            )
            throw e
        }
        return dataAsString
    }

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataId to identify the stored data
     * @param dataType to check the correctness of the type of the retrieved data
     * @param correlationId to use in combination with dataId to retrieve data and assert type
     * @param storageFunction the function to retrieve the dataset from the respective storage service
     * @return data set associated with the data ID provided in the input
     */
    fun getDataSet(dataId: String, dataType: DataType, correlationId: String, storageFunction: (String, String) -> String):
        StorableDataSet {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        assertActualAndExpectedDataTypeForIdMatch(dataId, dataType, dataMetaInformation, correlationId)
        lateinit var dataAsString: String
        try {
            dataAsString = storageFunction(dataId, correlationId)
        } catch (e: ClientException) {
            handleStorageClientException(e, dataId, correlationId)
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        dataAsStorableDataSet.requireConsistencyWith(dataMetaInformation)
        return dataAsStorableDataSet
    }
}
