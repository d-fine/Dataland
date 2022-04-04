package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataManagerInputToGetData
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
    @Autowired var edcClient: DefaultApi
) : DataManagerInterface {
    var dataMetaData = mutableMapOf<String, DataMetaInformation>()
    var companyData = mutableMapOf<String, StoredCompany>()
    private var companyCounter = 0

    /*
    ________________________________
    Helper-methods:
    ________________________________
     */

    private fun verifyCompanyIdExists(companyId: String) {
        if (!companyData.containsKey(companyId)) {
            throw IllegalArgumentException("Dataland does not know the company ID $companyId.")
        }
    }

    private fun verifyDataIdExists(dataId: String) {
        if (!dataMetaData.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId.")
        }
    }

    private fun verifyDataTypeExists(dataType: String) {
        val matchesForDataType = dataMetaData.any { it.value.dataType == dataType }
        if (!matchesForDataType) {
            throw IllegalArgumentException("Dataland does not know the data type: $dataType")
        }
    }

    private fun companyDataMapToListConversion(input: Map<String, StoredCompany>):
        List<StoredCompany> {
        return input.map {
            StoredCompany(
                companyId = it.key,
                companyInformation = it.value.companyInformation,
                dataRegisteredByDataland = it.value.dataRegisteredByDataland
            )
        }
    }

    /*
    ________________________________
    Methods to route data inserts and queries to the data store and save meta data in the Dataland-Meta-Data-Storage:
    ________________________________
     */

    override fun addDataSet(storableDataSet: StorableDataSet): String {
        verifyCompanyIdExists(storableDataSet.companyId)

        val dataId = edcClient.insertData(storableDataSet.data)

        if (dataMetaData.containsKey(dataId)) {
            throw IllegalArgumentException("The data ID $dataId already exists in Dataland.")
        }

        val dataMetaInformation =
            DataMetaInformation(dataId, dataType = storableDataSet.dataType, companyId = storableDataSet.companyId)
        dataMetaData[dataId] = dataMetaInformation
        companyData[storableDataSet.companyId]!!.dataRegisteredByDataland.add(dataMetaInformation)
        return dataId
    }

    override fun getData(dataManagerInputToGetData: DataManagerInputToGetData): String {
        verifyDataIdExists(dataManagerInputToGetData.dataId)

        val data = edcClient.selectDataById(dataManagerInputToGetData.dataId)

        if (data == "") {
            throw IllegalArgumentException(
                "No data set with the id: ${dataManagerInputToGetData.dataId} " +
                    "could be found in the data store."
            )
        }
        if (dataMetaData[dataManagerInputToGetData.dataId]!!.dataType != dataManagerInputToGetData.dataType) {
            throw IllegalArgumentException(
                "The data with the id: ${dataManagerInputToGetData.dataId} is registered as type" +
                    " ${dataMetaData[dataManagerInputToGetData.dataId]} by Dataland instead of your requested" +
                    " type ${dataManagerInputToGetData.dataType}."
            )
        }
        return data
    }

    /*
    ________________________________
    Methods to process meta data:
    ________________________________
     */

    override fun searchDataMetaInfo(dataId: String, companyId: String, dataType: String): List<DataMetaInformation> {
        if (dataId.isNotEmpty()) {
            verifyDataIdExists(dataId)
            return listOf(dataMetaData[dataId]!!)
        }

        var matches: Map<String, DataMetaInformation> = dataMetaData

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

    /*
    ________________________________
    Methods to add and retrieve company info and save associated company meta data in the
    Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    override fun addCompany(companyInformation: CompanyInformation): StoredCompany {
        companyCounter++
        companyData["$companyCounter"] = StoredCompany(
            companyId = companyCounter.toString(),
            companyInformation,
            dataRegisteredByDataland = mutableListOf()
        )
        return companyData["$companyCounter"]!!
    }

    override fun listCompaniesByName(companyName: String): List<StoredCompany> {
        val matches = companyData.filter { it.value.companyInformation.companyName.contains(companyName, true) }
        if (matches.isEmpty()) {
            throw IllegalArgumentException("No matches for company with name '$companyName'.")
        }
        return companyDataMapToListConversion(matches)
    }

    override fun getCompanyById(companyId: String): StoredCompany {
        verifyCompanyIdExists(companyId)

        return companyData[companyId]!!
    }
}
