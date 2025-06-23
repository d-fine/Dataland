package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.junit.jupiter.params.provider.Arguments
import java.util.UUID
import java.util.stream.Stream

class DataRequestUpdateManagerTestDataProvider {
    val dummyRequestChangeReason = "dummy reason"
    val dummyCompanyId = "dummyCompanyId"

    fun getDummyNonSourceableInfo() =
        SourceabilityInfo(
            companyId = dummyCompanyId,
            dataType = DataTypeEnum.nuclearMinusAndMinusGas,
            reportingPeriod = "dummyPeriod",
            isNonSourceable = true,
            reason = dummyRequestChangeReason,
        )

    fun getDummySourceableInfo() =
        SourceabilityInfo(
            companyId = "",
            dataType = DataTypeEnum.nuclearMinusAndMinusGas,
            reportingPeriod = "",
            isNonSourceable = false,
            reason = dummyRequestChangeReason,
        )

    fun getDataMetaInformation() =
        DataMetaInformation(
            dataId = UUID.randomUUID().toString(),
            companyId = dummyCompanyId,
            dataType = DataTypeEnum.nuclearMinusAndMinusGas,
            uploadTime = 0,
            reportingPeriod = "dummyPeriod",
            currentlyActive = false,
            qaStatus = QaStatus.Accepted,
            ref = "test",
        )

    fun getListOfBasicCompanyInformationForSubsidiaries() =
        listOf(
            BasicCompanyInformation(
                companyName = "dummyChildCompany1",
                companyId = "dummyChildCompanyId1",
                headquarters = "",
                countryCode = "",
            ),
            BasicCompanyInformation(
                companyName = "dummyChildCompany2",
                companyId = "dummyChildCompanyId2",
                headquarters = "",
                countryCode = "",
            ),
        )

    fun getStreamOfArgumentsToTestDataRequestUpdateUtils() =
        Stream.of(
            Arguments.of("lksg", RequestStatus.Open, RequestStatus.Answered),
            Arguments.of("vsme", RequestStatus.Open, RequestStatus.Answered),
            Arguments.of("nuclear-and-gas", RequestStatus.Open, RequestStatus.NonSourceable),
            Arguments.of("eu-taxonomy-financials", RequestStatus.Open, RequestStatus.Withdrawn),
            Arguments.of("sfdr", RequestStatus.Withdrawn, RequestStatus.Open),
        )

    fun getStreamOfArgumentsToTestFlagResetBehavior(): Stream<Arguments> {
        val argumentsList = mutableListOf<Arguments>()
        RequestStatus.entries.forEach { entry1 ->
            RequestStatus.entries.forEach { entry2 ->
                argumentsList.add(Arguments.of(entry1, entry2))
            }
        }
        return argumentsList.stream()
    }

    fun getDummyDataRequestEntities() =
        listOf(
            DataRequestEntity(
                userId = "4321", dataType = "nuclear-and-gas", notifyMeImmediately = true,
                reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                datalandCompanyId = dummyCompanyId,
            ),
            DataRequestEntity(
                userId = "1234", dataType = "nuclear-and-gas", notifyMeImmediately = false,
                reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                datalandCompanyId = dummyCompanyId,
            ),
        )

    fun getDummyDataRequestEntityWithdrawn(): DataRequestEntity {
        val dummyDataRequestEntityWithdrawn =
            getDummyDataRequestEntities().first().copy(
                dataRequestId = UUID.randomUUID().toString(),
                creationTimestamp = 0L,
            )
        dummyDataRequestEntityWithdrawn.addToDataRequestStatusHistory(
            RequestStatusEntity(
                StoredDataRequestStatusObject(
                    status = RequestStatus.Withdrawn,
                    creationTimestamp = 1L,
                    accessStatus = AccessStatus.Public,
                    requestStatusChangeReason = null,
                    answeringDataId = null,
                ),
                dummyDataRequestEntityWithdrawn,
            ),
        )
        return dummyDataRequestEntityWithdrawn
    }

    fun getDummyChildCompanyDataRequestEntities() =
        listOf(
            DataRequestEntity(
                userId = "1234", dataType = "nuclear-and-gas", notifyMeImmediately = true,
                reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                datalandCompanyId = "dummyChildCompanyId1",
            ),
            DataRequestEntity(
                userId = "1234", dataType = "nuclear-and-gas", notifyMeImmediately = false,
                reportingPeriod = "dummyPeriod", creationTimestamp = 0,
                datalandCompanyId = "dummyChildCompanyId2",
            ),
        )

    fun getDummyDataRequestEntity(dataType: String) =
        DataRequestEntity(
            userId = "",
            dataType = dataType,
            notifyMeImmediately = true,
            reportingPeriod = "",
            datalandCompanyId = "",
            creationTimestamp = 0L,
        )

    fun getDummyStoredDataRequestStatusObject(
        dataType: String,
        requestStatusBefore: RequestStatus,
    ) = StoredDataRequestStatusObject(
        status = requestStatusBefore,
        creationTimestamp = 1L,
        accessStatus = if (dataType == "vsme") AccessStatus.Granted else AccessStatus.Public,
        requestStatusChangeReason = null,
        answeringDataId = null,
    )
}
