package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.api.CompanyRolesControllerApi
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
import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi as DataSourcingRequestControllerApi

class ApiAccessor {
    companion object {
        private const val UPLOAD_TIMEOUT_IN_S = 10L
    }

    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val companyRolesControllerApi = CompanyRolesControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    val dataSourcingControllerApi = DataSourcingControllerApi(BASE_PATH_TO_DATA_SOURCING_SERVICE)
    val dataSourcingRequestControllerApi = DataSourcingRequestControllerApi(BASE_PATH_TO_DATA_SOURCING_SERVICE)

    val qaServiceControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)
    private val qaApiAccessor = QaApiAccessor()

    val jwtHelper = JwtAuthenticationHelper()

    private val generalTestDataProvider = GeneralTestDataProvider()

    val dataControllerApiForEuTaxonomyNonFinancials =
        EutaxonomyNonFinancialsDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedEuTaxonomyDataNonFinancialsControllerApi = UnauthorizedEuTaxonomyDataNonFinancialsControllerApi()
    val testDataProviderForEuTaxonomyDataForNonFinancials =
        FrameworkTestDataProvider.forFrameworkFixtures(EutaxonomyNonFinancialsData::class.java)
    val dataDeletionControllerApi = DataDeletionControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    fun euTaxonomyNonFinancialsUploaderFunction(
        companyId: String,
        euTaxonomyNonFinancialsData: EutaxonomyNonFinancialsData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedEuTaxonomyNonFinancialsData =
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                companyId,
                reportingPeriod,
                euTaxonomyNonFinancialsData,
            )
        return dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEutaxonomyNonFinancialsData(
            companyAssociatedEuTaxonomyNonFinancialsData, bypassQa,
        )
    }

    val dataControllerApiForEuTaxonomyFinancials =
        EutaxonomyFinancialsDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderEuTaxonomyForFinancials =
        FrameworkTestDataProvider.forFrameworkFixtures(EutaxonomyFinancialsData::class.java)

    fun euTaxonomyFinancialsUploaderFunction(
        companyId: String,
        euTaxonomyFinancialsData: EutaxonomyFinancialsData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedEuTaxonomyFinancialsData =
            CompanyAssociatedDataEutaxonomyFinancialsData(companyId, reportingPeriod, euTaxonomyFinancialsData)
        return dataControllerApiForEuTaxonomyFinancials.postCompanyAssociatedEutaxonomyFinancialsData(
            companyAssociatedEuTaxonomyFinancialsData, bypassQa,
        )
    }

    val dataControllerApiForLksgData = LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForLksgData = FrameworkTestDataProvider.forFrameworkFixtures(LksgData::class.java)

    fun lksgUploaderFunction(
        companyId: String,
        lksgData: LksgData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedLksgData = CompanyAssociatedDataLksgData(companyId, reportingPeriod, lksgData)
        return dataControllerApiForLksgData.postCompanyAssociatedLksgData(
            companyAssociatedLksgData, bypassQa,
        )
    }

    val dataControllerApiForSfdrData = SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSfdrData = FrameworkTestDataProvider.forFrameworkFixtures(SfdrData::class.java)

    fun sfdrUploaderFunction(
        companyId: String,
        sfdrData: SfdrData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedSfdrData = CompanyAssociatedDataSfdrData(companyId, reportingPeriod, sfdrData)
        return dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            companyAssociatedSfdrData, bypassQa,
        )
    }

    /**
     * Uploads each of the datasets provided in [frameworkDatasets] for each of the companies provided in
     * [companyInfo] via [frameworkDataUploadFunction].
     */
    fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        companyInfo: List<CompanyInformation>,
        frameworkDatasets: List<T>,
        frameworkDataUploadFunction: (
            companyId: String,
            frameworkData: T,
            reportingPeriod: String,
            bypassQa: Boolean,
        ) -> DataMetaInformation,
        uploadConfig: UploadConfiguration = UploadConfiguration(TechnicalUser.Admin, true),
        reportingPeriod: String = "",
        ensureQaPassed: Boolean = true,
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val storedCompanyInfos = companyInfo.map { companyDataControllerApi.postCompany(it) }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(uploadConfig.uploadingTechnicalUser)
        frameworkDatasets.forEach { frameworkDataset ->
            companyInfo.zip(storedCompanyInfos).forEach { pair ->
                ApiAwait.waitForSuccess(UPLOAD_TIMEOUT_IN_S) {
                    val receivedDataMetaInformation =
                        frameworkDataUploadFunction(
                            pair.second.companyId, frameworkDataset, reportingPeriod, uploadConfig.bypassQa,
                        )
                    listOfUploadInfo.add(UploadInfo(pair.first, pair.second, receivedDataMetaInformation))
                }
            }
        }
        if (ensureQaPassed) qaApiAccessor.ensureQaCompletedAndUpdateUploadInfo(listOfUploadInfo, metaDataControllerApi)
        return listOfUploadInfo
    }

    fun <T> uploadSingleFrameworkDataset(
        companyId: String,
        frameworkData: T,
        reportingPeriod: String,
        frameworkDataUploadFunction: (
            companyId: String,
            frameworkData: T,
            reportingPeriod: String,
            bypassQa: Boolean,
        ) -> DataMetaInformation,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataMetaInformation = frameworkDataUploadFunction(companyId, frameworkData, reportingPeriod, bypassQa)
        return if (bypassQa) {
            qaApiAccessor.ensureQaIsPassed(listOf(dataMetaInformation), metaDataControllerApi)[0]
        } else {
            dataMetaInformation
        }
    }

    fun uploadDummyFrameworkDataset(
        companyId: String,
        dataType: DataTypeEnum,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        fun <T> uploadDataset(
            testDataProvider: FrameworkTestDataProvider<T>,
            frameworkDataUploaderFunction: (
                companyId: String,
                frameworkData: T,
                reportingPeriod: String,
                bypassQa: Boolean,
            ) -> DataMetaInformation,
            byPassQa: Boolean = true,
        ) = uploadSingleFrameworkDataset(
            companyId,
            testDataProvider.getTData(1)[0],
            reportingPeriod,
            frameworkDataUploaderFunction,
            byPassQa,
        )

        return when (dataType) {
            DataTypeEnum.lksg ->
                uploadDataset(testDataProviderForLksgData, this::lksgUploaderFunction, bypassQa)

            DataTypeEnum.sfdr ->
                uploadDataset(testDataProviderForSfdrData, this::sfdrUploaderFunction, bypassQa)

            DataTypeEnum.eutaxonomyMinusNonMinusFinancials ->
                uploadDataset(
                    testDataProviderForEuTaxonomyDataForNonFinancials,
                    this::euTaxonomyNonFinancialsUploaderFunction,
                    bypassQa,
                )

            DataTypeEnum.eutaxonomyMinusFinancials ->
                uploadDataset(testDataProviderEuTaxonomyForFinancials, this::euTaxonomyFinancialsUploaderFunction, bypassQa)

            else -> {
                throw IllegalArgumentException("The datatype $dataType is not integrated into the ApiAccessor yet")
            }
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
            frameworkDataUploadFunction: (
                companyId: String,
                frameworkData: T,
                reportingPeriod: String,
                bypassQa: Boolean,
            ) -> DataMetaInformation,
        ) = uploadCompanyAndFrameworkDataForOneFramework(
            companyInfo = listOfCompanyInformation,
            frameworkDatasets = testDataProvider.getTData(numberOfDatasetsPerCompany),
            frameworkDataUploadFunction = frameworkDataUploadFunction,
            uploadConfig = uploadConfig,
            reportingPeriod = reportingPeriod,
            ensureQaPassed = ensureQaPassed,
        )
        return when (dataType) {
            DataTypeEnum.lksg ->
                uploadCompaniesAndDatasets(testDataProviderForLksgData, this::lksgUploaderFunction)

            DataTypeEnum.sfdr ->
                uploadCompaniesAndDatasets(testDataProviderForSfdrData, this::sfdrUploaderFunction)

            DataTypeEnum.eutaxonomyMinusNonMinusFinancials ->
                uploadCompaniesAndDatasets(
                    testDataProviderForEuTaxonomyDataForNonFinancials,
                    this::euTaxonomyNonFinancialsUploaderFunction,
                )

            DataTypeEnum.eutaxonomyMinusFinancials ->
                uploadCompaniesAndDatasets(
                    testDataProviderEuTaxonomyForFinancials,
                    this::euTaxonomyFinancialsUploaderFunction,
                )

            else -> {
                throw IllegalArgumentException("The datatype $dataType is not integrated into the ApiAccessor yet")
            }
        }
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDatasetsPerCompany: Int,
        uploadConfig: UploadConfiguration = UploadConfiguration(TechnicalUser.Admin, true),
        reportingPeriod: String = "",
        ensureQaPassed: Boolean = true,
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        companyInformationPerFramework.keys.forEach {
            listOfUploadInfo.addAll(
                uploadForDataType(
                    dataType = it,
                    listOfCompanyInformation = companyInformationPerFramework.getValue(it),
                    numberOfDatasetsPerCompany = numberOfDatasetsPerCompany,
                    uploadConfig = uploadConfig,
                    reportingPeriod = reportingPeriod,
                    ensureQaPassed = ensureQaPassed,
                ),
            )
        }
        return listOfUploadInfo
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
                this::euTaxonomyNonFinancialsUploaderFunction,
                ensureQaPassed = ensureQaPassed,
            )
        val companyId = listOfUploadInfo[0].actualStoredCompany.companyId
        val dataId = listOfUploadInfo[0].actualStoredDataMetaInfo!!.dataId
        return mapOf("companyId" to companyId, "dataId" to dataId)
    }

    fun uploadNCompaniesWithoutIdentifiers(numCompanies: Int): List<UploadInfo> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val listOfCompanyInformation =
            testDataProviderEuTaxonomyForFinancials
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
            generalTestDataProvider
                .generateCompanyInformation("NameDoesNotMatter", "SectorDoesNotMatter")
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithIdentifiers(
        lei: String? = null,
        isins: List<String>? = null,
        permId: String? = null,
    ): UploadInfo? {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        if (lei.isNullOrEmpty() && isins.isNullOrEmpty() && permId.isNullOrEmpty()) {
            return null
        }
        val testCompanyInformation =
            generalTestDataProvider
                .generateCompanyInformationWithNameAndIdentifiers(lei, isins, permId)
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun getCompaniesByNameAndIdentifier(searchString: String): List<BasicCompanyInformation> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies(
            searchString,
        )
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
        return metaDataControllerApi
            .getListOfDataMetaInfo(
                companyId,
                dataType,
                showOnlyActive,
                reportingPeriod,
            ).size
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
        val waitTime = 1L
        val uploadedMetaData =
            uploadSingleFrameworkDataset(
                companyId = companyId,
                frameworkData = frameworkData,
                frameworkDataUploadFunction = uploadFunction,
                reportingPeriod = reportingPeriod,
            )
        Thread.sleep(waitTime)
        return uploadedMetaData
    }
}

data class UploadInfo(
    val inputCompanyInformation: CompanyInformation,
    val actualStoredCompany: StoredCompany,
    var actualStoredDataMetaInfo: DataMetaInformation? = null,
)
