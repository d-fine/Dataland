package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples

/**
 * --- API model ---
 * Class for defining the meta data of a company
 * @param companyId identifies the company
 * @param companyInformation contains information of company
 * @param dataRegisteredByDataland contains meta info for all datasets of this company
 */
data class StoredCompany(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    val companyInformation: CompanyInformation,
    @field:JsonProperty(required = true)
    val dataRegisteredByDataland: List<DataMetaInformation>,
)
