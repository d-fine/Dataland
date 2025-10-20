package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.communitymanager.openApiClient.api.EmailAddressControllerApi
import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.api.DataSourcingControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataDeletionControllerApi
import org.dataland.datalandbackend.openApiClient.api.EutaxonomyFinancialsDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EutaxonomyNonFinancialsDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyFinancialsData
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.BASE_PATH_TO_DATA_SOURCING_SERVICE
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.UnauthorizedCompanyDataControllerApi
import org.dataland.e2etests.customApiControllers.UnauthorizedEuTaxonomyDataNonFinancialsControllerApi
import org.dataland.e2etests.customApiControllers.UnauthorizedMetaDataControllerApi
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.dataland.e2etests.utils.testDataProviders.GeneralTestDataProvider
import java.lang.Thread.sleep
import java.time.LocalDate
import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi as DataSourcingRequestControllerApi

class ApiAccessor {
    companion object {
        private const val UPLOAD_TIMEOUT_IN_S = 10L
    }

    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val companyRolesControllerApi = CompanyRolesControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val emailAddressControllerApi = EmailAddressControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val dataSourcingControllerApi = DataSourcingControllerApi(BASE_PATH_TO_DATA_SOURCING_SERVICE)
    val dataSourcingRequestControllerApi = DataSourcingRequestControllerApi(BASE_PATH_TO_DATA_SOURCING_SERVICE)
    val qaServiceControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)
    val dataDeletionControllerApi = DataDeletionControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()
    val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()
    val unauthorizedEuTaxonomyDataNonFinancialsControllerApi = UnauthorizedEuTaxonomyDataNonFinancialsControllerApi()

    val dataControllerApiForEuTaxonomyNonFinancials =
        EutaxonomyNonFinancialsDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val dataControllerApiForEuTaxonomyFinancials = EutaxonomyFinancialsDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForEuTaxonomyDataForNonFinancials =
        FrameworkTestDataProvider
            .forFrameworkFixtures(EutaxonomyNonFinancialsData::class.java)
    val testDataProviderEuTaxonomyForFinancials =
        FrameworkTestDataProvider.forFrameworkFixtures(EutaxonomyFinancialsData::class.java)

    val dataControllerApiForLksgData = LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForLksgData = FrameworkTestDataProvider.forFrameworkFixtures(LksgData::class.java)
    val dataControllerApiForSfdrData = SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSfdrData = FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java)

    val jwtHelper = JwtAuthenticationHelper()
    val generalTestDataProvider = GeneralTestDataProvider()
    private val qaApiAccessor = QaApiAccessor()

    fun euTaxonomyNonFinancialsUploaderFunction(
        companyId: String,
        euTaxonomyNonFinancialsData: EutaxonomyNonFinancialsData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ) = dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEutaxonomyNonFinancialsData(
        CompanyAssociatedDataEutaxonomyNonFinancialsData(companyId, reportingPeriod, euTaxonomyNonFinancialsData),
        bypassQa,
    )

    fun euTaxonomyFinancialsUploaderFunction(
        companyId: String,
        data: EutaxonomyFinancialsData,
        period: String,
        bypassQa: Boolean = true,
    ) = dataControllerApiForEuTaxonomyFinancials.postCompanyAssociatedEutaxonomyFinancialsData(
        CompanyAssociatedDataEutaxonomyFinancialsData(companyId, period, data), bypassQa,
    )

    fun lksgUploaderFunction(
        companyId: String,
        lksgData: LksgData,
        period: String,
        bypassQa: Boolean = true,
    ) = dataControllerApiForLksgData.postCompanyAssociatedLksgData(
        CompanyAssociatedDataLksgData(companyId, period, lksgData), bypassQa,
    )

    fun sfdrUploaderFunction(
        companyId: String,
        sfdrData: SfdrData,
        period: String,
        bypassQa: Boolean = true,
    ) = dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
        CompanyAssociatedDataSfdrData(companyId, period, sfdrData), bypassQa,
    )

    /**
     * Uploads each of the datasets provided in [frameworkDatasets] for each of the companies provided in [companyInfo].
     * The upload function is provided. Handles authentication and QA as needed. Returns all [UploadInfo] produced.
     */
    fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        companyInfo: List<CompanyInformation>,
        frameworkDatasets: List<T>,
        frameworkDataUploadFunction: (String, T, String, Boolean) -> DataMetaInformation,
        uploadConfig: UploadConfiguration = UploadConfiguration(TechnicalUser.Admin, true),
        reportingPeriod: String = "",
        ensureQaPassed: Boolean = true,
    ): List<UploadInfo> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val storedCompanies = companyInfo.map { companyDataControllerApi.postCompany(it) }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(uploadConfig.uploadingTechnicalUser)
        val listOfUploadInfo = mutableListOf<UploadInfo>()
        for (frameworkDataset in frameworkDatasets) {
            companyInfo.zip(storedCompanies).forEach { (input, stored) ->
                ApiAwait.waitForSuccess(UPLOAD_TIMEOUT_IN_S) {
                    val receivedDataMetaInformation =
                        frameworkDataUploadFunction(
                            stored.companyId, frameworkDataset,
                            reportingPeriod, uploadConfig.bypassQa,
                        )
                    listOfUploadInfo += UploadInfo(input, stored, receivedDataMetaInformation)
                }
            }
        }
        if (ensureQaPassed) {
            qaApiAccessor.ensureQaCompletedAndUpdateUploadInfo(listOfUploadInfo, metaDataControllerApi)
        }
        return listOfUploadInfo
    }

    fun <T> uploadSingleFrameworkDataset(
        companyId: String,
        frameworkData: T,
        reportingPeriod: String,
        frameworkDataUploadFunction: (String, T, String, Boolean) -> DataMetaInformation,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataMetaInformation = frameworkDataUploadFunction(companyId, frameworkData, reportingPeriod, bypassQa)
        return if (bypassQa) {
            qaApiAccessor.ensureQaIsPassed(listOf(dataMetaInformation), metaDataControllerApi)[0]
        } else {
            sleep(5000) // although QA service will not update the meta information, give it some time to process
            dataMetaInformation
        }
    }

    fun uploadDummyFrameworkDataset(
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        fun <T> upload(
            provider: FrameworkTestDataProvider<T>,
            uploader: (String, T, String, Boolean) -> DataMetaInformation,
        ) = uploadSingleFrameworkDataset(companyId, provider.getTData(1).first(), reportingPeriod, uploader, bypassQa)

        return when (dataType) {
            DataTypeEnum.lksg -> upload(testDataProviderForLksgData, ::lksgUploaderFunction)
            DataTypeEnum.sfdr -> upload(testDataProviderForSfdrData, ::sfdrUploaderFunction)
            DataTypeEnum.eutaxonomyMinusNonMinusFinancials ->
                upload(testDataProviderForEuTaxonomyDataForNonFinancials, ::euTaxonomyNonFinancialsUploaderFunction)

            DataTypeEnum.eutaxonomyMinusFinancials ->
                upload(testDataProviderEuTaxonomyForFinancials, ::euTaxonomyFinancialsUploaderFunction)

            else -> throw IllegalArgumentException("Datatype $dataType not integrated into ApiAccessor.")
        }
    }

    private fun uploadForDataType(
        dataType: DataTypeEnum,
        listOfCompanyInformation: List<CompanyInformation>,
        numberOfDatasetsPerCompany: Int,
        uploadConfig: UploadConfiguration = UploadConfiguration(TechnicalUser.Admin, true),
        reportingPeriod: String,
        ensureQaPassed: Boolean,
    ): List<UploadInfo> {
        fun <T> uploadCompaniesAndDatasets(
            testDataProvider: FrameworkTestDataProvider<T>,
            frameworkDataUploadFunction: (String, T, String, Boolean) -> DataMetaInformation,
        ) = uploadCompanyAndFrameworkDataForOneFramework(
            companyInfo = listOfCompanyInformation,
            frameworkDatasets = testDataProvider.getTData(numberOfDatasetsPerCompany),
            frameworkDataUploadFunction = frameworkDataUploadFunction,
            uploadConfig = uploadConfig,
            reportingPeriod = reportingPeriod,
            ensureQaPassed = ensureQaPassed,
        )
        return when (dataType) {
            DataTypeEnum.lksg -> uploadCompaniesAndDatasets(testDataProviderForLksgData, ::lksgUploaderFunction)
            DataTypeEnum.sfdr -> uploadCompaniesAndDatasets(testDataProviderForSfdrData, ::sfdrUploaderFunction)
            DataTypeEnum.eutaxonomyMinusNonMinusFinancials ->
                uploadCompaniesAndDatasets(
                    testDataProviderForEuTaxonomyDataForNonFinancials,
                    ::euTaxonomyNonFinancialsUploaderFunction,
                )

            DataTypeEnum.eutaxonomyMinusFinancials ->
                uploadCompaniesAndDatasets(
                    testDataProviderEuTaxonomyForFinancials,
                    ::euTaxonomyFinancialsUploaderFunction,
                )

            else -> throw IllegalArgumentException("The datatype $dataType is not integrated into the ApiAccessor yet")
        }
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDatasetsPerCompany: Int,
        uploadConfig: UploadConfiguration = UploadConfiguration(TechnicalUser.Admin, true),
        reportingPeriod: String = "",
        ensureQaPassed: Boolean = true,
    ): List<UploadInfo> =
        companyInformationPerFramework.flatMap { (dataType, companies) ->
            uploadForDataType(
                dataType = dataType,
                listOfCompanyInformation = companies,
                numberOfDatasetsPerCompany = numberOfDatasetsPerCompany,
                uploadConfig = uploadConfig,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = ensureQaPassed,
            )
        }

    fun uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
        companyInformation: CompanyInformation,
        euTaxonomyDataForNonFinancials: EutaxonomyNonFinancialsData,
        ensureQaPassed: Boolean = true,
    ): Map<String, String> {
        val listOfUploadInfo =
            uploadCompanyAndFrameworkDataForOneFramework(
                listOf(companyInformation),
                listOf(euTaxonomyDataForNonFinancials),
                ::euTaxonomyNonFinancialsUploaderFunction,
                ensureQaPassed = ensureQaPassed,
            ).first()
        val companyId = listOfUploadInfo.actualStoredCompany.companyId
        val dataId = listOfUploadInfo.actualStoredDataMetaInfo!!.dataId
        return mapOf("companyId" to companyId, "dataId" to dataId)
    }

    fun uploadNCompaniesWithoutIdentifiers(numCompanies: Int): List<UploadInfo> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        return testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithoutIdentifiers(numCompanies)
            .map { companyInformation ->
                UploadInfo(
                    companyInformation,
                    companyDataControllerApi.postCompany(companyInformation),
                )
            }
    }

    fun uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(setAsTeaserCompany: Boolean): UploadInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyInformation =
            testDataProviderEuTaxonomyForFinancials
                .getCompanyInformationWithoutIdentifiers(1)
                .first()
                .copy(isTeaserCompany = setAsTeaserCompany, companyContactDetails = emptyList())
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithRandomIdentifier(): UploadInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyInformation =
            generalTestDataProvider.generateCompanyInformation("NameDoesNotMatter", "SectorDoesNotMatter")
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithRandomIdentifierFYEAndReportingShift(): UploadInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyInformation =
            generalTestDataProvider.generateCompanyInformationWithFYEAndReportingShift(LocalDate.of(2023, 12, 31), 0)
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithIdentifiers(
        lei: String? = null,
        isins: List<String>? = null,
        permId: String? = null,
    ): UploadInfo? {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        if (lei.isNullOrEmpty() && isins.isNullOrEmpty() && permId.isNullOrEmpty()) return null
        val testCompanyInformation = generalTestDataProvider.generateCompanyInformationWithNameAndIdentifiers(lei, isins, permId)
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun getCompaniesByNameAndIdentifier(searchString: String): List<BasicCompanyInformation> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies(searchString)
    }

    fun getNumberOfStoredCompanies(): Int {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies().size
    }

    fun getNumberOfDataMetaInfo(
        companyId: String? = null,
        dataType: DataTypeEnum? = null,
        showOnlyActive: Boolean? = null,
        reportingPeriod: String? = null,
    ): Int {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return metaDataControllerApi.getListOfDataMetaInfo(companyId, dataType, showOnlyActive, reportingPeriod).size
    }

    /**
     * Upload the dataset provided in [frameworkData] via [uploadFunction] for the given [companyId] and
     * [reportingPeriod] waiting 1ms after the upload. The wait circumvents error 500 if frameworkdata for the
     * same company and reporting period is uploaded multiple times. It is also ensured that QA is passed before
     * returning the current metadata of the uploaded data.
     */
    fun <T> uploadWithWait(
        companyId: String,
        frameworkData: T,
        reportingPeriod: String,
        uploadFunction: (String, T, String, Boolean) -> DataMetaInformation,
    ): DataMetaInformation {
        val uploadedMetaData = uploadSingleFrameworkDataset(companyId, frameworkData, reportingPeriod, uploadFunction)
        sleep(1L)
        return uploadedMetaData
    }
}

data class UploadInfo(
    val inputCompanyInformation: CompanyInformation,
    val actualStoredCompany: StoredCompany,
    var actualStoredDataMetaInfo: DataMetaInformation? = null,
)
