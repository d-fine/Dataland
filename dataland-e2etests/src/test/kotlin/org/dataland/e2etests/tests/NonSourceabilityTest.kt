package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceabilityRequest
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.testDataProviders.awaitUntilAsserted
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaServiceQaStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NonSourceabilityTest {
    private val apiAccessor = ApiAccessor()

    private val testReportingPeriod = "2026"

    @Test
    fun `POST metadata nonSourceable followed by GET returns the correct triple`() {
        val companyId =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
            }

        GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
                NonSourceabilityRequest(
                    companyId = companyId,
                    dataType = DataTypeEnum.sfdr,
                    reportingPeriod = testReportingPeriod,
                    reason = "No public source available",
                    bypassQa = true,
                ),
            )
        }

        val results =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                    companyId = companyId,
                    dataType = DataTypeEnum.sfdr,
                    reportingPeriod = testReportingPeriod,
                )
            }

        assertTrue(results.isNotEmpty(), "Expected at least one non-sourceability entry")
        val entry = results.first()
        assertEquals(companyId, entry.companyId)
        assertEquals(DataTypeEnum.sfdr, entry.dataType)
        assertEquals(testReportingPeriod, entry.reportingPeriod)
    }

    @Test
    fun `POST nonSourceable with bypassQa false triggers full QA lifecycle and currentlyActive becomes true after acceptance`() {
        val companyId =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
            }

        val nonSourceabilityId = postNonSourceableAndAssertPending(companyId)
        assertBackendEntryIsPending(companyId)
        assertQaReviewRowAppears(companyId)
        acceptInQaServiceAndAssert(nonSourceabilityId)
        assertBackendEntryIsAcceptedAndActive(companyId)
    }

    private fun postNonSourceableAndAssertPending(companyId: String): String {
        val createdEntry =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
                    NonSourceabilityRequest(
                        companyId = companyId,
                        dataType = DataTypeEnum.sfdr,
                        reportingPeriod = testReportingPeriod,
                        reason = "No public source available",
                        bypassQa = false,
                    ),
                )
            }
        assertEquals(BackendQaStatus.Pending, createdEntry.qaStatus, "Entry must be Pending after POST with bypassQa=false")
        assertFalse(createdEntry.currentlyActive, "Entry must be inactive until QA accepts it")
        return createdEntry.nonSourceabilityId
    }

    private fun assertBackendEntryIsPending(companyId: String) {
        val entries =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                    companyId = companyId,
                    dataType = DataTypeEnum.sfdr,
                    reportingPeriod = testReportingPeriod,
                )
            }
        assertEquals(1, entries.size, "Expected exactly one entry for the posted triple")
        val entry = entries.first()
        assertEquals(companyId, entry.companyId)
        assertEquals(DataTypeEnum.sfdr, entry.dataType)
        assertEquals(testReportingPeriod, entry.reportingPeriod)
        assertEquals(BackendQaStatus.Pending, entry.qaStatus)
        assertFalse(entry.currentlyActive)
    }

    private fun assertQaReviewRowAppears(companyId: String) {
        awaitUntilAsserted {
            val qaReviews =
                GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                    apiAccessor.nonSourceabilityQaControllerApi.getNonSourceableReviews(
                        companyId = companyId,
                        dataType = DataTypeEnum.sfdr.value,
                        reportingPeriod = testReportingPeriod,
                    )
                }
            assertTrue(qaReviews.isNotEmpty(), "QA review row must appear after backend emits non-sourceability-created event")
            assertEquals(QaServiceQaStatus.Pending, qaReviews.first().qaStatus)
        }
    }

    private fun acceptInQaServiceAndAssert(nonSourceabilityId: String) {
        val qaDecision =
            GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                apiAccessor.nonSourceabilityQaControllerApi.postNonSourceabilityDecision(
                    nonSourceabilityId = nonSourceabilityId,
                    qaStatus = QaServiceQaStatus.Accepted,
                )
            }
        assertEquals(QaServiceQaStatus.Accepted, qaDecision.qaStatus, "POST decision response must reflect Accepted status")
    }

    private fun assertBackendEntryIsAcceptedAndActive(companyId: String) {
        awaitUntilAsserted {
            val entries =
                GlobalAuth.withTechnicalUser(TechnicalUser.Admin) {
                    apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                        companyId = companyId,
                        dataType = DataTypeEnum.sfdr,
                        reportingPeriod = testReportingPeriod,
                    )
                }
            assertEquals(1, entries.size)
            val entry = entries.first()
            assertEquals(BackendQaStatus.Accepted, entry.qaStatus, "Backend entry must be Accepted after QA acceptance event")
            assertTrue(entry.currentlyActive, "currentlyActive must be true after QA acceptance")
        }
    }
}
