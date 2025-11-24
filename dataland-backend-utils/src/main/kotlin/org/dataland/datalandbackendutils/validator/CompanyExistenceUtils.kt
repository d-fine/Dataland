package org.dataland.datalandbackendutils.validator

import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import java.io.IOException

/**
 * Makes a HEAD request to the company data API to verify if a company with the specified ID exists.
 *
 * @param backendBaseUrl The base URL of the backend server hosting the company data API.
 * @param authenticatedOkHttpClient An authenticated OkHttpClient used to make the HTTP request.
 * @param companyId The unique identifier of the company to be checked.
 * @param logger A logger instance used to log information and warnings related to the request.
 * @return True if the company exists (determined by a successful HTTP response), false otherwise.
 */
fun callCompanyDataApiAndCheckCompanyId(
    backendBaseUrl: String,
    authenticatedOkHttpClient: OkHttpClient,
    companyId: String,
    logger: Logger,
): Boolean {
    val request =
        Request
            .Builder()
            .url("$backendBaseUrl/companies/$companyId")
            .head()
            .build()
    return try {
        authenticatedOkHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                true
            } else {
                logger.info("Company with id $companyId not found: Status code ${response.code}")
                false
            }
        }
    } catch (exception: IOException) {
        logger.warn("Error validating company existence: ${exception.message}")
        false
    }
}
