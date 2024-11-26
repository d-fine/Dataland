package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * The class holds methods which are used in handling data requests
 *  @param objectMapper object mapper used for converting data classes to strings and vice versa
 *  @param companyQueryManager service for managing company data
 *  @param metaDataManager service for managing metadata
 */

@Service
class DataManagerUtils(
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles exceptions thrown by the data manager process while retrieving data by id
     * @param e the thrown client exception
     * @param dataId the dataId for which a exception was thrown
     * @param correlationId the correlationId of the request which caused the exception to be thrown
     */
    private fun handleStorageClientException(
        e: ClientException,
        dataId: String,
        correlationId: String,
    ) {
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
    private fun assertActualAndExpectedDataTypeForIdMatch(
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
     * @param datasetJsonStringGetter the function which specifies from which storage the dataset should be retrieved
     * @return the dataset as JSON string
     */
    fun getDatasetAsJsonStringFromStorageService(
        dataId: String,
        correlationId: String,
        datasetJsonStringGetter: (String, String) ->
        String,
    ): String {
        val dataAsString: String
        logger.info("Retrieve data from storage. Correlation ID: $correlationId")
        try {
            dataAsString = datasetJsonStringGetter(dataId, correlationId)
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
     * @param datasetJsonStringGetter the function to retrieve the dataset from the respective storage service
     * @return data set associated with the data ID provided in the input
     */
    fun getStorableDataset(
        dataId: String,
        dataType: DataType,
        correlationId: String,
        datasetJsonStringGetter: (String, String)
        -> String,
    ): StorableDataSet {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        assertActualAndExpectedDataTypeForIdMatch(dataId, dataType, dataMetaInformation, correlationId)
        lateinit var dataAsString: String
        try {
            dataAsString = datasetJsonStringGetter(dataId, correlationId)
        } catch (e: ClientException) {
            handleStorageClientException(e, dataId, correlationId)
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        dataAsStorableDataSet.requireConsistencyWith(dataMetaInformation)
        return dataAsStorableDataSet
    }

    /**
     * This method returns the company for a given companyId
     * @param companyId the companyId of the company for which information should be retrieved
     */
    fun getCompanyByCompanyId(companyId: String): StoredCompanyEntity = companyQueryManager.getCompanyById(companyId)
}
