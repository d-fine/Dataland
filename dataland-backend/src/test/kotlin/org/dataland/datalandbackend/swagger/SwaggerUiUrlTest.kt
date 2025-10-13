package org.dataland.datalandbackend.swagger

import org.dataland.datalandbackend.DatalandBackend
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class SwaggerUiUrlTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `send a get request to the correct Swagger UI URL and check that it works`() {
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk)
    }

    @Test
    fun `send a get request to a Swagger UI URL with typo and check that it does not work`() {
        mockMvc.perform(get("/swagger-ui/inndex.html")).andExpect(status().isNotFound)
    }
}
