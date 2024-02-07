package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.repositories.utils.GetDataRequestsSearchFilter
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestManagerUtils
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a request manager service for all operations concerning the processing of single data requests
 */
@Service("SingleDataRequestManager")
class SingleDataRequestManager(
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Autowired private val dataRequestLogger: DataRequestLogger,
    @Autowired private val companyApi: CompanyDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
) {
    private val utils = DataRequestManagerUtils(dataRequestRepository, dataRequestLogger, companyApi, objectMapper)
    val companyIdRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\$")

    /**
     * Processes a single data request from a user
     * @param singleDataRequest info provided by a user in order to request a single dataset on Dataland
     * @return the stored data request object
     */
    @Transactional
    fun processSingleDataRequest(singleDataRequest: SingleDataRequest): List<StoredDataRequest> {
        lateinit var identifierTypeToStore: DataRequestCompanyIdentifierType
        lateinit var identifierValueToStore: String
        val storedDataRequests = mutableListOf<StoredDataRequest>()
        if (companyIdRegex.matches(singleDataRequest.companyIdentifier)) {
            checkIfCompanyIsValid(singleDataRequest.companyIdentifier)
            identifierTypeToStore = DataRequestCompanyIdentifierType.DatalandCompanyId
            identifierValueToStore = singleDataRequest.companyIdentifier
        } else {
            val matchedIdentifierType = utils.determineIdentifierTypeViaRegex(singleDataRequest.companyIdentifier)
            dataRequestLogger.logMessageForSingleDataRequest(singleDataRequest.companyIdentifier)
            if (matchedIdentifierType != null) {
                val datalandCompanyId = utils.getDatalandCompanyIdForIdentifierValue(
                    singleDataRequest.companyIdentifier,
                )
                identifierTypeToStore = datalandCompanyId?.let {
                    DataRequestCompanyIdentifierType.DatalandCompanyId
                } ?: matchedIdentifierType
                identifierValueToStore = datalandCompanyId ?: singleDataRequest.companyIdentifier
            } else {
                throwInvalidInputApiExceptionBecauseIdentifierWasRejected()
            }
        }
        throwInvalidInputApiExceptionIfFinalMessageObjectNotMeaningful(singleDataRequest)
        storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
            storedDataRequests, singleDataRequest, identifierValueToStore, identifierTypeToStore,
        )
        return storedDataRequests
    }

    private fun checkIfCompanyIsValid(companyId: String) {
        try {
            companyApi.getCompanyById(companyId)
        } catch (e: ClientException) {
            if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                throw ResourceNotFoundApiException(
                    "Company not found",
                    "Dataland-backend does not know the company ID $companyId",
                )
            }
        }
    }

    private fun throwInvalidInputApiExceptionBecauseIdentifierWasRejected() {
        val summary = "The provided company identifier has an invalid format."
        val message = "The company identifier you provided does not match the patterns " +
            "of a valid LEI, ISIN, PermId or Dataland CompanyID."
        throw InvalidInputApiException(
            summary,
            message,
        )
    }

    private fun throwInvalidInputApiExceptionIfFinalMessageObjectNotMeaningful(singleDataRequest: SingleDataRequest) {
        if (utils.isContactListTrivial(singleDataRequest.contactList) && !singleDataRequest.message.isNullOrBlank()) {
            throw InvalidInputApiException(
                "Insufficient information to create message object.",
                "Without at least one proper email address being provided no message can be forwarded.",
            )
        }
    }

    private fun storeDataRequestsAndAddThemToListForEachReportingPeriodIfNotAlreadyExisting(
        storedDataRequests: MutableList<StoredDataRequest>,
        singleDataRequest: SingleDataRequest,
        identifierValueToStore: String,
        identifierTypeToStore: DataRequestCompanyIdentifierType,
    ) {
        for (reportingPeriod in singleDataRequest.listOfReportingPeriods.distinct()) {
            storedDataRequests.add(
                utils.buildStoredDataRequestFromDataRequestEntity(
                    utils.storeDataRequestEntityIfNotExisting(
                        identifierValueToStore,
                        identifierTypeToStore,
                        singleDataRequest.frameworkName,
                        reportingPeriod,
                        singleDataRequest.contactList,
                        singleDataRequest.message,
                    ),
                ),
            )
        }
    }

    /**
     * Method to retrieve a data request by its ID
     * @param dataRequestId the ID of the data request to retrieve
     * @return the data request corresponding to the provided ID
     */
    @Transactional
    fun getDataRequestById(dataRequestId: String): StoredDataRequest {
        throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId)
        val dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return utils.buildStoredDataRequestFromDataRequestEntity(dataRequestEntity)
    }

    private fun throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId: String) {
        if (!dataRequestRepository.existsById(dataRequestId)) {
            throw ResourceNotFoundApiException(
                "Data request not found",
                "Dataland does not know the Data request ID $dataRequestId",
            )
        }
    }

    /**
     * Method to get all data requests based on filters.
     * @param dataType the framework to apply to the data request
     * @param requestStatus the status to apply to the data request
     * @param userId the user to apply to the data request
     * @param reportingPeriod the reporting period to apply to the data request
     * @param dataRequestCompanyIdentifierValue the company identifier value to apply to the data request
     * @return all filtered data requests
     */

    fun getDataRequests(
        dataType: DataTypeEnum?,
        userId: String?,
        requestStatus: RequestStatus?,
        reportingPeriod: String?,
        dataRequestCompanyIdentifierValue: String?,
    ): List<StoredDataRequest>? {
        val filter = GetDataRequestsSearchFilter(
            dataTypeNameFilter = dataType?.name ?: "",
            userIdFilter = userId ?: "",
            requestStatus = requestStatus,
            reportingPeriodFilter = reportingPeriod ?: "",
            dataRequestCompanyIdentifierValueFilter = dataRequestCompanyIdentifierValue ?: "",
        )
        val result = dataRequestRepository.searchDataRequestEntity(filter)

        return result.map { utils.buildStoredDataRequestFromDataRequestEntity(it) }
    }

    /**
     * Method to patch the status of a data request.
     * @param dataRequestId the id of the data request to patch
     * @param requestStatus the status to apply to the data request
     * @return the updated data request object
     */
    @Transactional
    fun patchDataRequest(dataRequestId: String, requestStatus: RequestStatus): StoredDataRequest {
        throwResourceNotFoundExceptionIfDataRequestIdUnknown(dataRequestId)
        var dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        dataRequestLogger.logMessageForPatchingRequestStatus(dataRequestEntity.dataRequestId, requestStatus)
        dataRequestEntity.requestStatus = requestStatus
        dataRequestRepository.save(dataRequestEntity)
        dataRequestEntity = dataRequestRepository.findById(dataRequestId).get()
        return utils.buildStoredDataRequestFromDataRequestEntity(dataRequestEntity)
    }

    /**
     * Method to send out a confirmation email to the requester as soon as the requested data is provided by the company
     * @param jsonString the message describing the result of the completed QA process
     * @param type the type of the message
     */

    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "dataQualityAssuredCommunityManagerDataManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.DataQualityAssured, declare = "false"),
                key = [RoutingKeyNames.data],
            ),
        ],
    )
    @Transactional
    fun sendAnsweredRequestConfirmationEmail(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.QaCompleted)
        val qaCompletedMessage = objectMapper.readValue(jsonString, QaCompletedMessage::class.java)
        val dataId = qaCompletedMessage.identifier
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        val metaData = metaDataControllerApi.getDataMetaInfo(dataId)
        val filter = GetDataRequestsSearchFilter(
            dataTypeNameFilter = metaData.dataType.name,
            userIdFilter = "",
            requestStatus = RequestStatus.Open,
            reportingPeriodFilter = metaData.reportingPeriod,
            dataRequestCompanyIdentifierValueFilter = metaData.companyId,
        )
        dataRequestRepository.searchDataRequestEntity(filter)
    }
}
