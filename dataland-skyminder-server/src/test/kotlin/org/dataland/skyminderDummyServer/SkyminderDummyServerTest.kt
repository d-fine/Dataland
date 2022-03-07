package org.dataland.skyminderDummyServer

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite.SuiteClasses
import org.powermock.core.classloader.annotations.PrepareForTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

/*
Note: some of the following annotations are redundant.
just to check coverage
 */
@RunWith(SpringRunner::class)
@SuiteClasses(DummySkyminder::class)
@SpringBootTest
@AutoConfigureMockMvc
class SkyminderDummyServerTest(@Autowired var mockMvc: MockMvc) {
    @Test
    @PrepareForTest(DummySkyminder::class)
    fun `check that the dummy server is available`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @PrepareForTest(DummySkyminder::class)
    fun `check top level main function`() {
        val context = main(emptyArray())
        assertNotNull(context)
    }
}
