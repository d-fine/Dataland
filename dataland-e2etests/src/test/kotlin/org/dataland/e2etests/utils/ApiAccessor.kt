package org.dataland.e2etests.utils

import org.awaitility.Awaitility.await
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
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifier
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForFinancials
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.SfdrData
import org.dataland.datalandbackend.openApiClient.model.SmeData
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.unauthorizedApiControllers.UnauthorizedCompanyDataControllerApi
import org.dataland.e2etests.unauthorizedApiControllers.UnauthorizedEuTaxonomyDataNonFinancialsControllerApi
import org.dataland.e2etests.unauthorizedApiControllers.UnauthorizedMetaDataControllerApi
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

class ApiAccessor {
    val frameworkData = setOf(
        DataTypeEnum.lksg, DataTypeEnum.eutaxonomyMinusFinancials,
        DataTypeEnum.eutaxonomyMinusNonMinusFinancials, DataTypeEnum.sfdr,
    )

    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedCompanyDataControllerApi = UnauthorizedCompanyDataControllerApi()

    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedMetaDataControllerApi = UnauthorizedMetaDataControllerApi()

    val qaServiceControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)

    val jwtHelper = JwtAuthenticationHelper()

    val generalTestDataProvider = GeneralTestDataProvider()

    val dataControllerApiForEuTaxonomyNonFinancials =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val unauthorizedEuTaxonomyDataNonFinancialsControllerApi = UnauthorizedEuTaxonomyDataNonFinancialsControllerApi()
    val testDataProviderForEuTaxonomyDataForNonFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForNonFinancials::class.java)
    fun euTaxonomyNonFinancialsUploaderFunction(
        companyId: String,
        euTaxonomyNonFinancialsData: EuTaxonomyDataForNonFinancials,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedEuTaxonomyNonFinancialsData =
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                companyId,
                reportingPeriod,
                euTaxonomyNonFinancialsData,
            )
        return dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            companyAssociatedEuTaxonomyNonFinancialsData, bypassQa,
        )
    }

    val dataControllerApiForEuTaxonomyFinancials =
        EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderEuTaxonomyForFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForFinancials::class.java)
    fun euTaxonomyFinancialsUploaderFunction(
        companyId: String,
        euTaxonomyFinancialsData: EuTaxonomyDataForFinancials,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedEuTaxonomyFinancialsData =
            CompanyAssociatedDataEuTaxonomyDataForFinancials(companyId, reportingPeriod, euTaxonomyFinancialsData)
        return dataControllerApiForEuTaxonomyFinancials.postCompanyAssociatedEuTaxonomyDataForFinancials(
            companyAssociatedEuTaxonomyFinancialsData, bypassQa,
        )
    }

    val dataControllerApiForLksgData =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForLksgData =
        FrameworkTestDataProvider(LksgData::class.java)
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

    val dataControllerApiForSfdrData =
        SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSfdrData =
        FrameworkTestDataProvider(SfdrData::class.java)
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

    val dataControllerApiForSmeData =
        SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSmeData =
        FrameworkTestDataProvider(SmeData::class.java)
    fun smeUploaderFunction(
        companyId: String,
        smeData: SmeData,
        reportingPeriod: String,
        bypassQa: Boolean = true,
    ): DataMetaInformation {
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, reportingPeriod, smeData)
        return dataControllerApiForSmeData.postCompanyAssociatedSmeData(
            companyAssociatedSmeData, bypassQa,
        )
    }

    /**
     * Uploads each of the datasets provided in [listOfFrameworkData] for each of the companies provided in
     * [listOfCompanyInformation] via [frameworkDataUploadFunction]. If data for the same framework is uploaded multiple
     * times for the same company a wait of at least 1ms is necessary to avoid an error 500.
     */
    fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        listOfCompanyInformation: List<CompanyInformation>,
        listOfFrameworkData: List<T>,
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
        val waitTimeBeforeNextUpload = if (listOfFrameworkData.size > 1) 1L else 0L
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(uploadConfig.uploadingTechnicalUser)
        val storedCompanyInfos = listOfCompanyInformation.map { companyDataControllerApi.postCompany(it) }
        listOfFrameworkData.forEach { frameworkDataSet ->
            listOfCompanyInformation.zip(storedCompanyInfos).forEach { pair ->
                val receivedDataMetaInformation = frameworkDataUploadFunction(
                    pair.second.companyId, frameworkDataSet, reportingPeriod, uploadConfig.bypassQa,
                )
                listOfUploadInfo.add(UploadInfo(pair.first, pair.second, receivedDataMetaInformation))
            }
            Thread.sleep(waitTimeBeforeNextUpload)
        }
        if (ensureQaPassed) ensureQaCompletedAndUpdateUploadInfo(listOfUploadInfo)
        return listOfUploadInfo
    }

    fun <T> uploadSingleFrameworkDataSet(
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
        return ensureQaIsPassed(listOf(dataMetaInformation))[0]
    }

    /**
     * Wait until QaStatus is accepted for all Upload Infos or throw error. The metadata of the provided uploadInfos
     * are updated in the process.
     *
     * @param uploadInfos List of UploadInfo for which an update of the QaStatus should be checked and awaited
     * @return Input list of UplaodInfo but with updated metadata
     */
    fun ensureQaCompletedAndUpdateUploadInfo(uploadInfos: List<UploadInfo>) {
        await().atMost(10, TimeUnit.SECONDS).until { checkIfQaPassedAndUpdateUploadInfo(uploadInfos) }
    }

    private fun checkIfQaPassedAndUpdateUploadInfo(uploadInfos: List<UploadInfo>): Boolean {
        return uploadInfos.all { uploadInfo ->
            val metaData = uploadInfo.actualStoredDataMetaInfo
                ?: throw NullPointerException(
                    "To check QA Status, metadata is required but was null for $uploadInfo",
                )
            if (metaData.qaStatus != QaStatus.accepted) {
                uploadInfo.actualStoredDataMetaInfo = metaDataControllerApi.getDataMetaInfo(metaData.dataId)
            }
            return uploadInfo.actualStoredDataMetaInfo!!.qaStatus == QaStatus.accepted
        }
    }

    /**
     * Waits until the status of all provided [metaDatas] is QaStatus.Accepted. Then returns an updated list of metaData
     * each of which has qaStatus = QaStatus.Accepted.
     */
    private fun ensureQaIsPassed(metaDatas: List<DataMetaInformation>): List<DataMetaInformation> {
        await().atMost(10, TimeUnit.SECONDS).until { checkIfQaPassedForMetaDataList(metaDatas) }
        val updatedMetaDatas = mutableListOf<DataMetaInformation>()
        metaDatas.forEach { metaData ->
            updatedMetaDatas.add(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
        }
        return updatedMetaDatas
    }

    private fun checkIfQaPassedForMetaDataList(metaDatas: List<DataMetaInformation>): Boolean {
        return metaDatas.all { metaData ->
            return (metaDataControllerApi.getDataMetaInfo(metaData.dataId).qaStatus == QaStatus.accepted)
        }
    }

    @Suppress("kotlin:S138")
    private fun uploadForDataType(
        dataType: DataTypeEnum,
        listOfCompanyInformation: List<CompanyInformation>,
        numberOfDataSetsPerCompany: Int,
        uploadConfig: UploadConfiguration = UploadConfiguration(TechnicalUser.Admin, true),
        reportingPeriod: String,
        ensureQaPassed: Boolean,
    ): List<UploadInfo> {
        return when (dataType) {
            DataTypeEnum.lksg -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation = listOfCompanyInformation,
                listOfFrameworkData = testDataProviderForLksgData.getTData(numberOfDataSetsPerCompany),
                frameworkDataUploadFunction = this::lksgUploaderFunction,
                uploadConfig = uploadConfig,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = ensureQaPassed,
            )

            DataTypeEnum.sfdr -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation = listOfCompanyInformation,
                listOfFrameworkData = testDataProviderForSfdrData.getTData(numberOfDataSetsPerCompany),
                frameworkDataUploadFunction = this::sfdrUploaderFunction,
                uploadConfig = uploadConfig,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = ensureQaPassed,
            )

            DataTypeEnum.sme -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation = listOfCompanyInformation,
                listOfFrameworkData = testDataProviderForSmeData.getTData(numberOfDataSetsPerCompany),
                frameworkDataUploadFunction = this::smeUploaderFunction,
                uploadConfig = uploadConfig,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = ensureQaPassed,
            )

            DataTypeEnum.eutaxonomyMinusNonMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation = listOfCompanyInformation,
                listOfFrameworkData = testDataProviderForEuTaxonomyDataForNonFinancials
                    .getTData(numberOfDataSetsPerCompany),
                frameworkDataUploadFunction = this::euTaxonomyNonFinancialsUploaderFunction,
                uploadConfig = uploadConfig,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = ensureQaPassed,
            )

            DataTypeEnum.eutaxonomyMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation = listOfCompanyInformation,
                listOfFrameworkData = testDataProviderEuTaxonomyForFinancials.getTData(numberOfDataSetsPerCompany),
                frameworkDataUploadFunction = this::euTaxonomyFinancialsUploaderFunction,
                uploadConfig = uploadConfig,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = ensureQaPassed,
            )
        }
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDataSetsPerCompany: Int,
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
                    numberOfDataSetsPerCompany = numberOfDataSetsPerCompany,
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
        euTaxonomyDataForNonFinancials: EuTaxonomyDataForNonFinancials,
    ):
        Map<String, String> {
        val listOfUploadInfo = uploadCompanyAndFrameworkDataForOneFramework(
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
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyInformation = testDataProviderEuTaxonomyForFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = setAsTeaserCompany)
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun uploadOneCompanyWithRandomIdentifier(): UploadInfo {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val testCompanyInformation = generalTestDataProvider
            .generateCompanyInformation("NameDoesNotMatter", "SectorDoesNotMatter")
        return UploadInfo(testCompanyInformation, companyDataControllerApi.postCompany(testCompanyInformation))
    }

    fun getCompaniesOnlyByName(searchString: String): List<StoredCompany> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies(
            frameworkData,
            searchString,
            onlyCompanyNames = true,
        )
    }
    fun getCompaniesByNameAndIdentifier(searchString: String): List<StoredCompany> {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies(
            frameworkData,
            searchString,
        )
    }

    fun getNumberOfStoredCompanies(): Int {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return companyDataControllerApi.getCompanies(frameworkData).size
    }

    fun getNumberOfDataMetaInfo(
        companyId: String? = null,
        dataType: DataTypeEnum? = null,
        showOnlyActive: Boolean? = null,
        reportingPeriod: String? = null,
    ): Int {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        return metaDataControllerApi.getListOfDataMetaInfo(
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
        val uploadedMetaData = uploadSingleFrameworkDataSet(
            companyId = companyId,
            frameworkData = frameworkData,
            frameworkDataUploadFunction = uploadFunction,
            reportingPeriod = reportingPeriod,
        )
        Thread.sleep(waitTime)
        return uploadedMetaData
    }
    fun createCompanyIdentifier(type: CompanyIdentifier.IdentifierType, value: String): CompanyIdentifier {
        return CompanyIdentifier(
            type,
            value,
        )
    }
}

data class UploadInfo(

    val inputCompanyInformation: CompanyInformation,

    val actualStoredCompany: StoredCompany,

    var actualStoredDataMetaInfo: DataMetaInformation? = null,

)
