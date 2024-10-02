package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.e2etests.auth.TechnicalUser
import java.util.UUID

class CompanyDataControllerTestUtils {
    val apiAccessor = ApiAccessor()

    private val baseCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithRandomIdentifiers(1)
            .first()
    val checkOtherCompanyTrue = "Other Company true"
    val checkOtherCompanyFalse = "Other Company false"
    val dataReaderUserId: UUID = UUID.fromString(TechnicalUser.Reader.technicalUserId)
    val fullPatchObject =
        CompanyInformationPatch(
            companyContactDetails = listOf("NewcompanyContactDetails@example.com"),
            companyName = "New-companyName",
            companyAlternativeNames = listOf("New-companyAlternativeNames"),
            companyLegalForm = "New-companyLegalForm",
            headquarters = "New-headquarters",
            headquartersPostalCode = "New-headquartersPostalCode",
            sector = "New-sector",
            countryCode = "New-countryCode",
            isTeaserCompany = false,
            website = "New-website",
            parentCompanyLei = "New-parentCompanyLei",
            identifiers =
                mapOf(
                    IdentifierType.Duns.value to listOf("Test-DUNS${UUID.randomUUID()}"),
                ),
        )

    private val dummyCompanyAssociatedDataWithoutCompanyId =
        CompanyAssociatedDataEutaxonomyNonFinancialsData(
            companyId = "placeholder",
            reportingPeriod = "placeholder",
            data = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first(),
        )

    fun uploadCompaniesInReverseToExpectedOrder(expectedSearchString: String) {
        uploadModifiedBaseCompany("$expectedSearchString none", null)
        var companyId = uploadModifiedBaseCompany("$expectedSearchString false", null)
        uploadDummyDataset(companyId = companyId, bypassQa = false)
        companyId = uploadModifiedBaseCompany("$expectedSearchString true", null)
        uploadDummyDataset(companyId = companyId, bypassQa = true)
        companyId = uploadModifiedBaseCompany(checkOtherCompanyFalse, listOf("1${expectedSearchString}2"))
        uploadDummyDataset(companyId = companyId, bypassQa = false)
        companyId = uploadModifiedBaseCompany(checkOtherCompanyTrue, listOf("1${expectedSearchString}2"))
        uploadDummyDataset(companyId = companyId, bypassQa = true)
    }

    fun uploadModifiedBaseCompany(
        name: String,
        alternativeNames: List<String>?,
    ): String {
        val companyInformation =
            baseCompanyInformation.copy(
                companyName = name,
                companyAlternativeNames = alternativeNames,
                identifiers =
                    mapOf(
                        IdentifierType.Isin.value to listOf(UUID.randomUUID().toString()),
                    ),
            )
        return apiAccessor.companyDataControllerApi.postCompany(companyInformation).companyId
    }

    fun uploadDummyDataset(
        companyId: String,
        reportingPeriod: String = "default",
        bypassQa: Boolean = false,
    ) {
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEutaxonomyNonFinancialsData(
            dummyCompanyAssociatedDataWithoutCompanyId.copy(companyId = companyId, reportingPeriod = reportingPeriod),
            bypassQa,
        )
    }
}
