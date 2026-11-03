package org.dataland.datalandproxyfinder.services

import org.dataland.datalandproxyfinder.model.DatalandCompanyCandidate
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

/**
 * Tool functions that the LLM can call to resolve companies to Dataland company IDs.
 */
class DatalandCompanyTools(
    private val datalandCompanyService: DatalandCompanyService,
) {
    companion object {
        const val MAX_SEARCH_RESULTS = 10
    }

    /**
     * Searches Dataland companies by (part of) their name.
     */
    @Tool(description = "Searches companies on Dataland by name and returns matching Dataland company IDs and names.")
    fun searchCompaniesByName(
        @ToolParam(description = "The company name or a part of it (at least 3 characters).") name: String,
    ): List<DatalandCompanyCandidate> = datalandCompanyService.searchCompaniesByName(name, MAX_SEARCH_RESULTS)

    /**
     * Looks up a Dataland company by an identifier.
     */
    @Tool(description = "Looks up a company on Dataland by an identifier and returns it, or null if it is unknown.")
    fun findCompanyByIdentifier(
        @ToolParam(description = "The identifier type, e.g. Lei, Isin, PermId, Ticker, Duns, CompanyRegistrationNumber.")
        identifierType: String,
        @ToolParam(description = "The identifier value.") identifier: String,
    ): DatalandCompanyCandidate? = datalandCompanyService.findCompanyByIdentifier(identifierType, identifier)
}
