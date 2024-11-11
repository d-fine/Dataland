package org.dataland.datalandemailservice.email

import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestClosed
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import java.util.*

object TypedEmailContentTestData {

    val companyId = UUID.randomUUID().toString()
    val companyName = "Banana Inc."
    val numberOfOpenDataRequestForCompany = 10

    val requesterEmail = "requester@example.com"
    val dataTypeA = "eutaxonomy-non-financials"
    val dataTypeLabelA = "EU Taxonomy for non-financial companies"
    val dataTypeB = "VSME"
    val dataTypeLabelB = "VSME"
    val reportingPeriodA = "2020"
    val reportingPeriodB = "2023"
    val reportinPeriodC = "2024"

    val message = "Some message"
    val firstName = "John"
    val lastName = "Doe"
    val subscriptionUuid = UUID.randomUUID().toString()
    val baseUrl = "test.dataland.com"

    val creationDate = "October 5th"
    val dataRequestId = UUID.randomUUID().toString()
    val numberOfDays = 23

    val datasetRequestedClaimOwnership = DatasetRequestedClaimOwnership(
        companyId, companyName, requesterEmail, dataTypeLabelA, listOf(reportingPeriodA, reportingPeriodB),
        message, firstName, lastName
    ).also {
        it.subscriptionUuid = subscriptionUuid
        it.baseUrl = baseUrl
    }

    val datasetRequestedClaimOwnershipKeywords = listOf(
        companyId, companyName, requesterEmail, dataTypeLabelA, reportingPeriodA,
        reportingPeriodB, message, firstName, lastName, subscriptionUuid, baseUrl
    )

    val dataRequestAnswered = DataRequestAnswered(
        companyId, companyName, dataTypeA, reportingPeriodA, creationDate, dataRequestId, numberOfDays, dataTypeLabelA
    ).also {
        it.baseUrl = baseUrl
    }

    val dataRequestAnsweredKeywords = listOf(
        companyId, companyName, dataTypeA, reportingPeriodA, creationDate, dataRequestId,
        numberOfDays.toString(), dataTypeLabelA, baseUrl
    )

    val dataRequestClosed = DataRequestClosed(
        companyId, companyName, dataTypeA, reportingPeriodA, creationDate, dataRequestId, numberOfDays, dataTypeLabelA
    ).also {
        it.baseUrl = baseUrl
    }

    val dataRequestClosedKeywords = listOf(
        companyId, companyName, dataTypeA, reportingPeriodA, creationDate, dataRequestId,
        numberOfDays.toString(), dataTypeLabelA, baseUrl
    )

    val companyOwnershipClaimApproved = CompanyOwnershipClaimApproved(
        companyId, companyName, numberOfOpenDataRequestForCompany
    ).also {
        it.baseUrl = baseUrl
    }

    val companyOwnershipClaimApprovedKeywords = listOf(
        companyId, companyName, numberOfOpenDataRequestForCompany.toString(), baseUrl
    )

    val accessToDatasetRequested = AccessToDatasetRequested(
        companyId, companyName, dataTypeLabelA, listOf(reportingPeriodA, reportingPeriodB),
        message, requesterEmail, firstName, lastName
    ).also {
        it.baseUrl = baseUrl
    }

    val accessToDatasetRequestedKeywords = listOf(
        companyId, companyName, dataTypeLabelA, reportingPeriodA, reportingPeriodB,
        message, requesterEmail, firstName, lastName, baseUrl
    )

    val accessToDatasetGranted = AccessToDatasetGranted(
        companyId, companyName, dataTypeA, dataTypeLabelA, reportingPeriodA, creationDate
    ).also {
        it.baseUrl = baseUrl
    }

    val accessToDatasetGrantedKeywords = listOf(
        companyId, companyName, dataTypeA, dataTypeLabelA, reportingPeriodA, creationDate, baseUrl
    )

    val singleDatasetUploadedEngagement = SingleDatasetUploadedEngagement(
        companyId, companyName, dataTypeLabelA, reportingPeriodA
    ).also {
        it.baseUrl = baseUrl
        it.subscriptionUuid = subscriptionUuid
    }

    val singleDatasetUploadedEngagementKeywords = listOf(
        companyId, companyName, dataTypeLabelA, reportingPeriodA, baseUrl, subscriptionUuid
    )

    val multipleDatasetsUploadedEngagement = MultipleDatasetsUploadedEngagement(
        companyId, companyName,
        listOf(
            MultipleDatasetsUploadedEngagement.FrameworkData(dataTypeLabelA, listOf(reportingPeriodA, reportingPeriodB)),
            MultipleDatasetsUploadedEngagement.FrameworkData(dataTypeLabelB, listOf(reportinPeriodC))
        ),
        numberOfDays.toLong(),
    ).also {
        it.baseUrl = baseUrl
        it.subscriptionUuid = subscriptionUuid
    }

    val multipleDatasetsUploadedEngagementKeywords = listOf(
        companyId, companyName, dataTypeLabelA, reportingPeriodA, reportingPeriodB, dataTypeLabelB, reportinPeriodC,
        numberOfDays.toString(), baseUrl, subscriptionUuid
    )

    val keyValueTable = KeyValueTable(
        "subject", "textTitle", "htmlTitle",
        listOf(
            "Key1" to Value.Text("Value1"),
            "Key2" to Value.RelativeLink("/example", "Example"),
            "Key3" to Value.List(Value.Text("ValueA"), Value.Text("ValueB"), Value.RelativeLink("/test", "Test")))
    ).also {
        it.baseUrl = baseUrl
    }

    val keyValueTableKeywords = emptyList<String>() // TODO

    val contentToKeywordsMap: List<Pair<TypedEmailContent, List<String>>> =
        listOf(
            datasetRequestedClaimOwnership to datasetRequestedClaimOwnershipKeywords,
            dataRequestAnswered to dataRequestAnsweredKeywords,
            dataRequestClosed to dataRequestClosedKeywords,
            companyOwnershipClaimApproved to companyOwnershipClaimApprovedKeywords,
            accessToDatasetRequested to accessToDatasetRequestedKeywords,
            accessToDatasetGranted to accessToDatasetGrantedKeywords,
            singleDatasetUploadedEngagement to singleDatasetUploadedEngagementKeywords,
            multipleDatasetsUploadedEngagement to multipleDatasetsUploadedEngagementKeywords,
            keyValueTable to keyValueTableKeywords
        )
}