package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SmeDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForFinancials
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
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedCompanyDataControllerApi
import org.dataland.e2etests.accessmanagement.UnauthorizedMetaDataControllerApi

class ApiAccessor {

    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    val tokenHandler = TokenHandler()

    val generalTestDataProvider = GeneralTestDataProvider()

    private val dataControllerApiForEuTaxonomyNonFinancials =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForEuTaxonomyDataForNonFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForNonFinancials::class.java)
    val euTaxonomyNonFinancialsUploaderFunction =
        { companyId: String, euTaxonomyNonFinancialsData: EuTaxonomyDataForNonFinancials ->
            val companyAssociatedEuTaxonomyNonFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, euTaxonomyNonFinancialsData)
            dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                companyAssociatedEuTaxonomyNonFinancialsData
            )
        }

    private val dataControllerApiForEuTaxonomyFinancials =
        EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderEuTaxonomyForFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForFinancials::class.java)
    private val euTaxonomyFinancialsUploaderFunction =
        { companyId: String, euTaxonomyFinancialsData: EuTaxonomyDataForFinancials ->
            val companyAssociatedEuTaxonomyFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForFinancials(companyId, euTaxonomyFinancialsData)
            dataControllerApiForEuTaxonomyFinancials.postCompanyAssociatedEuTaxonomyDataForFinancials(
                companyAssociatedEuTaxonomyFinancialsData
            )
        }

    private val dataControllerApiForLksgData =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForLksgData =
        FrameworkTestDataProvider(LksgData::class.java)
    private val lksgUploaderFunction = { companyId: String, lksgData: LksgData ->
        val companyAssociatedLksgData = CompanyAssociatedDataLksgData(companyId, lksgData)
        dataControllerApiForLksgData.postCompanyAssociatedLksgData(
            companyAssociatedLksgData
        )
    }

    private val dataControllerApiForSfdrData =
        SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForSfdrData =
        FrameworkTestDataProvider(SfdrData::class.java)
    private val sfdrUploaderFunction = { companyId: String, sfdrData: SfdrData ->
        val companyAssociatedSfdrData = CompanyAssociatedDataSfdrData(companyId, sfdrData)
        dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            companyAssociatedSfdrData
        )
    }

    private val dataControllerApiForSmeData =
        SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val testDataProviderForSmeData =
        FrameworkTestDataProvider(SmeData::class.java)
    private val smeUploaderFunction = { companyId: String, smeData: SmeData ->
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, smeData)
        dataControllerApiForSmeData.postCompanyAssociatedSmeData(
            companyAssociatedSmeData
        )
    }

    private fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        listOfCompanyInformation: List<CompanyInformation>,
        listOfFrameworkData: List<T>,
        frameworkDataUploadFunction: (companyId: String, frameworkData: T) -> DataMetaInformation
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        listOfCompanyInformation.forEach { companyInformation ->
            val receivedStoredCompany = companyDataControllerApi.postCompany(companyInformation)
            listOfFrameworkData.forEach { frameworkDataSet ->
                val receivedDataMetaInformation =
                    frameworkDataUploadFunction(receivedStoredCompany.companyId, frameworkDataSet)
                listOfUploadInfo.add(
                    UploadInfo(
                        companyInformation,
                        receivedStoredCompany,
                        receivedDataMetaInformation
                    )
                )
            }
        }
        return listOfUploadInfo
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDataSetsPerCompany: Int,
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        companyInformationPerFramework.keys.forEach {
            when (it) {
                DataTypeEnum.lksg -> listOfUploadInfo.addAll(
                    uploadCompanyAndFrameworkDataForOneFramework(
                        companyInformationPerFramework[it]!!,
                        testDataProviderForLksgData.getTData(numberOfDataSetsPerCompany), lksgUploaderFunction
                    )
                )
                DataTypeEnum.sfdr -> listOfUploadInfo.addAll(
                    uploadCompanyAndFrameworkDataForOneFramework(
                        companyInformationPerFramework[it]!!,
                        testDataProviderForSfdrData.getTData(numberOfDataSetsPerCompany), sfdrUploaderFunction
                    )
                )
                DataTypeEnum.sme -> listOfUploadInfo.addAll(
                    uploadCompanyAndFrameworkDataForOneFramework(
                        companyInformationPerFramework[it]!!,
                        testDataProviderForSmeData.getTData(numberOfDataSetsPerCompany), smeUploaderFunction
                    )
                )
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials -> listOfUploadInfo.addAll(
                    uploadCompanyAndFrameworkDataForOneFramework(
                        companyInformationPerFramework[it]!!,
                        testDataProviderForEuTaxonomyDataForNonFinancials.getTData(numberOfDataSetsPerCompany),
                        euTaxonomyNonFinancialsUploaderFunction
                    )
                )
                DataTypeEnum.eutaxonomyMinusFinancials -> listOfUploadInfo.addAll(
                    uploadCompanyAndFrameworkDataForOneFramework(
                        companyInformationPerFramework[it]!!,
                        testDataProviderEuTaxonomyForFinancials.getTData(numberOfDataSetsPerCompany),
                        euTaxonomyFinancialsUploaderFunction
                    )
                )
            }
        }
        return listOfUploadInfo
    }

    fun uploadNCompaniesWithoutIdentifiers(numCompanies: Int): List<UploadInfo> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val listOfCompanyInformation = testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithoutIdentifiers(numCompanies)
        val listOfUploadInfos = mutableListOf<UploadInfo>()
        listOfCompanyInformation.forEach {
                companyInformation ->
            listOfUploadInfos.add(
                UploadInfo(
                    companyInformation,
                    companyDataControllerApi.postCompany(companyInformation)
                )
            )
        }
        return listOfUploadInfos
    }

    fun uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(setAsTeaserCompany: Boolean): UploadInfo {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = setAsTeaserCompany)
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithRandomIdentifier(): UploadInfo {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = generalTestDataProvider
            .generateCompanyInformation("NameDoesNotMatter", "SectorDoesNotMatter")
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun getCompaniesOnlyByName(searchString: String): List<StoredCompany> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        return companyDataControllerApi.getCompanies(
            searchString,
            onlyCompanyNames = true
        )
    }

    fun getNumberOfStoredCompanies(): Int {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        return companyDataControllerApi.getCompanies().size
    }

    fun getNumberOfDataMetaInfo(companyId: String? = null, dataType: DataTypeEnum? = null): Int {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        return metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType).size
    }
}

data class UploadInfo(

    val inputCompanyInformation: CompanyInformation,

    val actualStoredCompany: StoredCompany,

    val actualStoredDataMetaInfo: DataMetaInformation? = null

)
