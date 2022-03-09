package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.*
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component("DefaultStore")
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<String, StoredDataSet>()
    private var dataCounter = 0
    var companyData = mutableMapOf<String, Company>()
    private var companyCounter = 0

    override fun addDataSet(companyId: String, dataType: String, data: String): String {
        if (companyData.containsKey(companyId)) {
            dataCounter++
            this.data["$dataCounter"] =
                StoredDataSet(dataId = "$dataCounter", companyId = companyId, dataType = dataType, data = data)
            this.companyData[companyId]?.dataSets?.add(DataIdentifier(dataId = "$dataCounter", dataType = dataType))
            return "$dataCounter"
        }
        throw IllegalArgumentException("No company with the companyId $companyId exists.")
    }

    override fun listDataSets(): List<DataSetMetaInformation> {
        return data.map {
            DataSetMetaInformation(
                dataId = it.key.toString(),
                companyId = it.value.companyId,
                dataType = it.value.dataType
            )
        }
    }

    override fun getDataSet(dataId: String, dataType: String): String {
        if (! data.containsKey(dataId)) {throw IllegalArgumentException("The id: $dataId does not exist.")}
        if (data[dataId]?.dataType != dataType) {throw IllegalArgumentException("The data with id: $dataId is of type ${data[dataId]?.dataType} instead of the expected $dataType.")}
        return data[dataId]?.data ?: ""
    }

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData["$companyCounter"] = Company(companyName = companyName, dataSets = mutableListOf())
        return CompanyMetaInformation(companyName = companyName, companyId = "$companyCounter")
    }

    override fun listAllCompanies(): List<CompanyMetaInformation> {
        return companyData.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key)}
    }

    override fun listCompaniesByName(name: String): List<CompanyMetaInformation> {
        val matches = companyData.filter { it.value.companyName.contains(name, true) }
        if (matches.isEmpty()) {throw IllegalArgumentException("No matches for company with name '$name'.")}
        return matches.map { CompanyMetaInformation(companyName = it.value.companyName, companyId = it.key)}
    }

    override fun listDataSetsByCompany(companyId: String): List<DataIdentifier> {
        if (! data.containsKey(companyId)) {throw IllegalArgumentException("The id: $companyId does not exist.")}
        return companyData[companyId]?.dataSets ?: emptyList()
    }
}
