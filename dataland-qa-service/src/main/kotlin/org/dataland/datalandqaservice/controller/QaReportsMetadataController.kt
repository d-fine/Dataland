package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.QaReportsMetadataApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataAndQaReportMetadata
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class QaReportsMetadataController : QaReportsMetadataApi {
    override fun getQaReportsMetadata(
        uploaderUserIds: Set<UUID>?,
        showOnlyActive: Boolean,
        qaStatus: Set<QaStatus>?,
        startDate: String?,
        endDate: String?,
    ): ResponseEntity<List<DataAndQaReportMetadata>> {
        // 1. add parameter to API companyIdentifier: String?
        // 2. search for datalandCompanyId by search (LEI, ISIN, ...)
        //    autowire CompanyController
        //    use get endpoint to get datalandCompanyId
        //    search only if company search string is not null
        //    see singleDataRequestManager in community-manager
        //    see findDatalandCompanyIdForCompanyIdentifier
        // 3. use new metadata endpoint in backend to retrieve dataIds for datasets
        //     use uploaderUserIds, qaStatus, datalandCompanyIds
        // 4. search in qa_reports repository for qa_reports matching
        //   - data_id IN dataIds
        //   - startDate
        //   - endDate
        //   - onlyActive QA Reports
        // 5. we have all data. now combine to return

        TODO("Not yet implemented")
    }
}
