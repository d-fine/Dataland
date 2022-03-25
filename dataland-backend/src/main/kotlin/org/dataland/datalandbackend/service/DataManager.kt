package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataManagerInputToGetData
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.xml.crypto.Data

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DefaultManager")
class DataManager(
    @Autowired var dataStore: DataStoreInterface
) : DataManagerInterface {
    var dataMetaData = mutableMapOf<String, DataMetaInformation>()
    var companyData = mutableMapOf<String, CompanyMetaInformation>()
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

    private fun verifyDataIdIsRegistered(dataId: String) {
        if (!dataMetaData.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId.")
        }
    }

    private fun verifyDataTypeIsRegistered(dataType: String) {
        // TODO verify that data type is valid
        }

    /*
    ________________________________
    Methods to route data inserts and queries to the data store and save meta data in the Dataland-Meta-Data-Storage:
    ________________________________
     */

    override fun addDataSet(storableDataSet: StorableDataSet): String {
        verifyCompanyIdExists(storableDataSet.companyId)

        val dataId = dataStore.insertDataSet(storableDataSet.data)

        dataMetaData[dataId] =
            DataMetaInformation(dataId, dataType = storableDataSet.dataType, companyId = storableDataSet.companyId)
        companyData[storableDataSet.companyId]!!.dataRegisteredByDataland.add(
            DataMetaInformation(dataId = dataId, dataType = storableDataSet.dataType, companyId = storableDataSet.companyId)
        )
        return dataId
    }

    override fun getData(dataManagerInputToGetData: DataManagerInputToGetData): String {
        verifyDataIdIsRegistered(dataManagerInputToGetData.dataId)

        val data = dataStore.selectDataSet(dataManagerInputToGetData.dataId)

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
            verifyDataIdIsRegistered(dataId)
            return listOf(dataMetaData[dataId]!!)
        }

        var matches = mapOf<String, DataMetaInformation>()

        if (companyId.isNotEmpty()) {
            verifyCompanyIdExists(companyId)
            matches = dataMetaData.filter { it.value.companyId == companyId }

            if (dataType.isEmpty()) {
                return matches.map { DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId) }
            }

            matches = matches.filter { it.value.dataType == dataType}
            return matches.map { DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId) }
        }

        if (dataType.isNotEmpty()) {
            verifyDataTypeIsRegistered(dataType)
            matches = dataMetaData.filter { it.value.dataType == dataType }

            if (companyId.isEmpty()) {
                return matches.map { DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId) }
            }

            matches = matches.filter { it.value.companyId == companyId}
            return matches.map { DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId) }
        }

        return matches.map { DataMetaInformation(dataId = it.key, dataType = it.value.dataType, companyId = it.value.companyId) }
    }

    /*
    ________________________________
    Methods to add and retrieve company info and save associated company meta data in the
    Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = CompanyMetaInformation(companyId = companyCounter.toString(), companyName = companyName, dataRegisteredByDataland = mutableListOf())
        return companyData["$companyCounter"]!!
    }

    override fun listCompaniesByName(companyName: String): List<CompanyMetaInformation> {
        val matches = companyData.filter { it.value.companyName.contains(companyName, true) }
        if (matches.isEmpty()) {
            throw IllegalArgumentException("No matches for company with name '$companyName'.")
        }
        return matches.map { CompanyMetaInformation(companyId = it.key, companyName = it.value.companyName, dataRegisteredByDataland = it.value.dataRegisteredByDataland) }
    }
/*

This method will be obsolete as soon as AllDataAPI is implemented.

    override fun listDataSetsByCompanyId(companyId: String): List<DataManagerInputToGetData> {
        verifyCompanyIdExists(companyId)

        return companyData[companyId]!!.dataMetaInformation
    }
*/
    override fun getCompanyById(companyId: String): CompanyMetaInformation {
        verifyCompanyIdExists(companyId)

        return companyData[companyId]!!
    }
}
