package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.model.QaStatus
import org.mockito.kotlin.mock
import java.util.UUID

const val DEFAULT_REPORTING_PERIOD = "2023"
const val DEFAULT_FRAMEWORK = "sfdr"
const val DEFAULT_COMPANY_ID = "46b5374b-a720-43e6-9c5e-9dd92bd95b33"
const val DEFAULT_DATA_POINT_TYPE = "extendedDateFiscalYearEnd"

/**
 * Utility class to create and store test data in the database during SpringBootTests
 */
class DataBaseCreationUtils(
    val storedCompanyRepository: StoredCompanyRepository = mock<StoredCompanyRepository>(),
    val dataMetaInformationRepository: DataMetaInformationRepository = mock<DataMetaInformationRepository>(),
    val dataPointMetaInformationRepository: DataPointMetaInformationRepository = mock<DataPointMetaInformationRepository>(),
) {
    val defaultCompany = storeCompany()
    val defaultQaStatus = QaStatus.Accepted

    private fun getUUID(): String = UUID.randomUUID().toString()

    private fun getCurrentTime(): Long = System.currentTimeMillis()

    /**
     * Wrapper function to enable simple company storage in the database with default values, which can be overridden.
     */
    @Suppress("LongParameterList")
    fun storeCompany(
        companyId: String = DEFAULT_COMPANY_ID,
        companyName: String = "Test Company",
        companyAlternativeNames: List<String>? = null,
        companyContactDetails: List<String>? = null,
        companyLegalForm: String? = null,
        headquarters: String = "Berlin",
        headquartersPostalCode: String? = null,
        sector: String? = null,
        sectorCodeWz: String? = null,
        identifiers: MutableList<CompanyIdentifierEntity> = mutableListOf(),
        parentCompanyLei: String? = null,
        dataRegisteredByDataland: MutableList<DataMetaInformationEntity> = mutableListOf(),
        countryCode: String = "DE",
        isTeaserCompany: Boolean = false,
        website: String? = null,
        associatedSubdomains: List<String>? = null,
    ): StoredCompanyEntity =
        storedCompanyRepository.saveAndFlush(
            StoredCompanyEntity(
                companyId = companyId,
                companyName = companyName,
                companyAlternativeNames = companyAlternativeNames,
                companyContactDetails = companyContactDetails,
                companyLegalForm = companyLegalForm,
                headquarters = headquarters,
                headquartersPostalCode = headquartersPostalCode,
                sector = sector,
                sectorCodeWz = sectorCodeWz,
                identifiers = identifiers,
                parentCompanyLei = parentCompanyLei,
                dataRegisteredByDataland = dataRegisteredByDataland,
                countryCode = countryCode,
                isTeaserCompany = isTeaserCompany,
                website = website,
                associatedSubdomains = associatedSubdomains,
            ),
        )

    /**
     * Wrapper function to enable simple dataset metadata storage in the database with default values, which can be overridden.
     */
    @Suppress("LongParameterList")
    fun storeDatasetMetaData(
        dataId: String = getUUID(),
        company: StoredCompanyEntity = defaultCompany,
        dataType: String = DEFAULT_FRAMEWORK,
        uploaderUserId: String = getUUID(),
        uploadTime: Long = getCurrentTime(),
        reportingPeriod: String = DEFAULT_REPORTING_PERIOD,
        currentlyActive: Boolean? = true,
        qaStatus: QaStatus = defaultQaStatus,
    ): DataMetaInformationEntity =
        dataMetaInformationRepository.saveAndFlush(
            DataMetaInformationEntity(
                dataId = dataId,
                company = company,
                dataType = dataType,
                uploaderUserId = uploaderUserId,
                uploadTime = uploadTime,
                reportingPeriod = reportingPeriod,
                currentlyActive = currentlyActive,
                qaStatus = qaStatus,
            ),
        )

    /**
     * Wrapper function to enable simple data point metadata storage in the database with default values, which can be overridden.
     */
    @Suppress("LongParameterList")
    fun storeDataPointMetaData(
        dataPointId: String = getUUID(),
        companyId: String = DEFAULT_COMPANY_ID,
        dataPointType: String = DEFAULT_DATA_POINT_TYPE,
        reportingPeriod: String = DEFAULT_REPORTING_PERIOD,
        uploaderUserId: String = getUUID(),
        uploadTime: Long = getCurrentTime(),
        currentlyActive: Boolean? = true,
        qaStatus: QaStatus = defaultQaStatus,
    ) {
        dataPointMetaInformationRepository.saveAndFlush(
            DataPointMetaInformationEntity(
                dataPointId = dataPointId,
                companyId = companyId,
                dataPointType = dataPointType,
                reportingPeriod = reportingPeriod,
                uploaderUserId = uploaderUserId,
                uploadTime = uploadTime,
                currentlyActive = currentlyActive,
                qaStatus = qaStatus,
            ),
        )
    }
}
