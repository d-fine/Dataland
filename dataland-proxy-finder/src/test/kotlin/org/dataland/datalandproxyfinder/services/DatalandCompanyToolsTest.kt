package org.dataland.datalandproxyfinder.services

import org.dataland.datalandproxyfinder.model.DatalandCompanyCandidate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DatalandCompanyToolsTest {
    private val service = mock<DatalandCompanyService>()
    private val tools = DatalandCompanyTools(service)
    private val candidate = DatalandCompanyCandidate("id-1", "Parent AG")

    @Test
    fun `search by name delegates with the result limit`() {
        whenever(service.searchCompaniesByName("Parent", DatalandCompanyTools.MAX_SEARCH_RESULTS))
            .thenReturn(listOf(candidate))
        assertEquals(listOf(candidate), tools.searchCompaniesByName("Parent"))
        verify(service).searchCompaniesByName("Parent", DatalandCompanyTools.MAX_SEARCH_RESULTS)
    }

    @Test
    fun `identifier lookup returns the company or null`() {
        whenever(service.findCompanyByIdentifier("Lei", "123")).thenReturn(candidate)
        assertEquals(candidate, tools.findCompanyByIdentifier("Lei", "123"))
        assertNull(tools.findCompanyByIdentifier("Lei", "unknown"))
    }
}
