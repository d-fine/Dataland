package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.DataIdentifier
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.StoredDataSet
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component("DefaultStore")
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<String, StoredDataSet>()
    private var dataCounter = 0
    var companyData = mutableMapOf<String, StoredCompany>()
    private var companyCounter = 0

    override fun addDataSet(storedDataSet: StoredDataSet): String {
        if (companyData.containsKey(storedDataSet.companyId)) {
            dataCounter++
            this.data["$dataCounter"] =
                StoredDataSet(
                    companyId = storedDataSet.companyId,
                    dataType = storedDataSet.dataType,
                    data = storedDataSet.data
                )
            this.companyData[storedDataSet.companyId]?.dataSets?.add(
                DataIdentifier(dataId = "$dataCounter", dataType = storedDataSet.dataType)
            )
            return "$dataCounter"
        }
        throw IllegalArgumentException("No company with the companyId $storedDataSet.companyId exists.")
    }

    override fun listDataSets(): List<DataSetMetaInformation> {
        return data.map {
            DataSetMetaInformation(
                DataIdentifier(dataId = it.key, dataType = it.value.dataType),
                companyId = it.value.companyId
            )
        }
    }

    override fun getDataSet(dataIdentifier: DataIdentifier): String {
        if (!data.containsKey(dataIdentifier.dataId)) {
            throw IllegalArgumentException("The id: ${dataIdentifier.dataId} does not exist.")
        }
        if (data[dataIdentifier.dataId]?.dataType != dataIdentifier.dataType) {
            throw IllegalArgumentException(
                "The data with id: ${dataIdentifier.dataId} is of type" +
                    " ${data[dataIdentifier.dataId]?.dataType} instead of the expected ${dataIdentifier.dataType}."
            )
        }
        return data[dataIdentifier.dataId]?.data ?: ""
    }

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = StoredCompany(companyName = companyName, dataSets = mutableListOf())
        return CompanyMetaInformation(companyName = companyName, companyId = "$companyCounter")
    }

    override fun listAllCompanies(): List<CompanyMetaInformation> {
        return companyData.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key) }
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

    override fun getCompanyNameById(companyId: String): String {
        return companyData[companyId]?.companyName ?: ""
    }
}
