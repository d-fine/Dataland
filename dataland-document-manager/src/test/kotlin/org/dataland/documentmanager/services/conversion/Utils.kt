package org.dataland.documentmanager.services.conversion

import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream

fun mockClamAvClient(): ClamavClient {
    fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
    return mock(ClamavClient::class.java).also {
        `when`(it.scan(any(InputStream::class.java))).thenReturn(ScanResult.OK)
    }
}
