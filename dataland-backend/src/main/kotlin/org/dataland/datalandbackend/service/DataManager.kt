package org.dataland.datalandbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.RATIO_PRECISION
import org.dataland.datalandbackend.annotations.DataTypesExtractor
import org.dataland.datalandbackend.edcClient.api.DefaultApi
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.enums.StockIndex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of a data manager for Dataland including meta data storages
 */
@Component("DataManager")
class DataManager(
    @Autowired var edcClient: DefaultApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManager
) : DataManagerInterface {
    var dataMetaInformationPerDataId = ConcurrentHashMap<String, DataMetaInformation>()
    val allDataTypes = DataTypesExtractor().getAllDataTypes()
    private val greenAssetRatiosForNonFinancials = ConcurrentHashMap<StockIndex, BigDecimal>()

    private fun verifyDataIdExists(dataId: String) {
        if (!dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("Dataland does not know the data ID: $dataId")
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
        companyManager.verifyCompanyIdExists(storableDataSet.companyId)
        val dataId = edcClient.insertData(objectMapper.writeValueAsString(storableDataSet)).dataId

        if (dataMetaInformationPerDataId.containsKey(dataId)) {
            throw IllegalArgumentException("The data ID $dataId already exists in Dataland.")
        }

        val dataMetaInformation =
            DataMetaInformation(dataId, storableDataSet.dataType, storableDataSet.companyId)
        dataMetaInformationPerDataId[dataId] = dataMetaInformation
        companyManager.addMetaDataInformationToCompanyStore(storableDataSet.companyId, dataMetaInformation)

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
            companyManager.verifyCompanyIdExists(companyId)
            matches = matches.filter { it.value.companyId == companyId }
        }
        if (dataType.isNotEmpty()) {
            verifyDataTypeExists(dataType)
            matches = matches.filter { it.value.dataType == dataType }
        }

        return matches.map { it.value }
    }

    override fun getDataMetaInfo(dataId: String): DataMetaInformation {
        verifyDataIdExists(dataId)
        return dataMetaInformationPerDataId[dataId]!!
    }

    override fun getGreenAssetRatioForNonFinancials(selectedIndex: StockIndex?): Map<StockIndex, BigDecimal> {

        val indices = if (selectedIndex == null) {
            StockIndex.values().toList()
        } else {
            listOf(selectedIndex)
        }

        for (index in indices) {
            val filteredCompanies = companyManager.searchCompaniesByIndex(index).filter {
                it.dataRegisteredByDataland.any { data -> data.dataType == "EuTaxonomyDataForNonFinancials" }
            }

            if (filteredCompanies.isEmpty()) {
                continue
            }
            updateGreenAssetRatioForNonFinancialsOnIndexLevel(index, filteredCompanies)
        }
        return greenAssetRatiosForNonFinancials
    }

    private fun updateGreenAssetRatioForNonFinancialsOnIndexLevel(index: StockIndex, companies: List<StoredCompany>) {
        var eligibleSum = BigDecimal(0.0)
        var totalSum = BigDecimal(0.0)
        for (company in companies) {
            val dataId = company.dataRegisteredByDataland.last { it.dataType == "EuTaxonomyDataForNonFinancials" }
                .dataId
            val data = objectMapper.readValue(
                getDataSet(dataId, "EuTaxonomyDataForNonFinancials").data,
                EuTaxonomyDataForNonFinancials::class.java
            )
            eligibleSum += data.capex?.eligiblePercentage ?: BigDecimal(0.0)
            eligibleSum += data.opex?.eligiblePercentage ?: BigDecimal(0.0)
            eligibleSum += data.revenue?.eligiblePercentage ?: BigDecimal(0.0)
            totalSum += data.capex?.totalAmount ?: BigDecimal(0.0)
            totalSum += data.opex?.totalAmount ?: BigDecimal(0.0)
            totalSum += data.revenue?.totalAmount ?: BigDecimal(0.0)
        }
        greenAssetRatiosForNonFinancials[index] = eligibleSum.divide(totalSum, RATIO_PRECISION, RoundingMode.HALF_UP)
    }

    override fun isDataSetPublic(dataId: String): Boolean {
        val associatedCompanyId = getDataMetaInfo(dataId).companyId
        return companyManager.isCompanyPublic(associatedCompanyId)
    }
}
