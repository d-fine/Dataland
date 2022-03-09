package org.dataland.datalandbackend.service

import org.dataland.datalandbackend.interfaces.DataStoreInterface
import org.dataland.datalandbackend.model.Company
import org.dataland.datalandbackend.model.CompanyMetaInformation
import org.dataland.datalandbackend.model.StoredDataSet
import org.dataland.datalandbackend.model.DataSetMetaInformation
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using in memory storage
 */
@Component("DefaultStore")
class InMemoryDataStore : DataStoreInterface {
    var data = mutableMapOf<Int, StoredDataSet>()
    private var dataCounter = 0
    var companyData = mutableMapOf<String, Company>()
    private var companyCounter = 0

    override fun addDataSet(companyId: String, dataType: String, data: String): String {
        dataCounter++
        if (companyData.containsKey(companyId)) {
            this.data[dataCounter] =
                StoredDataSet(dataId = dataCounter.toString(), companyId = companyId, dataType = dataType, data = data)
            this.companyData[companyId]?.eutaxonomy?.add("dataId")
            return dataCounter.toString()
        }
        dataCounter--
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
        if (! data.containsKey(dataId.toInt())) {throw IllegalArgumentException("The id: $dataId does not exist.")}
        if (data[dataId.toInt()]?.dataType != dataType) {throw IllegalArgumentException("The data with id: $dataId is of type ${data[dataId.toInt()]?.dataType} instead of the expected $dataType.")}
        return data[dataId.toInt()]?.data ?: ""
    }

    override fun addCompany(companyName: String): CompanyMetaInformation {
        companyCounter++
        companyData[companyCounter.toString()] = Company(companyName, mutableListOf())
        return CompanyMetaInformation(companyName = companyName, companyId = companyCounter.toString())
    }

    override fun listAllCompanies(): Map<String, Company> {
        return companyData
    }

    override fun listCompaniesByName(name: String): Map<String, Company> {

        val resultList = companyData.filter { it.value.companyName.contains(name, true) }

        /*val resultList = mutableMapOf<String, Company>()

        for ((k, v) in companyData) {
            if (v.companyName.contains(name, true)) {
                resultList[k] = v
            }
        }*/

        if (resultList.isEmpty()) {throw IllegalArgumentException("No matches for company with name '$name'.")}
        return resultList
    }
}
