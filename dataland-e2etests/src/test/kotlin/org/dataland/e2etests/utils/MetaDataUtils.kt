package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.TechnicalUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

object MetaDataUtils {
    /**
     * Asserts that the data meta info matches the expected data meta info without considering the upload time. If the reference URL is
     * not empty, it is checked that the actual URL contains the expected URL via substring matching.
     * @param expectedDataMetaInfo The expected data meta info.
     * @param actualDataMetaInfo The actual data meta info.
     */
    fun assertDataMetaInfoMatches(
        expectedDataMetaInfo: DataMetaInformation,
        actualDataMetaInfo: DataMetaInformation,
    ) {
        assertEquals(
            expectedDataMetaInfo.copy(url = "", uploadTime = 1), actualDataMetaInfo.copy(url = "", uploadTime = 1),
            "The meta info comparison without the reference failed.",
        )
        if (!expectedDataMetaInfo.url.isNullOrEmpty()) {
            assertTrue(
                actualDataMetaInfo.url!!.contains(expectedDataMetaInfo.url!!),
                "The reference is not as expected.",
            )
        }
    }

    /**
     * Builds a data meta information object with the given parameters. But setting uploadTime and reportingPeriod to dummy values.
     * @param dataId The data ID.
     * @param companyId The company ID.
     * @param testDataType The data type.
     * @param user The user who uploaded the data.
     * @return The data meta information object.
     */
    fun buildAcceptedAndActiveDataMetaInformation(
        dataId: String,
        companyId: String,
        testDataType: DataTypeEnum,
        user: TechnicalUser,
    ) = DataMetaInformation(
        dataId = dataId, companyId = companyId, dataType = testDataType, uploadTime = 1, reportingPeriod = "",
        currentlyActive = true, qaStatus = QaStatus.Accepted, uploaderUserId = user.technicalUserId,
        url = "companies/$companyId/frameworks/$testDataType/$dataId",
    )
}
