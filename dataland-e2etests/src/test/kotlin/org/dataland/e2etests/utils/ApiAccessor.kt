package org.dataland.e2etests.utils

import org.dataland.communitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.communitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataDeletionControllerApi
import org.dataland.datalandbackend.openApiClient.api.EutaxonomyFinancialsDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EutaxonomyNonFinancialsDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.LksgDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.P2pDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataLksgData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataPathwaysToParisData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyFinancialsData
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.PathwaysToParisData
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.customApiControllers.UnauthorizedCompanyDataControllerApi
import org.dataland.e2etests.customApiControllers.UnauthorizedEuTaxonomyDataNonFinancialsControllerApi
import org.dataland.e2etests.customApiControllers.UnauthorizedMetaDataControllerApi
import org.dataland.e2etests.utils.testDataProvivders.FrameworkTestDataProvider
import org.dataland.e2etests.utils.testDataProvivders.GeneralTestDataProvider

class ApiAccessor {
    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    val requestControllerApi = RequestControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
    val companyRolesControllerApi = CompanyRolesControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)

    val qaServiceControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)
    private val qaApiAccessor = QaApiAccessor()

    val jwtHelper = JwtAuthenticationHelper()

    val generalTestDataProvider = GeneralTestDataProvider()

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

    val dataControllerApiForP2pData = P2pDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForP2pData = FrameworkTestDataProvider.forFrameworkFixtures(PathwaysToParisData::class.java)

    fun p2pUploaderFunction(
        companyId: String,
        p2pData: PathwaysToParisData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedP2pData = CompanyAssociatedDataPathwaysToParisData(companyId, reportingPeriod, p2pData)
        return dataControllerApiForP2pData.postCompanyAssociatedP2pData(
            companyAssociatedP2pData, bypassQa,
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
     * [companyInfo] via [frameworkDataUploadFunction]. If data for the same framework is uploaded multiple
     * times for the same company a wait of at least 1ms is necessary to avoid an error 500.
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
        val waitTimeBeforeNextUpload = if (frameworkDatasets.size > 1) 1L else 0L
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val storedCompanyInfos = companyInfo.map { companyDataControllerApi.postCompany(it) }
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(uploadConfig.uploadingTechnicalUser)
        frameworkDatasets.forEach { frameworkDataset ->
            companyInfo.zip(storedCompanyInfos).forEach { pair ->
                val receivedDataMetaInformation =
                    frameworkDataUploadFunction(
                        pair.second.companyId, frameworkDataset, reportingPeriod, uploadConfig.bypassQa,
                    )
                listOfUploadInfo.add(UploadInfo(pair.first, pair.second, receivedDataMetaInformation))
            }
            Thread.sleep(waitTimeBeforeNextUpload)
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
        ) -> DataMetaInformation,
    ): DataMetaInformation {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataMetaInformation = frameworkDataUploadFunction(companyId, frameworkData, reportingPeriod)
        return qaApiAccessor.ensureQaIsPassed(listOf(dataMetaInformation), metaDataControllerApi)[0]
    }

    @Suppress("kotlin:S138")
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
                uploadCompaniesAndDatasets(
                    testDataProvider = testDataProviderForLksgData,
                    frameworkDataUploadFunction = this::lksgUploaderFunction,
                )

            DataTypeEnum.sfdr ->
                uploadCompaniesAndDatasets(
                    testDataProvider = testDataProviderForSfdrData,
                    frameworkDataUploadFunction = this::sfdrUploaderFunction,
                )

            DataTypeEnum.eutaxonomyMinusNonMinusFinancials ->
                uploadCompaniesAndDatasets(
                    testDataProvider = testDataProviderForEuTaxonomyDataForNonFinancials,
                    frameworkDataUploadFunction = this::euTaxonomyNonFinancialsUploaderFunction,
                )

            DataTypeEnum.eutaxonomyMinusFinancials ->
                uploadCompaniesAndDatasets(
                    testDataProvider = testDataProviderEuTaxonomyForFinancials,
                    frameworkDataUploadFunction = this::euTaxonomyFinancialsUploaderFunction,
                )

            DataTypeEnum.p2p ->
                uploadCompaniesAndDatasets(
                    testDataProvider = testDataProviderForP2pData,
                    frameworkDataUploadFunction = this::p2pUploaderFunction,
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
    ): Map<String, String> {
        val listOfUploadInfo =
            uploadCompanyAndFrameworkDataForOneFramework(
                listOf(companyInformation),
                listOf(euTaxonomyDataForNonFinancials),
                this::euTaxonomyNonFinancialsUploaderFunction,
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
        uploadFunction: (String, T, String) -> DataMetaInformation,
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
