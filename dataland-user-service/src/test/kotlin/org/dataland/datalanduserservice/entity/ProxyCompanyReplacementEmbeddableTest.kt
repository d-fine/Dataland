package org.dataland.datalanduserservice.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class ProxyCompanyReplacementEmbeddableTest {
    private val proxyCompanyReplacementEmbeddable =
        ProxyCompanyReplacementEmbeddable(
            userId = "user-id",
            timestamp = 1L,
            proxiedCompanyId = "proxied-company-id",
            proxyCompanyId = "proxy-company-id",
        )

    @Test
    fun `test that ProxyCompanyReplacementEmbeddable fields are accessible`() {
        assertEquals("user-id", proxyCompanyReplacementEmbeddable.userId)
        assertEquals(1L, proxyCompanyReplacementEmbeddable.timestamp)
        assertEquals("proxied-company-id", proxyCompanyReplacementEmbeddable.proxiedCompanyId)
        assertEquals("proxy-company-id", proxyCompanyReplacementEmbeddable.proxyCompanyId)
    }

    @Test
    fun `test that ProxyCompanyReplacementEmbeddable equals, hashCode, toString and copy work as expected`() {
        val copy = proxyCompanyReplacementEmbeddable.copy()
        assertEquals(proxyCompanyReplacementEmbeddable, copy)
        assertEquals(proxyCompanyReplacementEmbeddable.hashCode(), copy.hashCode())
        assertEquals(proxyCompanyReplacementEmbeddable.toString(), copy.toString())

        val differentEmbeddable = proxyCompanyReplacementEmbeddable.copy(userId = "other-user-id")
        assertNotEquals(proxyCompanyReplacementEmbeddable, differentEmbeddable)
    }
}
