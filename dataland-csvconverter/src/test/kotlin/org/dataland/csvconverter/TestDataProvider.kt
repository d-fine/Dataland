package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.eutaxonomy.financials.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.model.eutaxonomy.nonfinancials.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.utils.CompanyInformationWithData
import java.io.File

class TestDataProvider(private val objectMapper: ObjectMapper) {
    fun getAllCompanyInformationWithEuTaxonomyDataForNonFinancials():
        List<CompanyInformationWithData<EuTaxonomyDataForNonFinancials>> {
        val jsonFile = File("./build/resources/test/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
        return objectMapper.readValue(jsonFile)
    }

    fun getAllCompanyInformationWithEuTaxonomyDataForFinancials():
        List<CompanyInformationWithData<EuTaxonomyDataForFinancials>> {
        val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyDataForFinancials.json")
        return objectMapper.readValue(jsonFile)
    }
}
