package org.dataland.datalandqaservice.controller

import jakarta.transaction.Transactional
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.utils.NoBackendRequestQaReportConfiguration
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
        NoBackendRequestQaReportConfiguration::class,
    ],
)
class QaControllerTest
