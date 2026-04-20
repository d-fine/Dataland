package org.dataland.e2etests.tests

import org.dataland.dataSourcingService.openApiClient.model.DataSourcingState
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.SingleRequest
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

    private companion object {
        private const val NO_PUBLIC_SOURCE_REASON = "No public source available"
    }

    private data class Ctx(
        val companyId: String,
        val dataType: DataTypeEnum,
        val reportingPeriod: String,
        val dataSourcingId: String? = null,
    )

    private fun <T> asAdmin(block: () -> T): T = GlobalAuth.withTechnicalUser(TechnicalUser.Admin) { block() }

    @Test
    fun `post metadata nonSourceable followed by get returns the correct triple`() {
        val companyId = asAdmin { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId }

        asAdmin {
            apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
                NonSourceabilityRequest(
                    companyId = companyId,
                    dataType = DataTypeEnum.sfdr,
                    reportingPeriod = testReportingPeriod,
                    reason = NO_PUBLIC_SOURCE_REASON,
                    bypassQa = true,
                    currentlyActive = true,
                ),
            )
        }

        val results =
            asAdmin {
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
    fun `post nonSourceable with bypassQa false triggers full qa lifecycle and currentlyActive becomes true after acceptance`() {
        var ctx =
            Ctx(
                companyId = asAdmin { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId },
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = testReportingPeriod,
            )
        ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))

        val nonSourceabilityId = postNonSourceableAndAssertPending(ctx)
        assertBackendEntryIsPending(ctx)
        assertQaReviewRowAppears(ctx)
        assertDsStateIsNonSourceableVerification(ctx)
        postQaDecision(nonSourceabilityId, QaServiceQaStatus.Accepted)
        assertQaReviewIsAccepted(ctx)
        assertBackendEntryIsAcceptedAndActive(ctx)
        assertDsStateIsNonSourceable(ctx)
    }

    @Test
    fun `post nonSourceable with bypassQa false and qa Rejected keeps backend inactive and DS state unchanged`() {
        var ctx =
            Ctx(
                companyId = asAdmin { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId },
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = testReportingPeriod,
            )
        ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))

        val nonSourceabilityId = postNonSourceableAndAssertPending(ctx)
        assertBackendEntryIsPending(ctx)
        assertQaReviewRowAppears(ctx)
        assertDsStateIsNonSourceableVerification(ctx)
        postQaDecision(nonSourceabilityId, QaServiceQaStatus.Rejected)
        assertQaReviewIsRejected(ctx)
        assertBackendEntryIsRejectedAndInactive(ctx)
        assertDsStateIsUnchanged(ctx, DataSourcingState.NonSourceableVerification)
    }

    @Test
    fun `post nonSourceable with bypassQa true immediately accepts entry and transitions ds to NonSourceable`() {
        var ctx =
            Ctx(
                companyId = asAdmin { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId },
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = testReportingPeriod,
            )
        ctx = ctx.copy(dataSourcingId = initializeDataSourcing(ctx.companyId))

        postNonSourceableWithBypassQa(ctx)
        assertNoQaReviewRowExists(ctx)
        assertBackendEntryIsAcceptedAndActive(ctx)
        assertDsStateIsNonSourceable(ctx)
    }

    @Test
    fun `currentlyActive becomes false after QA approves a dataset for the same triple`() {
        val ctx =
            Ctx(
                companyId = asAdmin { apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId },
                dataType = DataTypeEnum.sfdr,
                reportingPeriod = testReportingPeriod,
            )

        postNonSourceableWithBypassQa(ctx)
        assertBackendEntryIsAcceptedAndActive(ctx)

        val dataId = uploadDatasetForTriple(ctx)
        asAdmin { apiAccessor.qaServiceControllerApi.changeQaStatus(dataId, QaServiceQaStatus.Accepted) }

        assertNonSourceabilityIsInactive(ctx)
    }

    private fun postNonSourceableAndAssertPending(ctx: Ctx): String {
        val createdEntry =
            asAdmin {
                apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
                    NonSourceabilityRequest(
                        companyId = ctx.companyId,
                        dataType = ctx.dataType,
                        reportingPeriod = ctx.reportingPeriod,
                        reason = NO_PUBLIC_SOURCE_REASON,
                        bypassQa = false,
                        currentlyActive = false,
                    ),
                )
            }
        assertEquals(
            BackendQaStatus.Pending,
            createdEntry.qaStatus,
            "Entry must be Pending after POST with bypassQa=false",
        )
        assertFalse(createdEntry.currentlyActive, "Entry must be inactive until QA accepts it")
        return createdEntry.nonSourceabilityId
    }

    private fun assertBackendEntryIsPending(ctx: Ctx) {
        val entries =
            asAdmin {
                apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                    companyId = ctx.companyId,
                    dataType = ctx.dataType,
                    reportingPeriod = ctx.reportingPeriod,
                )
            }
        assertEquals(1, entries.size, "Expected exactly one entry for the posted triple")
        assertEquals(BackendQaStatus.Pending, entries.first().qaStatus)
        assertFalse(entries.first().currentlyActive)
    }

    private fun assertQaReviewRowAppears(ctx: Ctx) {
        awaitUntilAsserted {
            val qaReviews =
                asAdmin {
                    apiAccessor.nonSourceabilityQaControllerApi.getNonSourceableReviews(
                        companyId = ctx.companyId,
                        dataType = ctx.dataType.value,
                        reportingPeriod = ctx.reportingPeriod,
                    )
                }
            assertTrue(
                qaReviews.isNotEmpty(),
                "QA review row must appear after backend emits non-sourceability-created event",
            )
            assertEquals(QaServiceQaStatus.Pending, qaReviews.first().qaStatus)
        }
    }

    private fun postQaDecision(
        nonSourceabilityId: String,
        qaStatus: QaServiceQaStatus,
    ) {
        val qaDecision =
            asAdmin {
                apiAccessor.nonSourceabilityQaControllerApi.postNonSourceabilityDecision(
                    nonSourceabilityId,
                    org.dataland.datalandqaservice.openApiClient.model.NonSourceabilityDecisionRequest(
                        qaStatus = qaStatus,
                        qaComment = null,
                    ),
                )
            }
        assertEquals(qaStatus, qaDecision.qaStatus, "post decision response must reflect requested status")
    }

    private fun assertQaReviewIsAccepted(ctx: Ctx) {
        val qaReviews =
            asAdmin {
                apiAccessor.nonSourceabilityQaControllerApi.getNonSourceableReviews(
                    companyId = ctx.companyId,
                    dataType = ctx.dataType.value,
                    reportingPeriod = ctx.reportingPeriod,
                )
            }
        assertEquals(
            QaServiceQaStatus.Accepted,
            qaReviews.first().qaStatus,
            "QA service must persist Accepted status after decision",
        )
    }

    private fun assertBackendEntryIsAcceptedAndActive(ctx: Ctx) {
        awaitUntilAsserted {
            val entries =
                asAdmin {
                    apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                        companyId = ctx.companyId,
                        dataType = ctx.dataType,
                        reportingPeriod = ctx.reportingPeriod,
                    )
                }
            assertEquals(1, entries.size)
            val entry = entries.first()
            assertEquals(
                BackendQaStatus.Accepted,
                entry.qaStatus,
                "Backend entry must be Accepted after QA acceptance event",
            )
            assertTrue(entry.currentlyActive, "currentlyActive must be true after QA acceptance")
        }
    }

    private fun initializeDataSourcing(companyId: String): String {
        val requestId =
            asAdmin {
                apiAccessor.dataSourcingRequestControllerApi
                    .createRequest(
                        SingleRequest(
                            companyIdentifier = companyId,
                            dataType = DataTypeEnum.sfdr.value,
                            reportingPeriod = testReportingPeriod,
                            memberComment = null,
                        ),
                    ).requestId
            }
        val storedRequest =
            asAdmin {
                apiAccessor.dataSourcingRequestControllerApi.patchRequestState(requestId, RequestState.Processing)
            }
        return storedRequest.dataSourcingEntityId!!
    }

    private fun assertDsStateIsNonSourceableVerification(ctx: Ctx) {
        awaitUntilAsserted {
            val ds = asAdmin { apiAccessor.dataSourcingControllerApi.getDataSourcingById(ctx.dataSourcingId!!) }
            assertEquals(
                DataSourcingState.NonSourceableVerification,
                ds.state,
                "DS state must be NonSourceableVerification after non-sourceability posted",
            )
        }
    }

    private fun assertDsStateIsNonSourceable(ctx: Ctx) {
        awaitUntilAsserted {
            val ds = asAdmin { apiAccessor.dataSourcingControllerApi.getDataSourcingById(ctx.dataSourcingId!!) }
            assertEquals(
                DataSourcingState.NonSourceable,
                ds.state,
                "DS state must be NonSourceable after QA acceptance",
            )
        }
    }

    private fun assertQaReviewIsRejected(ctx: Ctx) {
        val qaReviews =
            asAdmin {
                apiAccessor.nonSourceabilityQaControllerApi.getNonSourceableReviews(
                    companyId = ctx.companyId,
                    dataType = ctx.dataType.value,
                    reportingPeriod = ctx.reportingPeriod,
                )
            }
        assertEquals(
            QaServiceQaStatus.Rejected,
            qaReviews.first().qaStatus,
            "QA service must persist Rejected status after decision",
        )
    }

    private fun assertBackendEntryIsRejectedAndInactive(ctx: Ctx) {
        awaitUntilAsserted {
            val entries =
                asAdmin {
                    apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                        companyId = ctx.companyId,
                        dataType = ctx.dataType,
                        reportingPeriod = ctx.reportingPeriod,
                    )
                }
            assertEquals(1, entries.size)
            val entry = entries.first()
            assertEquals(
                BackendQaStatus.Rejected,
                entry.qaStatus,
                "Backend entry must be Rejected after QA rejection event",
            )
            assertFalse(entry.currentlyActive, "currentlyActive must be false after QA rejection")
        }
    }

    private fun assertDsStateIsUnchanged(
        ctx: Ctx,
        expected: DataSourcingState,
    ) {
        val ds = asAdmin { apiAccessor.dataSourcingControllerApi.getDataSourcingById(ctx.dataSourcingId!!) }
        assertEquals(expected, ds.state, "DS state must remain $expected after QA rejection")
    }

    private fun postNonSourceableWithBypassQa(ctx: Ctx) {
        val createdEntry =
            asAdmin {
                apiAccessor.metaDataControllerApi.postNonSourceabilityOfADataset(
                    NonSourceabilityRequest(
                        companyId = ctx.companyId,
                        dataType = ctx.dataType,
                        reportingPeriod = ctx.reportingPeriod,
                        reason = NO_PUBLIC_SOURCE_REASON,
                        bypassQa = true,
                        currentlyActive = true,
                    ),
                )
            }
        assertEquals(
            BackendQaStatus.Accepted,
            createdEntry.qaStatus,
            "Entry must be immediately Accepted when bypassQa=true",
        )
        assertTrue(createdEntry.currentlyActive, "Entry must be immediately active when bypassQa=true")
    }

    private fun assertNoQaReviewRowExists(ctx: Ctx) {
        val qaReviews =
            asAdmin {
                apiAccessor.nonSourceabilityQaControllerApi.getNonSourceableReviews(
                    companyId = ctx.companyId,
                    dataType = ctx.dataType.value,
                    reportingPeriod = ctx.reportingPeriod,
                )
            }
        assertTrue(qaReviews.isEmpty(), "QA service must have no review rows when bypassQa=true")
    }

    private fun uploadDatasetForTriple(ctx: Ctx): String =
        asAdmin {
            apiAccessor
                .uploadDummyFrameworkDataset(
                    companyId = ctx.companyId,
                    dataType = ctx.dataType,
                    reportingPeriod = ctx.reportingPeriod,
                    bypassQa = false,
                ).dataId
        }

    private fun assertNonSourceabilityIsInactive(ctx: Ctx) {
        awaitUntilAsserted {
            val entries =
                asAdmin {
                    apiAccessor.metaDataControllerApi.getInfoOnNonSourceabilityOfDatasets(
                        companyId = ctx.companyId,
                        dataType = ctx.dataType,
                        reportingPeriod = ctx.reportingPeriod,
                    )
                }
            assertFalse(
                entries.first().currentlyActive,
                "currentlyActive must be false after QA accepts a dataset for the same triple",
            )
        }
    }
}
