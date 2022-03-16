package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.StoredDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component("DefaultManager")
class DataManager (
    @Autowired @Qualifier("DefaultStore") var dataStore: DataStoreInterface
        ) : DataManagerInterface  {
    var dataMetaData = mutableMapOf<String, String>()
    var companyData = mutableMapOf<String, StoredCompany>()
    private var companyCounter = 0

    /*
    ________________________________
    Methods to route data inserts and queries to the data store and save meta data in the Dataland-Meta-Data-Storage:
    ________________________________
     */

    override fun addDataSet(storedDataSet: StoredDataSet): String {
        if (companyData.containsKey(storedDataSet.companyId)) {
            val dataId = dataStore.insertDataSet(storedDataSet.data)
            this.dataMetaData[dataId] = storedDataSet.dataType
            this.companyData[storedDataSet.companyId]?.dataSets?.add(
                DataIdentifier(dataId = dataId, dataType = storedDataSet.dataType)
            )
            return dataId
        }
        throw IllegalArgumentException("No company with the companyId $storedDataSet.companyId exists.")
    }
/*
    override fun listDataSets(): List<DataSetMetaInformation> {
        return data.map {
            DataSetMetaInformation(
                DataIdentifier(dataId = it.key, dataType = it.value.dataType),
                companyId = it.value.companyId
            )
        }
    }
*/
    override fun getDataSet(dataIdentifier: DataIdentifier): String {
        if(!dataMetaData.containsKey(dataIdentifier.dataId)) {
            throw IllegalArgumentException("Dataland does not know a data set with the id: ${dataIdentifier.dataId} ")
        }

        if (dataStore.selectDataSet(dataIdentifier.dataId)=="") {
            throw IllegalArgumentException("No data set with the id: ${dataIdentifier.dataId} " +
                    "could be found in the data store.")
        }
        if (dataMetaData[dataIdentifier.dataId] != dataIdentifier.dataType) {
            throw IllegalArgumentException(
                "The data with id: ${dataIdentifier.dataId} is of type" +
                        " ${dataMetaData[dataIdentifier.dataId]} instead of the expected ${dataIdentifier.dataType}."
            )
        }
    return dataStore.selectDataSet(dataIdentifier.dataId)
    }

    /*
    ________________________________
    Methods to add and retrieve company info and save meta data in the Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = StoredCompany(companyName = companyName, dataSets = mutableListOf())
        return CompanyMetaInformation(companyName = companyName, companyId = "$companyCounter")
    }

    override fun listAllCompanies(): List<CompanyMetaInformation> {
        return companyData.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key) }
    }

    override fun listCompaniesByName(companyName: String): List<CompanyMetaInformation> {
        val matches = companyData.filter { it.value.companyName.contains(companyName, true) }
        if (matches.isEmpty()) {
            throw IllegalArgumentException("No matches for company with name '$companyName'.")
        }
        return matches.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key) }
    }

    override fun listDataSetsByCompanyId(companyId: String): List<DataIdentifier> {
        if (!companyData.containsKey(companyId)) {
            throw IllegalArgumentException("The companyId: $companyId does not exist.")
        }
        return companyData[companyId]?.dataSets ?: emptyList()
    }

}
