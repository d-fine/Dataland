package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DataManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManager
) : DataManagerInterface {
    var dataMetaInformationPerDataId = ConcurrentHashMap<String, DataMetaInformation>()

    private fun verifyDataIdExists(dataId: String) {
        if (!dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId")
        }
    }

    private fun verifyDataIdExistsAndIsOfType(dataId: String, dataType: DataType) {
        verifyDataIdExists(dataId)
        if (dataMetaInformationPerDataId[dataId]!!.dataType != dataType) {
            throw IllegalArgumentException(
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaInformationPerDataId[dataId]!!.dataType} by Dataland instead of your requested" +
                    " type $dataType."
            )
        }
    }

    override fun addDataSet(storableDataSet: StorableDataSet): String {
        companyManager.verifyCompanyIdExists(storableDataSet.companyId)
        val dataId = edcClient.insertData(objectMapper.writeValueAsString(storableDataSet)).dataId

        if (dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("The data ID $dataId already exists in Dataland.")
        }

        val dataMetaInformation =
            DataMetaInformation(dataId, storableDataSet.dataType, storableDataSet.companyId)
        dataMetaInformationPerDataId[dataId] = dataMetaInformation
        companyManager.addMetaDataInformationToCompanyStore(storableDataSet.companyId, dataMetaInformation)

        return dataId
    }

    override fun getDataSet(dataId: String, dataType: DataType): StorableDataSet {
        verifyDataIdExistsAndIsOfType(dataId, dataType)
        val dataAsString = edcClient.selectDataById(dataId)
        if (dataAsString == "") {
            throw IllegalArgumentException(
                "No data set with the id: $dataId could be found in the data store."
            )
        }
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        if (dataAsStorableDataSet.dataType != dataType) {
            throw IllegalArgumentException(
                "The data set with the id: $dataId " +
                    "came back as type ${dataAsStorableDataSet.dataType} from the data store instead of type " +
                    "${dataMetaInformationPerDataId[dataId]} as registered by Dataland."
            )
        }
        return dataAsStorableDataSet
    }

    override fun searchDataMetaInfo(companyId: String, dataType: DataType?): List<DataMetaInformation> {
        var matches: Map<String, DataMetaInformation> = dataMetaInformationPerDataId

        if (companyId.isNotEmpty()) {
            companyManager.verifyCompanyIdExists(companyId)
            matches = matches.filter { it.value.companyId == companyId }
        }
        if (dataType != null) {
            matches = matches.filter { it.value.dataType == dataType }
        }

        return matches.map { it.value }
    }

    override fun getDataMetaInfo(dataId: String): DataMetaInformation {
        verifyDataIdExists(dataId)
        return dataMetaInformationPerDataId[dataId]!!
    }

    override fun isDataSetPublic(dataId: String): Boolean {
        val associatedCompanyId = getDataMetaInfo(dataId).companyId
        return companyManager.isCompanyPublic(associatedCompanyId)
    }
}
