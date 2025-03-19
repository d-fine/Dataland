package org.dataland.e2etests.utils.testDataProviders

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import java.math.BigDecimal
import java.time.LocalDate

object BigDecimalAdapter {
    @FromJson
    fun fromJson(string: String) = BigDecimal(string)

    @ToJson
    fun toJson(value: BigDecimal) = value.toString()
}

object LocalDateAdapter {
    @FromJson
    fun fromJson(string: String) = LocalDate.parse(string)

    @ToJson
    fun toJson(value: LocalDate) = value.toString()
}

data class CompanyInformationWithT<T>(
    @Json var companyInformation: CompanyInformation,
    @Json var t: T,
)
