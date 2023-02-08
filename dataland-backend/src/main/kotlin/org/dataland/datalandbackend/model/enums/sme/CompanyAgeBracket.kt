package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the age brackets of a company in the SME framework
 */
@Schema(
    enumAsRef = true,
)
enum class CompanyAgeBracket {
    LessThanOneYear, OneToFiveYears, FiveToTenYears, MoreThanTenYears
}
