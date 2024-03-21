package org.dataland.documentmanager.services.conversion

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.commands.scan.result.ScanResult
import java.io.InputStream

fun mockClamAvClient(): ClamavClient {
    return mock(ClamavClient::class.java).also {
        `when`(it.scan(any() as InputStream)).thenReturn(ScanResult.OK)
    }
}
