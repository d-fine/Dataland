package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ExceptionForwarder
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * A service class for accessing the Dataland backend with error handling
 */
@Service
class DatalandBackendAccessor(
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val exceptionForwarder: ExceptionForwarder,
) {
    /**
     * Ensures that a data set with the given id exists and is of the expected type.
     */
    fun ensureDatalandDataExists(
        dataId: String,
        dataType: String,
    ) {
        try {
            val dataMetaInfo = metaDataControllerApi.getDataMetaInfo(dataId)

            if (dataMetaInfo.dataType.value != dataType) {
                throw InvalidInputApiException(
                    "Data type mismatch",
                    "The requested data set '$dataId' is of type '${dataMetaInfo.dataType}'," +
                        " but the expected type is '$dataType'.",
                )
            }
        } catch (ex: ClientException) {
            val exceptionToThrow =
                if (ex.statusCode == HttpStatus.NOT_FOUND.value()) {
                    ResourceNotFoundApiException(
                        "Dataset '$dataId' not found",
                        "No data set with the id: $dataId could be found.",
                        ex,
                    )
                } else {
                    ex
                }
            throw exceptionToThrow
        }
    }

    /**
     * Calls backend to return companyIds for companyName
     */
    fun getCompanyIdsForCompanyName(companyName: String?): Set<String> {
        var companyIds = emptySet<String>()
        if (!companyName.isNullOrBlank()) {
            try {
                companyIds =
                    companyDataControllerApi.getCompaniesBySearchString(companyName).map { it.companyId }.toSet()
            } catch (clientException: ClientException) {
                val responseBody = (clientException.response as ClientError<*>).body.toString()
                exceptionForwarder.catchSearchStringTooShortClientException(
                    responseBody,
                    clientException.statusCode,
                    clientException,
                )
                throw clientException
            }
        }
        return companyIds
    }
}
