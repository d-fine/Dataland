package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DefaultManager")
class DataManager(
    @Autowired var edcClient: DefaultApi
) : DataManagerInterface {
    var dataMetaInformationPerDataId = mutableMapOf<String, DataMetaInformation>()
    var companyDataPerCompanyId = mutableMapOf<String, CompanyMetaInformation>()
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
        val matchesForDataType = dataMetaInformationPerDataId.any { it.value.dataType == dataType }
        if (!matchesForDataType) {
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

        val dataId = edcClient.insertData(storableDataSet.data)

        if (dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("The data ID $dataId already exists in Dataland.")
        }

        val dataMetaInformation =
            DataMetaInformation(dataId, dataType = storableDataSet.dataType, companyId = storableDataSet.companyId)
        dataMetaInformationPerDataId[dataId] = dataMetaInformation
        companyDataPerCompanyId[storableDataSet.companyId]!!.dataRegisteredByDataland.add(dataMetaInformation)
        return dataId
    }

    override fun getData(dataManagerInputToGetData: DataManagerInputToGetData): String {
        verifyDataIdExistsAndIsOfType(dataManagerInputToGetData.dataId, dataManagerInputToGetData.dataType)

        val data = edcClient.selectDataById(dataManagerInputToGetData.dataId)

        if (data == "") {
            throw IllegalArgumentException(
                "No data set with the id: ${dataManagerInputToGetData.dataId} " +
                    "could be found in the data store."
            )
        }
        return data
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
            DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId)
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
            companyName = companyName,
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
