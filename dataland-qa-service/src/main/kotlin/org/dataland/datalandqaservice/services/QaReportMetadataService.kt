package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * A service class for handling QA report metadata information.
 */
@Service
class QaReportMetadataService(
    @Autowired private val companyController: CompanyDataControllerApi,
    @Autowired private val qaReportRepository: QaReportRepository,
    @Autowired val metadataController: MetaDataControllerApi,
) {

    /**
     * Method to search all data and the connected meta information associated with a data set.
     * @param uploaderUserIds set of user ids of the uploader
     * @param showOnlyActive whether to show only active data
     * @param qaStatus the qa status
     * @param minUploadDate searches for reports uploaded after the minUploadDate.
     * @param maxUploadDate searches for reports uploaded before the maxUploadDate.
     * @param companyIdentifier external identifier of the company
     * @return a list of all data and the connected meta information associated with a data set
     */
    fun searchDataAndQaReportMetadata(
        uploaderUserIds: Set<UUID>?,
        showOnlyActive: Boolean,
        qaStatus: QaStatus?,
        minUploadDate: LocalDate?,
        maxUploadDate: LocalDate?,
        companyIdentifier: String?,
    ): List<DataAndQaReportMetadata> {
        val companyId: String? = companyIdentifier?.let {
            getCompanyIdFromCompanyIdentifier(it) ?: return emptyList()
        }
        val dataMetaInformation = metadataController.getListOfDataMetaInfo(
            companyId, null, false, null, uploaderUserIds, qaStatus,
        )
        val dataIds = dataMetaInformation.map { it.dataId }
        val startDateMillis = minUploadDate?.let { convertDateToMillis(it) }
        val endDateMillis = maxUploadDate?.let { convertDateToMillis(it) }
        val qaReportEntities = qaReportRepository
            .searchQaReportMetaInformation(dataIds, showOnlyActive, startDateMillis, endDateMillis)
        val qaReportMap = qaReportEntities.associateBy { it.dataId }
        return dataMetaInformation.mapNotNull { metaInformation ->
            qaReportMap[metaInformation.dataId]?.let { qaEntity ->
                DataAndQaReportMetadata(metaInformation, qaEntity.toMetaInformationApiModel())
            }
        }
    }

    private fun convertDateToMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.of("Europe/Berlin"))
            .toInstant()
            .toEpochMilli()
    }

    private fun getCompanyIdFromCompanyIdentifier(companyIdentifier: String): String? {
        val matchingCompanyIdsAndNamesOnDataland = companyController.getCompaniesBySearchString(companyIdentifier)
        return when (matchingCompanyIdsAndNamesOnDataland.size) {
            0 -> null
            1 -> matchingCompanyIdsAndNamesOnDataland[0].companyId
            else -> throw InvalidInputApiException(
                summary = "Company identifier is non unique. Multiple matching companies found.",
                message = "Multiple companies have been found for the identifier you specified. " +
                    "Please specify a unique company identifier.",
            )
        }
    }
}
