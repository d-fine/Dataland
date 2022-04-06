package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StoredCompany
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DefaultManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper
) : DataManagerInterface {
    var dataMetaInformationPerDataId = mutableMapOf<String, DataMetaInformation>()
    var companyDataPerCompanyId = mutableMapOf<String, StoredCompany>()
    val allDataTypes = DataTypesExtractor().getAllDataTypes()
    private var companyCounter = 0

    private fun verifyCompanyIdExists(companyId: String) {
        if (!companyDataPerCompanyId.containsKey(companyId)) {
            throw IllegalArgumentException("Dataland does not know the company ID $companyId.")
        }
    }

    private fun verifyDataIdExists(dataId: String) {
        if (!dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId.")
        }
    }

    private fun verifyDataTypeExists(dataType: String) {
        if (!allDataTypes.contains(dataType)) {
            throw IllegalArgumentException("Dataland does not know the data type: $dataType")
        }
    }

    private fun verifyDataIdExistsAndIsOfType(dataId: String, dataType: String) {
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
        verifyCompanyIdExists(storableDataSet.companyId)

        val dataId = edcClient.insertData(objectMapper.writeValueAsString(storableDataSet))

        if (dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("The data ID $dataId already exists in Dataland.")
        }

        val dataMetaInformation =
            DataMetaInformation(dataId, storableDataSet.dataType, storableDataSet.companyId)
        dataMetaInformationPerDataId[dataId] = dataMetaInformation
        companyDataPerCompanyId[storableDataSet.companyId]!!.dataRegisteredByDataland.add(dataMetaInformation)
        return dataId
    }

    override fun getDataSet(dataId: String, dataType: String): StorableDataSet {
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

    override fun searchDataMetaInfo(companyId: String, dataType: String): List<DataMetaInformation> {
        var matches: Map<String, DataMetaInformation> = dataMetaInformationPerDataId

        if (companyId.isNotEmpty()) {
            verifyCompanyIdExists(companyId)
            matches = matches.filter { it.value.companyId == companyId }
        }
        if (dataType.isNotEmpty()) {
            verifyDataTypeExists(dataType)
            matches = matches.filter { it.value.dataType == dataType }
        }

        return matches.map {
            DataMetaInformation(dataId = it.key, it.value.dataType, it.value.companyId)
        }
    }

    override fun getDataMetaInfo(dataId: String): DataMetaInformation {
        verifyDataIdExists(dataId)
        return dataMetaInformationPerDataId[dataId]!!
    }

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyDataPerCompanyId["$companyCounter"] = CompanyMetaInformation(
            companyId = companyCounter.toString(),
            companyInformation,
            dataRegisteredByDataland = mutableListOf()
        )
        return companyDataPerCompanyId["$companyCounter"]!!
    }

    override fun listCompaniesByName(companyName: String): List<CompanyMetaInformation> {
        return companyDataPerCompanyId.filter { it.value.companyName.contains(companyName, true) }.map {
            CompanyMetaInformation(
                companyId = it.key,
                companyName = it.value.companyName,
                dataRegisteredByDataland = it.value.dataRegisteredByDataland
            )
        }
    }

    override fun getCompanyById(companyId: String): CompanyMetaInformation {
        verifyCompanyIdExists(companyId)

        return companyDataPerCompanyId[companyId]!!
    }
}
