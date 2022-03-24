package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StorableCompany
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DefaultManager")
class DataManager(
    @Autowired var dataStore: DataStoreInterface
) : DataManagerInterface {
    var dataMetaData = mutableMapOf<String, DataSetMetaInformation>()
    var companyData = mutableMapOf<String, StorableCompany>()
    private var companyCounter = 0

    /*
    ________________________________
    Helper-methods:
    ________________________________
     */

    private fun verifyCompanyIdExists(companyId: String) {
        if (!companyData.containsKey(companyId)) {
            throw IllegalArgumentException("The companyId: ${companyId} does not exist.")
        }
    }

    private fun verifyDataIdIsRegistered(dataId: String) {
        if (!dataMetaData.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know a data set with the id: ${dataId} ")
        }
    }

    /*
    ________________________________
    Methods to route data inserts and queries to the data store and save meta data in the Dataland-Meta-Data-Storage:
    ________________________________
     */

    override fun addDataSet(storedDataSet: StorableDataSet): String {
        verifyCompanyIdExists(storedDataSet.companyId)

        val dataId = dataStore.insertDataSet(storedDataSet.data)

        this.dataMetaData[dataId] =
            DataSetMetaInformation(dataType = storedDataSet.dataType, companyId = storedDataSet.companyId)
        this.companyData[storedDataSet.companyId]!!.dataSets.add(
            DataIdentifier(dataId = dataId, dataType = storedDataSet.dataType)
        )
        return dataId
    }

    override fun getDataSet(dataIdentifier: DataIdentifier): String {
        verifyDataIdIsRegistered(dataIdentifier.dataId)

        val dataSet = dataStore.selectDataSet(dataIdentifier.dataId)

        if (dataSet == "") {
            throw IllegalArgumentException(
                "No data set with the id: ${dataIdentifier.dataId} " +
                    "could be found in the data store."
            )
        }
        if (dataMetaData[dataIdentifier.dataId]!!.dataType != dataIdentifier.dataType) {
            throw IllegalArgumentException(
                "The data with the id: ${dataIdentifier.dataId} is registered as type" +
                    " ${dataMetaData[dataIdentifier.dataId]} by Dataland instead of your requested" +
                    " type ${dataIdentifier.dataType}."
            )
        }
        return dataSet
    }

    /*
    ________________________________
    Methods to process meta data:
    ________________________________
     */

    override fun getMetaData(dataId: String): DataSetMetaInformation {
        verifyDataIdIsRegistered(dataId)
        return dataMetaData[dataId]!!
    }

    /*
    ________________________________
    Methods to add and retrieve company info and save associated company meta data in the
    Dataland-Company-Meta-Data-Storage:
    ________________________________
     */

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = StorableCompany(companyName = companyName, dataSets = mutableListOf())
        return CompanyMetaInformation(companyName = companyName, companyId = "$companyCounter")
    }

    override fun listCompaniesByName(companyName: String): List<CompanyMetaInformation> {
        val matches = companyData.filter { it.value.companyName.contains(companyName, true) }
        if (matches.isEmpty()) {
            throw IllegalArgumentException("No matches for company with name '$companyName'.")
        }
        return matches.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key) }
    }

    override fun listDataSetsByCompanyId(companyId: String): List<DataIdentifier> {
        verifyCompanyIdExists(companyId)

        return companyData[companyId]!!.dataSets
    }

    override fun getCompanyById(companyId: String): CompanyMetaInformation {
        verifyCompanyIdExists(companyId)

        return CompanyMetaInformation(companyName = companyData[companyId]!!.companyName, companyId = companyId)
    }
}
