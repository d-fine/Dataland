package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportMetaInformationEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaReportMetaInformationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * A service class for managing QA report meta-information
 */
@Component("QaReportMetaInformationManager")
class QaReportMetaInformationManager(
    @Autowired private val qaReportMetaInformationRepository: QaReportMetaInformationRepository,
) {

    /**
     * Method to make the QA report manager get meta info about one specific QA report
     * @param qaReportId filters the requested meta info to one specific QA report ID
     * @return meta info about QA report behind the qaReportId
     */
    fun getDataMetaInformationByQaReportId(qaReportId: String): QaReportMetaInformationEntity {
        return qaReportMetaInformationRepository.findById(qaReportId).orElseThrow {
            ResourceNotFoundApiException(
                "QA report not found",
                "No QA report with the id: $qaReportId could be found.",
            )
        }
    }

    /**
     * Method to make the QA report manager get all meta infos associated with a data set
     * @param dataId filters the requested meta info to one specific data ID
     * @return a list of meta info about QA reports associated to the data set
     */
    fun searchQaReportMetaInfo(
        dataId: String,
        reporterUserId: String?,
    ): List<QaReportMetaInformationEntity> {
        return qaReportMetaInformationRepository.searchQaReportMetaInformation(dataId, reporterUserId)
    }
}
