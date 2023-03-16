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
import org.dataland.datalandbackend.openApiClient.model.QAStatus
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.unauthorizedApiControllers.UnauthorizedCompanyDataControllerApi
import org.dataland.e2etests.unauthorizedApiControllers.UnauthorizedEuTaxonomyDataNonFinancialsControllerApi
import org.dataland.e2etests.unauthorizedApiControllers.UnauthorizedMetaDataControllerApi

class ApiAccessor {

    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    val jwtHelper = JwtAuthenticationHelper()

    val generalTestDataProvider = GeneralTestDataProvider()

    val dataControllerApiForEuTaxonomyNonFinancials =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedEuTaxonomyDataNonFinancialsControllerApi = UnauthorizedEuTaxonomyDataNonFinancialsControllerApi()
    val testDataProviderForEuTaxonomyDataForNonFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForNonFinancials::class.java)
    val euTaxonomyNonFinancialsUploaderFunction =
        { companyId: String, euTaxonomyNonFinancialsData: EuTaxonomyDataForNonFinancials ->
            val companyAssociatedEuTaxonomyNonFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, euTaxonomyNonFinancialsData)
            dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                companyAssociatedEuTaxonomyNonFinancialsData,
            )
        }

    val dataControllerApiForEuTaxonomyFinancials =
        EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderEuTaxonomyForFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForFinancials::class.java)
    val euTaxonomyFinancialsUploaderFunction =
        { companyId: String, euTaxonomyFinancialsData: EuTaxonomyDataForFinancials ->
            val companyAssociatedEuTaxonomyFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForFinancials(companyId, euTaxonomyFinancialsData)
            dataControllerApiForEuTaxonomyFinancials.postCompanyAssociatedEuTaxonomyDataForFinancials(
                companyAssociatedEuTaxonomyFinancialsData,
            )
        }

    val dataControllerApiForLksgData =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForLksgData =
        FrameworkTestDataProvider(LksgData::class.java)
    val lksgUploaderFunction = { companyId: String, lksgData: LksgData ->
        val companyAssociatedLksgData = CompanyAssociatedDataLksgData(companyId, lksgData)
        dataControllerApiForLksgData.postCompanyAssociatedLksgData(
            companyAssociatedLksgData,
        )
    }

    val dataControllerApiForSfdrData =
        SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSfdrData =
        FrameworkTestDataProvider(SfdrData::class.java)
    val sfdrUploaderFunction = { companyId: String, sfdrData: SfdrData ->
        val companyAssociatedSfdrData = CompanyAssociatedDataSfdrData(companyId, sfdrData)
        dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            companyAssociatedSfdrData,
        )
    }

    val dataControllerApiForSmeData =
        SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSmeData =
        FrameworkTestDataProvider(SmeData::class.java)
    val smeUploaderFunction = { companyId: String, smeData: SmeData ->
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, smeData)
        dataControllerApiForSmeData.postCompanyAssociatedSmeData(
            companyAssociatedSmeData,
        )
    }

    fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        listOfCompanyInformation: List<CompanyInformation>,
        listOfFrameworkData: List<T>,
        frameworkDataUploadFunction: (companyId: String, frameworkData: T) -> DataMetaInformation,
        uploadingTechnicalUser: TechnicalUser = TechnicalUser.Uploader,
        ensureQaPassed: Boolean = true,
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(uploadingTechnicalUser)
        listOfCompanyInformation.forEach { companyInformation ->
            val receivedStoredCompany = companyDataControllerApi.postCompany(companyInformation)
            listOfFrameworkData.forEach { frameworkDataSet ->
                val receivedDataMetaInformation =
                    frameworkDataUploadFunction(receivedStoredCompany.companyId, frameworkDataSet)
                listOfUploadInfo.add(
                    UploadInfo(
                        companyInformation,
                        receivedStoredCompany,
                        receivedDataMetaInformation,
                    ),
                )
            }
        }
        if (!ensureQaPassed) return listOfUploadInfo
        return ensureQaCompleted(listOfUploadInfo)
    }

    private fun ensureQaCompleted(listOfUploadInfo: MutableList<UploadInfo>): MutableList<UploadInfo> {
        // Check after upload to ensure that dummy QA is passed and wait until it is
        val maxQaPassedYetRetries = 100; val sleepIfQaNotPassedYetMs: Long = 50

        val result = mutableListOf<UploadInfo>()
        repeat(maxQaPassedYetRetries) {
            for (idx in listOfUploadInfo.indices.reversed()) {
                var metaData = listOfUploadInfo[idx].actualStoredDataMetaInfo!!
                listOfUploadInfo[idx].actualStoredDataMetaInfo = metaDataControllerApi.getDataMetaInfo(metaData.dataId)
                metaData = listOfUploadInfo[idx].actualStoredDataMetaInfo!!
                if (metaData.qaStatus == QAStatus.accepted) {
                    result.add(listOfUploadInfo[idx])
                    listOfUploadInfo.removeAt(idx)
                }
            }
            if (listOfUploadInfo.isEmpty()) return result
            Thread.sleep(sleepIfQaNotPassedYetMs)
        }
        class QaNotCompletedException(message: String) : RuntimeException(message)
        throw QaNotCompletedException(
            "$maxQaPassedYetRetries retries every $sleepIfQaNotPassedYetMs ms, qa for $listOfUploadInfo still failed",
        )
    }

    @Suppress("kotlin:S138")
    private fun uploadForDataType(
        dataType: DataTypeEnum,
        listOfCompanyInformation: List<CompanyInformation>,
        numberOfDataSetsPerCompany: Int,
        uploadingTechnicalUser: TechnicalUser = TechnicalUser.Uploader,
    ): List<UploadInfo> {
        return when (dataType) {
            DataTypeEnum.lksg -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForLksgData.getTData(numberOfDataSetsPerCompany),
                lksgUploaderFunction,
                uploadingTechnicalUser,
            )

            DataTypeEnum.sfdr -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForSfdrData.getTData(numberOfDataSetsPerCompany),
                sfdrUploaderFunction,
                uploadingTechnicalUser,
            )

            DataTypeEnum.sme -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForSmeData.getTData(numberOfDataSetsPerCompany),
                smeUploaderFunction,
                uploadingTechnicalUser,
            )

            DataTypeEnum.eutaxonomyMinusNonMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForEuTaxonomyDataForNonFinancials.getTData(numberOfDataSetsPerCompany),
                euTaxonomyNonFinancialsUploaderFunction,
                uploadingTechnicalUser,
            )

            DataTypeEnum.eutaxonomyMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderEuTaxonomyForFinancials.getTData(numberOfDataSetsPerCompany),
                euTaxonomyFinancialsUploaderFunction,
                uploadingTechnicalUser,
            )
        }
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDataSetsPerCompany: Int,
        uploadingTechnicalUser: TechnicalUser = TechnicalUser.Uploader,
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        companyInformationPerFramework.keys.forEach {
            listOfUploadInfo.addAll(
                uploadForDataType(
                    it,
                    companyInformationPerFramework[it]!!,
                    numberOfDataSetsPerCompany,
                    uploadingTechnicalUser,
                ),
            )
        }
        return listOfUploadInfo
    }

    fun uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
        companyInformation: CompanyInformation,
        euTaxonomyDataForNonFinancials: EuTaxonomyDataForNonFinancials,
    ):
        Map<String, String> {
        val listOfUploadInfo = uploadCompanyAndFrameworkDataForOneFramework(
            listOf(companyInformation),
            listOf(euTaxonomyDataForNonFinancials),
            euTaxonomyNonFinancialsUploaderFunction,
        )
        val companyId = listOfUploadInfo[0].actualStoredCompany.companyId
        val dataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        return mapOf("companyId" to companyId, "dataId" to dataId)
    }

    fun uploadNCompaniesWithoutIdentifiers(numCompanies: Int): List<UploadInfo> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val listOfCompanyInformation = testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithoutIdentifiers(numCompanies)
        val listOfUploadInfos = mutableListOf<UploadInfo>()
        listOfCompanyInformation.forEach { companyInformation ->
            listOfUploadInfos.add(
                UploadInfo(
                    companyInformation,
                    companyDataControllerApi.postCompany(companyInformation),
                ),
            )
        }
        return listOfUploadInfos
    }

    fun uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(setAsTeaserCompany: Boolean): UploadInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testCompanyInformation = testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = setAsTeaserCompany)
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithRandomIdentifier(): UploadInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testCompanyInformation = generalTestDataProvider
            .generateCompanyInformation("NameDoesNotMatter", "SectorDoesNotMatter")
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun getCompaniesOnlyByName(searchString: String): List<StoredCompany> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies(
            searchString,
            onlyCompanyNames = true,
        )
    }

    fun getNumberOfStoredCompanies(): Int {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies().size
    }

    fun getNumberOfDataMetaInfo(companyId: String? = null, dataType: DataTypeEnum? = null): Int {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType).size
    }
}

data class UploadInfo(

    val inputCompanyInformation: CompanyInformation,

    val actualStoredCompany: StoredCompany,

    var actualStoredDataMetaInfo: DataMetaInformation? = null,

)
