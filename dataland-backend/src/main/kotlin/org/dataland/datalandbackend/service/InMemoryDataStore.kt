package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StorableCompany
import org.dataland.datalandbackend.model.StorableDataSet
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<String, StorableDataSet>()
    private var dataCounter = 0
    var companyData = mutableMapOf<String, StorableCompany>()
    private var companyCounter = 0

    override fun addDataSet(storableDataSet: StorableDataSet): String {
        if (!companyData.containsKey(storableDataSet.companyId)) {
            throw IllegalArgumentException("No company with the companyId $storableDataSet.companyId exists.")
        }
        dataCounter++
        this.data["$dataCounter"] = storableDataSet
        this.companyData[storableDataSet.companyId]!!.dataSets.add(
            DataIdentifier(dataId = "$dataCounter", dataType = storableDataSet.dataType)
        )
        return "$dataCounter"
    }

    override fun listDataSets(): List<DataSetMetaInformation> {
        return data.map {
            DataSetMetaInformation(
                DataIdentifier(dataId = it.key, dataType = it.value.dataType),
                companyId = it.value.companyId
            )
        }
    }

    override fun getStorableDataSet(dataIdentifier: DataIdentifier): StorableDataSet {

        if (!data.containsKey(dataIdentifier.dataId)) {
            throw IllegalArgumentException("The id: ${dataIdentifier.dataId} does not exist.")
        }
        val storedDataset = data[dataIdentifier.dataId]!!
        if (storedDataset.dataType != dataIdentifier.dataType) {
            throw IllegalArgumentException(
                "The data with id: ${dataIdentifier.dataId} is of type" +
                    " ${storedDataset.dataType} instead of the expected ${dataIdentifier.dataType}."
            )
        }
        return storedDataset
    }

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = StorableCompany(companyName = companyName, dataSets = mutableListOf())
        return CompanyMetaInformation(companyName = companyName, companyId = "$companyCounter")
    }

    override fun listCompaniesByName(name: String): List<CompanyMetaInformation> {
        val matches = companyData.filter { it.value.companyName.contains(name, true) }
        if (matches.isEmpty()) {
            throw IllegalArgumentException("No matches for company with name '$name'.")
        }
        return matches.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key) }
    }

    override fun listDataSetsByCompanyId(companyId: String): List<DataIdentifier> {
        if (!companyData.containsKey(companyId)) {
            throw IllegalArgumentException("The companyId: $companyId does not exist.")
        }
        return companyData[companyId]?.dataSets ?: emptyList()
    }

    override fun getCompanyById(companyId: String): CompanyMetaInformation {
        if (!companyData.containsKey(companyId)) {
            throw IllegalArgumentException("The companyId: $companyId does not exist.")
        }
        return CompanyMetaInformation(companyData[companyId]!!.companyName, companyId)
    }
}
