package org.dataland.documentmanager.services.conversion

import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult

fun mockClamAvClient(): ClamavClient {
    return mock(ClamavClient::class.java).also {
        `when`(it.scan(any() ?: "TEST".encodeToByteArray().inputStream())).thenReturn(ScanResult.OK)
    }
}
