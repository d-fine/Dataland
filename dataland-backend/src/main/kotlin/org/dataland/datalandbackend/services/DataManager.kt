package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.edcClient.infrastructure.ServerException
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.interfaces.CompanyManagerInterface
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataMetaInformationManagerInterface
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DataManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManagerInterface,
    @Autowired var metaDataManager: DataMetaInformationManagerInterface
) : DataManagerInterface {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun getDataMetaInformationByIdAndVerifyDataType(
        dataId: String,
        dataType: DataType
    ): DataMetaInformationEntity {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        if (DataType.valueOf(dataMetaInformation.dataType) != dataType) {
            throw IllegalArgumentException(
                "The data with the id: $dataId is registered as type ${dataMetaInformation.dataType} by Dataland " +
                    "instead of your requested type $dataType."
            )
        }
        return dataMetaInformation
    }

    override fun addDataSet(storableDataSet: StorableDataSet, correlationId: String): String {
        val company = companyManager.getCompanyById(storableDataSet.companyId)
        logger.info(
            "Sending StorableDataSet of type ${storableDataSet.dataType} for company ID " +
                "${storableDataSet.companyId}, Company Name ${company.companyName} to EuroDaT Interface. " +
                "Correlation ID: $correlationId"
        )
        val dataId: String = storeDataSet(storableDataSet, company, correlationId)
        metaDataManager.storeDataMetaInformation(company, dataId, storableDataSet.dataType)
        return dataId
    }

    private fun storeDataSet(
        storableDataSet: StorableDataSet,
        company: StoredCompanyEntity,
        correlationId: String
    ): String {
        val dataId: String
        try {
            dataId = edcClient.insertData(correlationId, objectMapper.writeValueAsString(storableDataSet)).dataId
        } catch (e: ServerException) {
            logger.error(
                "Error sending insertData Request to Eurodat. Received ServerException with Message: ${e.message}. " +
                    "Correlation ID: $correlationId"
            )
            throw e
        }
        logger.info(
            "Stored StorableDataSet of type ${storableDataSet.dataType} for company ID ${storableDataSet.companyId}," +
                " Company Name ${company.companyName} received ID $dataId from EuroDaT. Correlation ID: $correlationId"
        )
        return dataId
    }

    override fun getDataSet(dataId: String, dataType: DataType, correlationId: String): StorableDataSet {
        val dataTypeNameExpectedByDataland = getTypeNameExpectedByDataland(dataId, dataType, correlationId)
        val dataAsString: String = getDataFromEdcClient(dataId, correlationId)
        if (dataAsString == "") {
            throw IllegalArgumentException(
                "No data set with the id: $dataId could be found in the data store."
            )
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        if (dataAsStorableDataSet.dataType != DataType.valueOf(dataTypeNameExpectedByDataland)) {
            throw IllegalArgumentException(
                "The data set with the id: $dataId came back as type ${dataAsStorableDataSet.dataType} from the " +
                    "data store instead of type $dataTypeNameExpectedByDataland as registered by Dataland."
            )
        }
        return dataAsStorableDataSet
    }

    private fun getTypeNameExpectedByDataland(
        dataId: String,
        dataType: DataType,
        correlationId: String
    ): String {
        val dataTypeNameExpectedByDataland = getDataMetaInformationByIdAndVerifyDataType(dataId, dataType).dataType
        logger.info(
            "Requesting Data with ID $dataId and expected type $dataType from EuroDat. Correlation ID: $correlationId"
        )
        return dataTypeNameExpectedByDataland
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

    override fun isDataSetPublic(dataId: String): Boolean {
        val associatedCompanyId = metaDataManager.getDataMetaInformationByDataId(dataId).company.companyId
        return companyManager.isCompanyPublic(associatedCompanyId)
    }
}
