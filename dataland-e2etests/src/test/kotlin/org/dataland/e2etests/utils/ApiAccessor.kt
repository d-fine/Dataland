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
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

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
        { companyId: String, euTaxonomyNonFinancialsData: EuTaxonomyDataForNonFinancials, reportingPeriod: String ->
            val companyAssociatedEuTaxonomyNonFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                    companyId,
                    reportingPeriod,
                    euTaxonomyNonFinancialsData,
                )
            dataControllerApiForEuTaxonomyNonFinancials.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                companyAssociatedEuTaxonomyNonFinancialsData,
            )
        }

    val dataControllerApiForEuTaxonomyFinancials =
        EuTaxonomyDataForFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderEuTaxonomyForFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForFinancials::class.java)
    val euTaxonomyFinancialsUploaderFunction =
        { companyId: String, euTaxonomyFinancialsData: EuTaxonomyDataForFinancials, reportingPeriod: String ->
            val companyAssociatedEuTaxonomyFinancialsData =
                CompanyAssociatedDataEuTaxonomyDataForFinancials(companyId, reportingPeriod, euTaxonomyFinancialsData)
            dataControllerApiForEuTaxonomyFinancials.postCompanyAssociatedEuTaxonomyDataForFinancials(
                companyAssociatedEuTaxonomyFinancialsData,
            )
        }

    val dataControllerApiForLksgData =
        LksgDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForLksgData =
        FrameworkTestDataProvider(LksgData::class.java)
    val lksgUploaderFunction = { companyId: String, lksgData: LksgData, reportingPeriod: String ->
        val companyAssociatedLksgData = CompanyAssociatedDataLksgData(companyId, reportingPeriod, lksgData)
        dataControllerApiForLksgData.postCompanyAssociatedLksgData(
            companyAssociatedLksgData,
        )
    }

    val dataControllerApiForSfdrData =
        SfdrDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSfdrData =
        FrameworkTestDataProvider(SfdrData::class.java)
    val sfdrUploaderFunction = { companyId: String, sfdrData: SfdrData, reportingPeriod: String ->
        val companyAssociatedSfdrData = CompanyAssociatedDataSfdrData(companyId, reportingPeriod, sfdrData)
        dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(
            companyAssociatedSfdrData,
        )
    }

    val dataControllerApiForSmeData =
        SmeDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val testDataProviderForSmeData =
        FrameworkTestDataProvider(SmeData::class.java)
    val smeUploaderFunction = { companyId: String, smeData: SmeData, reportingPeriod: String ->
        val companyAssociatedSmeData = CompanyAssociatedDataSmeData(companyId, reportingPeriod, smeData)
        dataControllerApiForSmeData.postCompanyAssociatedSmeData(
            companyAssociatedSmeData,
        )
    }

    /**
     * Uploads each of the datasets provided in [listOfFrameworkData] for each of the companies provided in
     * [listOfCompanyInformation] via [frameworkDataUploadFunction]. If data for the same framework is uploaded multiple
     * times for the same company a wait of at least [waitTimeBeforeNextUpload] is necessary to avoid an error 500.
     */
    fun <T> uploadCompanyAndFrameworkDataForOneFramework(
        listOfCompanyInformation: List<CompanyInformation>,
        listOfFrameworkData: List<T>,
        frameworkDataUploadFunction: (
            companyId: String,
            frameworkData: T,
            reportingPeriod: String,
        ) -> DataMetaInformation,
        uploadingTechnicalUser: TechnicalUser = TechnicalUser.Uploader,
        reportingPeriod: String = "",
        ensureQaPassed: Boolean = true,
        waitTimeBeforeNextUpload: Long = 1000
    ): List<UploadInfo> {
        val waitNecessary = (listOfFrameworkData.size>1)
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(uploadingTechnicalUser)
        val storedCompanyInfos = listOfCompanyInformation.map{companyDataControllerApi.postCompany(it)}
        listOfFrameworkData.forEach {frameworkDataSet ->
            listOfCompanyInformation.zip(storedCompanyInfos).forEach { pair ->
                val receivedDataMetaInformation = frameworkDataUploadFunction(pair.second.companyId,frameworkDataSet,reportingPeriod)
                listOfUploadInfo.add(UploadInfo(pair.first,pair.second,receivedDataMetaInformation))
            }
            if (waitNecessary) Thread.sleep(waitTimeBeforeNextUpload)
        }
        if (ensureQaPassed) ensureQaCompletedAndUpdateUploadInfo(listOfUploadInfo)
        return listOfUploadInfo
    }

    /**
     * Wait until QaStatus is accepted for all Upload Infos or throw error. The metadata of the provided uploadInfos
     * are updated in the process.
     *
     * @param uploadInfos List of UploadInfo for which an update of the QAStatus should be checked and awaited
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
            if (metaData.qaStatus != QAStatus.accepted) {
                uploadInfo.actualStoredDataMetaInfo = metaDataControllerApi.getDataMetaInfo(metaData.dataId)
            }
            return uploadInfo.actualStoredDataMetaInfo!!.qaStatus == QAStatus.accepted
        }
    }

    /**
     * Waits until the status of all provided [metaDatas] is QaStatus.Accepted. Then returns an updated list of metaData
     * each of which has qaStatus = QaStatus.Accepted.
     */
    fun ensureQaIsPassed(metaDatas: List<DataMetaInformation>): List<DataMetaInformation> {
        await().atMost(10, TimeUnit.SECONDS).until { checkIfQaPassedForMetaDataList(metaDatas) }
        val updatedMetaDatas = mutableListOf<DataMetaInformation>()
        metaDatas.forEach() { metaData ->
            updatedMetaDatas.add(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
        }
        return updatedMetaDatas
    }

    private fun checkIfQaPassedForMetaDataList(metaDatas: List<DataMetaInformation>): Boolean {
        return metaDatas.all { metaData ->
            return (metaDataControllerApi.getDataMetaInfo(metaData.dataId).qaStatus == QAStatus.accepted)
        }
    }

    @Suppress("kotlin:S138")
    private fun uploadForDataType(
        dataType: DataTypeEnum,
        listOfCompanyInformation: List<CompanyInformation>,
        numberOfDataSetsPerCompany: Int,
        uploadingTechnicalUser: TechnicalUser = TechnicalUser.Uploader,
        reportingPeriod: String,
    ): List<UploadInfo> {
        return when (dataType) {
            DataTypeEnum.lksg -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForLksgData.getTData(numberOfDataSetsPerCompany),
                lksgUploaderFunction,
                uploadingTechnicalUser,
                reportingPeriod,
            )

            DataTypeEnum.sfdr -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForSfdrData.getTData(numberOfDataSetsPerCompany),
                sfdrUploaderFunction,
                uploadingTechnicalUser,
                reportingPeriod,
            )

            DataTypeEnum.sme -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForSmeData.getTData(numberOfDataSetsPerCompany),
                smeUploaderFunction,
                uploadingTechnicalUser,
                reportingPeriod,
            )

            DataTypeEnum.eutaxonomyMinusNonMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderForEuTaxonomyDataForNonFinancials.getTData(numberOfDataSetsPerCompany),
                euTaxonomyNonFinancialsUploaderFunction,
                uploadingTechnicalUser,
                reportingPeriod,
            )

            DataTypeEnum.eutaxonomyMinusFinancials -> uploadCompanyAndFrameworkDataForOneFramework(
                listOfCompanyInformation,
                testDataProviderEuTaxonomyForFinancials.getTData(numberOfDataSetsPerCompany),
                euTaxonomyFinancialsUploaderFunction,
                uploadingTechnicalUser,
                reportingPeriod,
            )
        }
    }

    fun uploadCompanyAndFrameworkDataForMultipleFrameworks(
        companyInformationPerFramework: Map<DataTypeEnum, List<CompanyInformation>>,
        numberOfDataSetsPerCompany: Int,
        uploadingTechnicalUser: TechnicalUser = TechnicalUser.Uploader,
        reportingPeriod: String = "",
    ): List<UploadInfo> {
        val listOfUploadInfo: MutableList<UploadInfo> = mutableListOf()
        companyInformationPerFramework.keys.forEach {
            listOfUploadInfo.addAll(
                uploadForDataType(
                    it,
                    companyInformationPerFramework[it]!!,
                    numberOfDataSetsPerCompany,
                    uploadingTechnicalUser,
                    reportingPeriod,
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
     * Upload the dataset provided in [dataList] via [uploadFunction] multiple times for the given [companyId] and
     * [reportingPeriods] waiting [waitTime] ms in between each of the [n] sets of uploads.
     * The i-th dataset in [dataList] is uploaded [n] times for the i-th reportingPeriod in [reportingPeriods].
     * If [ensureQaIsPassed], it is ensured before return that QA is passed for all uploaded data sets.
     */
    fun <T> repeatUploadWithWait(
        n: Int,
        companyId: String,
        dataList: List<T>,
        reportingPeriods: List<String>,
        waitTime: Long,
        uploadFunction: (String, T, String) -> DataMetaInformation,
        ensureQaIsPassed: Boolean = true
    ): List<DataMetaInformation> {
        if (dataList.size != reportingPeriods.size) throw IllegalArgumentException(
            "Length of provided dataset and reporting period has to be the same."
        )
        val uploadedMetaData: MutableList<DataMetaInformation> = mutableListOf()
        for (i in 1..n) {
            dataList.zip(reportingPeriods).forEach {
                uploadedMetaData.add(uploadFunction(companyId, it.first, it.second))

            }
            if (i != n) Thread.sleep(waitTime)
        }
        return if (ensureQaIsPassed) ensureQaIsPassed(uploadedMetaData) else uploadedMetaData
    }

}

data class UploadInfo(

    val inputCompanyInformation: CompanyInformation,

    val actualStoredCompany: StoredCompany,

    var actualStoredDataMetaInfo: DataMetaInformation? = null,

    )
