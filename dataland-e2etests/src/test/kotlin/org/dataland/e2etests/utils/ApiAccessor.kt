package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSmeData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedCompanyDataControllerApi

class ApiAccessor {

    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val tokenHandler = TokenHandler()
    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    private val dataControllerApiForEuTaxonomyNonFinancials =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForEuTaxonomyDataForNonFinancials =
        TestDataProvider(EuTaxonomyDataForNonFinancials::class.java)
    val euTaxonomyNonFinancialsUploaderFunction =
        { companyId: String, euTaxonomyNonFinancialsData: EuTaxonomyDataForNonFinancials ->
            val companyAssociatedEuTaxonomyNonFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, euTaxonomyNonFinancialsData)
            dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                companyAssociatedEuTaxonomyNonFinancialsData
            )
        }

    private val testDataProviderForEuTaxonomyDataForFinancials =
        TestDataProvider(EuTaxonomyDataForFinancials::class.java)

    private val dataControllerApiForLksgData =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForLksgData =
        TestDataProvider(LksgData::class.java)
    val lksgUploaderFunction = { companyId: String, lksgData: LksgData ->
        val companyAssociatedLksgData = CompanyAssociatedDataLksgData(companyId, lksgData)
        dataControllerApiForLksgData.postCompanyAssociatedLksgData(
            companyAssociatedLksgData
        )
    }

    private val dataControllerApiForSfdrData =
        SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSfdrData =
        TestDataProvider(SfdrData::class.java)
    val sfdrUploaderFunction = { companyId: String, sfdrData: SfdrData ->
        val companyAssociatedSfdrData = CompanyAssociatedDataSfdrData(companyId, sfdrData)
        dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            companyAssociatedSfdrData
        )
    }

    private val dataControllerApiForSmeData =
        SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSmeData =
        TestDataProvider(SmeData::class.java)
    val smeUploaderFunction = { companyId: String, smeData: SmeData ->
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, smeData)
        dataControllerApiForSmeData.postCompanyAssociatedSmeData(
            companyAssociatedSmeData
        )
    }

    fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        listOfCompanyInformation: List<CompanyInformation>,
        listOfFrameworkData: List<T>,
        frameworkDataUploadFunction: (companyId: String, frameworkData: T) -> DataMetaInformation
    ) {
        if (listOfCompanyInformation.size == listOfFrameworkData.size) {
            tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
            listOfCompanyInformation.forEachIndexed { index, companyInformation ->
                val receivedCompanyId = companyDataControllerApi.postCompany(companyInformation).companyId
                frameworkDataUploadFunction(receivedCompanyId, listOfFrameworkData[index])
            }
        } else {
            throw throw IllegalArgumentException(
                "The list of companyInformation has ${listOfCompanyInformation.size} elements, " +
                    "and the list of frameworkData has ${listOfFrameworkData.size} elements. " +
                    "The numbers of elements of both lists need to match."
            )
        }
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        frameworksToConsider: List<DataTypeEnum>,
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDataSetsPerFramework: Int
        ) {
        frameworksToConsider.forEach {
            when (it) {
                DataTypeEnum.lksg -> uploadCompanyAndFrameworkDataForOneFramework(companyInformationPerFramework[it]!!,
                    testDataProviderForLksgData.getTData(numberOfDataSetsPerFramework), lksgUploaderFunction)
                DataTypeEnum.sfdr -> uploadCompanyAndFrameworkDataForOneFramework(companyInformationPerFramework[it]!!,
                    testDataProviderForSfdrData.getTData(numberOfDataSetsPerFramework), sfdrUploaderFunction)
                DataTypeEnum.sme -> uploadCompanyAndFrameworkDataForOneFramework(companyInformationPerFramework[it]!!,
                    testDataProviderForSmeData.getTData(numberOfDataSetsPerFramework), smeUploaderFunction)
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(companyInformationPerFramework[it]!!,
                    testDataProviderForEuTaxonomyDataForNonFinancials.getTData(numberOfDataSetsPerFramework), euTaxonomyNonFinancialsUploaderFunction)
                DataTypeEnum.eutaxonomyMinusFinancials -> println("e") //TODO
            }
        }
    }

    fun uploadNCompaniesWithoutIdentifiers(numCompanies: Int): List<CompanyUpload> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val listOfCompanyInformation = testDataProviderForEuTaxonomyDataForFinancials
            .getCompanyInformationWithoutIdentifiers(numCompanies)
        val listOfCompanyUploads = mutableListOf<CompanyUpload>()
        listOfCompanyInformation.forEach {
                companyInformation ->
            listOfCompanyUploads.add(
                CompanyUpload(
                    companyInformation,
                    companyDataControllerApi.postCompany(companyInformation)
                )
            )
        }
        return listOfCompanyUploads
    }

    fun uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(setAsTeaserCompany: Boolean): CompanyUpload {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = setAsTeaserCompany)
        return CompanyUpload(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithRandomIdentifier(): CompanyUpload {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForFinancials
            .generateCompanyInformation("NameDoesNotMatter", "SectorDoesNotMatter")
        return CompanyUpload(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun getCompaniesOnlyByName(searchString: String): List<StoredCompany> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        return companyDataControllerApi.getCompanies(
            searchString,
            onlyCompanyNames = true
        )
    }
}

data class CompanyUpload(

    val inputCompanyInformation: CompanyInformation,

    val actualStoredCompany: StoredCompany,

)
