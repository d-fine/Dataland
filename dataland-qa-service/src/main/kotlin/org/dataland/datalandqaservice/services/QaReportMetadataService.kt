package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
     * @param startDate start date of the search
     * @param endDate end date of the search
     * @param companyIdentifier external identifier of the company
     * @return a list of all data and the connected meta information associated with a data set
     */
    fun searchDataAndQaReportMetadata(
        uploaderUserIds: Set<UUID>?,
        showOnlyActive: Boolean,
        qaStatus: QaStatus?,
        startDate: String?,
        endDate: String?,
        companyIdentifier: String?,
    ): List<DataAndQaReportMetadata> {
        val companyId: String? = getCompanyIdFromCompanyIdentifier(companyIdentifier)
        val dataMetaInformation: List<DataMetaInformation> = metadataController
            .getListOfDataMetaInfo(
                companyId, null, showOnlyActive, null, uploaderUserIds, qaStatus,
            )
        val dataIds: List<String> = dataMetaInformation.stream().map { it.dataId }.toList()
        val qaReportEntities: List<QaReportEntity> = qaReportRepository
            .searchQaReportMetaInformation(dataIds, showOnlyActive, startDate, endDate)
        val qaReportMap: Map<String, QaReportEntity> = qaReportEntities.associateBy { it.dataId }
        return dataMetaInformation.mapNotNull { metaInformation ->
            qaReportMap[metaInformation.dataId]?.let { qaEntity ->
                DataAndQaReportMetadata(metaInformation, qaEntity.toMetaInformationApiModel())
            }
        }
    }

    private fun getCompanyIdFromCompanyIdentifier(companyIdentifier: String?): String? {
        var companyId: String? = null
        if (companyIdentifier != null) {
            val matchingCompanyIdsAndNamesOnDataland =
                companyController.getCompaniesBySearchString(companyIdentifier)
            companyId = if (matchingCompanyIdsAndNamesOnDataland.size == 1) {
                matchingCompanyIdsAndNamesOnDataland.first().companyId
            } else if (matchingCompanyIdsAndNamesOnDataland.size > 1) {
                throw InvalidInputApiException(
                    summary = "No unique identifier. Multiple companies could be found.",
                    message = "Multiple companies have been found for the identifier you specified.",
                )
            } else {
                null
            }
        }
        return companyId
    }
}
