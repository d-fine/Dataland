package org.dataland.datalandproxyfinder.services

import org.dataland.datalandproxyfinder.model.DatalandCompanyCandidate
import org.dataland.datalandproxyfinder.model.ProxySearchCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProxySearchUserMessageTest {
    private val company =
        ProxySearchCompany(
            companyId = "child-id",
            companyName = "Child GmbH",
            alternativeNames = listOf("Child Legal Name GmbH", "Child Trading"),
            identifiers = mapOf("Lei" to listOf("CHILDLEI"), "Isin" to listOf("DE0000000001")),
            countryCode = "DE",
            headquarters = "Berlin",
        )

    private fun lines(company: ProxySearchCompany) = ProxySearchUserMessage.build(company).lines()

    @Test
    fun `the message contains the Dataland data and the GLEIF entity without status`() {
        val lines = lines(company)

        assertEquals("Find the proxy company for the following company.", lines.first())
        assertTrue("- Name: Child GmbH (Dataland company ID child-id)" in lines)
        assertTrue(lines.any { it.startsWith("- Other names") && it.endsWith("Child Legal Name GmbH; Child Trading") })
        assertTrue("- Headquarters: Berlin, DE" in lines)
        assertTrue(
            "- Entity: Child GmbH (LEI CHILDLEI). Entity status and registration status are not available." in lines,
        )
        assertFalse(lines.any { it.contains("DE0000000001") })
    }

    @Test
    fun `a parent LEI on Dataland is described with its Dataland company and the relationship caveat`() {
        val parentLine =
            lines(
                company.copy(
                    parentCompanyLei = "PARENTLEI",
                    parentCompany = DatalandCompanyCandidate("parent-id", "Parent AG"),
                ),
            ).single { it.startsWith("- Parent LEI") }

        assertTrue(parentLine.startsWith("- Parent LEI reported by GLEIF: PARENTLEI, on Dataland as \"Parent AG\""))
        assertTrue(parentLine.contains("Dataland company ID parent-id"))
        assertTrue(parentLine.contains("ultimate accounting consolidation parent if GLEIF reports one"))
        assertTrue(parentLine.contains("fund manager"))
    }

    @Test
    fun `a parent LEI that is not on Dataland is marked as such`() {
        val parentLine = lines(company.copy(parentCompanyLei = "PARENTLEI")).single { it.startsWith("- Parent LEI") }
        assertTrue(parentLine.startsWith("- Parent LEI reported by GLEIF: PARENTLEI, not on Dataland."))
    }

    @Test
    fun `missing LEI and parent are stated explicitly`() {
        val lines =
            lines(ProxySearchCompany(companyId = "child-id", companyName = "Child GmbH"))

        assertTrue("- No LEI is known on Dataland for this company." in lines)
        assertTrue(lines.any { it.startsWith("- No parent LEI reported.") })
        assertFalse(lines.any { it.startsWith("- Other names") || it.startsWith("- Headquarters") })
    }
}
