package org.dataland.datalandcompanyservice.services

import org.dataland.datalandcompanyservice.model.ProxySearchCompany

/**
 * Builds the user message of the proxy research from the data Dataland stores about the company.
 *
 * The GLEIF data is taken from Dataland (imported from the GLEIF golden copy by the batch manager), GLEIF itself is
 * not called. Dataland does not store the entity and registration status, the relationship type of the parent LEI
 * or reporting exceptions, so the message states that this information is not available.
 */
object ProxySearchUserMessage {
    private const val LEI_IDENTIFIER_TYPE = "Lei"

    /**
     * Builds the user message for the given company.
     */
    fun build(company: ProxySearchCompany): String =
        listOf(
            "Find the proxy company for the following company.",
            describeDatalandCompany(company),
            describeGleifData(company),
        ).joinToString("\n\n")

    private fun describeDatalandCompany(company: ProxySearchCompany): String {
        val lines = mutableListOf("Company on Dataland:")
        lines += "- Name: ${company.companyName} (Dataland company ID ${company.companyId})"
        if (company.alternativeNames.isNotEmpty()) {
            lines += "- Other names (one of them may be the exact GLEIF legal name): " +
                company.alternativeNames.joinToString("; ")
        }
        val headquarters = listOfNotNull(company.headquarters, company.countryCode).filter { it.isNotBlank() }
        if (headquarters.isNotEmpty()) lines += "- Headquarters: ${headquarters.joinToString(", ")}"
        return lines.joinToString("\n")
    }

    private fun describeGleifData(company: ProxySearchCompany): String {
        val lines = mutableListOf("GLEIF data (stored on Dataland, imported from the GLEIF golden copy):")
        val leis = company.identifiers[LEI_IDENTIFIER_TYPE].orEmpty()
        lines +=
            if (leis.isEmpty()) {
                "- No LEI is known on Dataland for this company."
            } else {
                "- Entity: ${company.companyName} (LEI ${leis.joinToString(", ")}). " +
                    "Entity status and registration status are not available."
            }
        lines += describeParent(company)
        return lines.joinToString("\n")
    }

    private fun describeParent(company: ProxySearchCompany): String {
        val parentLei =
            company.parentCompanyLei
                ?: return "- No parent LEI reported."
        val datalandParent =
            company.parentCompany?.let {
                "on Dataland as \"${it.companyName}\" (Dataland company ID ${it.companyId}; this is the Dataland name, " +
                    "which can differ from the exact GLEIF legal name)"
            } ?: "not on Dataland"
        return "- Parent LEI reported by GLEIF: $parentLei, $datalandParent. This is the ultimate accounting " +
            "consolidation parent if GLEIF reports one; otherwise it is the direct accounting consolidation parent, " +
            "fund manager, umbrella fund, head office of an international branch or master fund. The relationship " +
            "type and the parent's entity and registration status are not available."
    }
}
