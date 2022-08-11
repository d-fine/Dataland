package org.dataland.csvconverter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.utils.CompanyInformationWithData
import java.io.File

class TestDataProvider(private val objectMapper: ObjectMapper) {
    fun getAllCompanyInformationWithEuTaxonomyDataForNonFinancials():
            List<CompanyInformationWithData<EuTaxonomyDataForNonFinancials>> {
        val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyDataForNonFinancials.json")
        val testCompanyInformationWithEuTaxonomyDataForNonFinancials:
                List<CompanyInformationWithData<EuTaxonomyDataForNonFinancials>> =
            objectMapper.readValue(
                jsonFile,
            )
        return testCompanyInformationWithEuTaxonomyDataForNonFinancials
    }

    fun getAllCompanyInformationWithEuTaxonomyDataForFinancials():
            List<CompanyInformationWithData<EuTaxonomyDataForFinancials>> {
        val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyDataForFinancials.json")
        val testCompanyInformationWithEuTaxonomyDataForFinancials:
                List<CompanyInformationWithData<EuTaxonomyDataForFinancials>> =
            objectMapper.readValue(
                jsonFile,
            )
        return testCompanyInformationWithEuTaxonomyDataForFinancials
    }
}
