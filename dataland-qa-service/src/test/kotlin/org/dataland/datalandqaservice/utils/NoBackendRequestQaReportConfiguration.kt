package org.dataland.datalandqaservice.utils

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportSecurityPolicy
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.UserAuthenticatedBackendClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * A configuration class that disables backend data-existence and access checks
 * for QA reports
 */
@Configuration
class NoBackendRequestQaReportConfiguration {
    @Bean
    @Primary
    fun getQaReportSecurityPolicy(
        @Autowired userAuthenticatedBackendClient: UserAuthenticatedBackendClient,
    ): QaReportSecurityPolicy = NoBackendRequestQaReportSecurityPolicy(userAuthenticatedBackendClient)
}
