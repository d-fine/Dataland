package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.RATIO_PRECISION
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.EuTaxonomyData
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.StockIndex
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DefaultManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper
) : DataManagerInterface {
    private val logger = LoggerFactory.getLogger(javaClass)
    var dataMetaInformationPerDataId = mutableMapOf<String, DataMetaInformation>()
    var companyDataPerCompanyId = mutableMapOf<String, StoredCompany>()
    val allDataTypes = DataTypesExtractor().getAllDataTypes()
    private val greenAssetRatios = mutableMapOf<StockIndex, BigDecimal>()

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
        logger.info("Add a dataset to a company " + Thread.currentThread().id + " Company Counter: " + storableDataSet.companyId)

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

    override fun addCompany(companyInformation: CompanyInformation): StoredCompany {
        val companyId = UUID.randomUUID().toString()
        logger.info("Add a company to store " + Thread.currentThread().id + " Company Counter: " + companyId + " Company Information: " + companyInformation)
        companyDataPerCompanyId[companyId] = StoredCompany(
            companyId = companyId,
            companyInformation,
            dataRegisteredByDataland = mutableListOf()
        )
        Thread.sleep(1000)
        return companyDataPerCompanyId[companyId]!!
    }

    override fun searchCompanies(searchString: String, onlyCompanyNames: Boolean): List<StoredCompany> {
        val allCompanies = companyDataPerCompanyId.values.toList()

        return if (searchString == "") {
            allCompanies
        } else if (onlyCompanyNames) {
            filterCompaniesByName(searchString, allCompanies)
        } else {
            (
                filterCompaniesByName(searchString, allCompanies) +
                    filterCompaniesByIdentifier(searchString, allCompanies)
                ).distinct()
        }
    }

    private fun filterCompaniesByName(searchString: String, companies: List<StoredCompany>): List<StoredCompany> {
        return (
            companies.filter { it.companyInformation.companyName.startsWith(searchString, true) } +
                companies.filter { it.companyInformation.companyName.contains(searchString, true) }
            ).distinct()
    }

    private fun filterCompaniesByIdentifier(searchString: String, companies: List<StoredCompany>): List<StoredCompany> {
        return companies.filter {
            it.companyInformation.identifiers.any { identifier ->
                identifier.identifierValue.contains(searchString, true)
            }
        }
    }

    override fun searchCompaniesByIndex(selectedIndex: StockIndex): List<StoredCompany> {
        return companyDataPerCompanyId.values.filter {
            it.companyInformation.indices.any { index -> index == selectedIndex }
        }
    }

    override fun getCompanyById(companyId: String): StoredCompany {
        verifyCompanyIdExists(companyId)

        return companyDataPerCompanyId[companyId]!!
    }

    override fun getGreenAssetRatio(selectedIndex: StockIndex?): Map<StockIndex, BigDecimal> {

        val indices = if (selectedIndex == null) {
            StockIndex.values().toList()
        } else {
            listOf(selectedIndex)
        }

        for (index in indices) {
            val filteredCompanies = searchCompaniesByIndex(index).filter {
                it.dataRegisteredByDataland.any { data -> data.dataType == "EuTaxonomyData" }
            }

            if (filteredCompanies.isEmpty()) {
                continue
            }
            updateGreenAssetRatioOnIndexLevel(index, filteredCompanies)
        }
        return greenAssetRatios
    }

    private fun updateGreenAssetRatioOnIndexLevel(index: StockIndex, companies: List<StoredCompany>) {
        var eligibleSum = BigDecimal(0.0)
        var totalSum = BigDecimal(0.0)
        for (company in companies) {
            val dataId = company.dataRegisteredByDataland.last { it.dataType == "EuTaxonomyData" }.dataId
            val data = objectMapper.readValue(getDataSet(dataId, "EuTaxonomyData").data, EuTaxonomyData::class.java)
            eligibleSum += data.capex?.eligiblePercentage ?: BigDecimal(0.0)
            eligibleSum += data.opex?.eligiblePercentage ?: BigDecimal(0.0)
            eligibleSum += data.revenue?.eligiblePercentage ?: BigDecimal(0.0)
            totalSum += data.capex?.totalAmount ?: BigDecimal(0.0)
            totalSum += data.opex?.totalAmount ?: BigDecimal(0.0)
            totalSum += data.revenue?.totalAmount ?: BigDecimal(0.0)
        }
        greenAssetRatios[index] = eligibleSum.divide(totalSum, RATIO_PRECISION, RoundingMode.HALF_UP)
    }
}
