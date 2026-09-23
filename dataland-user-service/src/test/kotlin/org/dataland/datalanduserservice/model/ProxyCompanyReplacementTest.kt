package org.dataland.datalanduserservice.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ProxyCompanyReplacementTest {
    private val proxyCompanyReplacement =
        ProxyCompanyReplacement(
            userId = "user-id",
            timestamp = 1L,
            proxiedCompanyId = "proxied-company-id",
            proxyCompanyId = "proxy-company-id",
        )

    @Test
    fun `test that ProxyCompanyReplacement fields are accessible`() {
        assertEquals("user-id", proxyCompanyReplacement.userId)
        assertEquals(1L, proxyCompanyReplacement.timestamp)
        assertEquals("proxied-company-id", proxyCompanyReplacement.proxiedCompanyId)
        assertEquals("proxy-company-id", proxyCompanyReplacement.proxyCompanyId)
    }

    @Test
    fun `test that ProxyCompanyReplacement equals, hashCode, toString and copy work as expected`() {
        val copy = proxyCompanyReplacement.copy()
        assertEquals(proxyCompanyReplacement, copy)
        assertEquals(proxyCompanyReplacement.hashCode(), copy.hashCode())
        assertEquals(proxyCompanyReplacement.toString(), copy.toString())

        val differentReplacement = proxyCompanyReplacement.copy(userId = "other-user-id")
        assertNotEquals(proxyCompanyReplacement, differentReplacement)
    }
}
