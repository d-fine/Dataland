package org.dataland.datalandqaservice.configurations

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration class that holds the list of exempt field identifiers per framework
 * for the automatic pre-approval workflow.
 *
 * Fields listed here are excluded from automatic pre-approval and must always be
 * reviewed manually, regardless of their QA report verdicts.
 * Only frameworks with non-empty exempt field lists are included.
 */
@ConfigurationProperties(prefix = "dataland.qa-service.preapproval")
data class PreApprovalExemptFieldsConfig(
    val exemptFields: Map<DataTypeEnum, Set<String>> = emptyMap(),
)
