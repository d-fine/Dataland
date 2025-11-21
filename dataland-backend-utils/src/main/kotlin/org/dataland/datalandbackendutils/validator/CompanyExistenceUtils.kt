package org.dataland.datalandbackendutils.validator

import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import java.io.IOException

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
