package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.edcClient.infrastructure.ServerException
import org.dataland.datalandbackend.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackend.exceptions.InvalidInputApiException
import org.dataland.datalandbackend.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param edcClient API client to communicate with the data storage service
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyManager service for managing company data
 * @param metaDataManager service for managing metadata
 */
@Component("DataManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManager,
    @Autowired var metaDataManager: DataMetaInformationManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun assertActualAndExpectedDataTypeForIdMatch(
        dataId: String,
        dataType: DataType,
        correlationId: String
    ) {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        if (DataType.valueOf(dataMetaInformation.dataType) != dataType) {
            throw InvalidInputApiException(
                "Requested data $dataId not of type $dataType",
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaInformation.dataType} by Dataland instead of your requested" +
                    " type $dataType."
            )
        }
        logger.info(
            "Requesting Data with ID $dataId and expected type $dataType from EuroDat. Correlation ID: $correlationId"
        )
    }

    /**
     * Method to make the data manager add data to a data store and store meta data in Dataland
     * @param storableDataSet contains all the inputs needed by Dataland
     * @return ID of the newly stored data in the data store
     */
    @Transactional
    fun addDataSet(storableDataSet: StorableDataSet, correlationId: String): String {
        val company = companyManager.getCompanyById(storableDataSet.companyId)
        logger.info(
            "Sending StorableDataSet of type ${storableDataSet.dataType} for company ID " +
                "${storableDataSet.companyId}, Company Name ${company.companyName} to storage Interface. " +
                "Correlation ID: $correlationId"
        )
        val dataId: String = storeDataSet(storableDataSet, company.companyName, correlationId)
        metaDataManager.storeDataMetaInformation(company, dataId, storableDataSet.dataType)
        return dataId
    }

    private fun storeDataSet(
        storableDataSet: StorableDataSet,
        companyName: String,
        correlationId: String
    ): String {
        val dataId: String
        try {
            dataId = edcClient.insertData(correlationId, objectMapper.writeValueAsString(storableDataSet)).dataId
        } catch (e: ServerException) {
            val message = "Error sending insertData Request to Eurodat." +
                " Received ServerException with Message: ${e.message}. Correlation ID: $correlationId"
            logger.error(message)
            throw InternalServerErrorApiException(
                "Upload to Storage failed", "The upload of the dataset to the Storage failed",
                message,
                e
            )
        }
        logger.info(
            "Stored StorableDataSet of type ${storableDataSet.dataType} for company ID ${storableDataSet.companyId}," +
                " Company Name $companyName received ID $dataId from EuroDaT. Correlation ID: $correlationId"
        )
        return dataId
    }

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataId to identify the stored data
     * @param dataType to check the correctness of the type of the retrieved data
     * @return data set associated with the data ID provided in the input
     */
    fun getDataSet(dataId: String, dataType: DataType, correlationId: String): StorableDataSet {
        assertActualAndExpectedDataTypeForIdMatch(dataId, dataType, correlationId)
        val dataAsString: String = getDataFromEdcClient(dataId, correlationId)
        if (dataAsString == "") {
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store."
            )
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        if (dataAsStorableDataSet.dataType != dataType) {
            throw InternalServerErrorApiException(
                "Dataland-Internal inconsistency regarding dataset $dataId",
                "We are having some internal issues with the dataset $dataId, please contact support.",
                "Dataset $dataId should be of type $dataType but is of type ${dataAsStorableDataSet.dataType}"
            )
        }
        return dataAsStorableDataSet
    }

    private fun getDataFromEdcClient(dataId: String, correlationId: String): String {
        val dataAsString: String
        logger.info("Retrieve data from edc client. Correlation ID: $correlationId")
        try {
            dataAsString = edcClient.selectDataById(dataId, correlationId)
        } catch (e: ServerException) {
            logger.error(
                "Error sending selectDataById request to Eurodat. Received ServerException with Message:" +
                    " ${e.message}. Correlation ID: $correlationId"
            )
            throw e
        }
        return dataAsString
    }

    /**
     * Method to check if a data set belongs to a teaser company and hence is publicly available
     * @param dataId the ID of the data set to be checked
     * @return a boolean signalling if the data is public or not
     */
    fun isDataSetPublic(dataId: String): Boolean {
        val associatedCompanyId = metaDataManager.getDataMetaInformationByDataId(dataId).company.companyId
        return companyManager.isCompanyPublic(associatedCompanyId)
    }
}
