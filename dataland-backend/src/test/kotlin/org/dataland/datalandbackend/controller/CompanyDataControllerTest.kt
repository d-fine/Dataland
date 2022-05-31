package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.TestDataProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {

    val dataProvider = TestDataProvider(objectMapper)
    val testCompanyInformation = dataProvider.getCompanyInformation(1).last()
    val token =
        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI0bDR3RVU1dDN0VnRmdHlRYk8zNXBzSlEzaFNVZkd4YU5acUxydFZYdU5VIn0.eyJleHAiOjE2NTQwMTE3NjQsImlhdCI6MTY1NDAxMTE2NCwianRpIjoiMDlkYWI2MGQtYTZlMy00NmFiLTljNTAtNjQ3ZTNjY2UyNjIzIiwiaXNzIjoiaHR0cDovL2tleWNsb2FrOjgwODAvcmVhbG1zL2RhdGFsYW5kc2VjdXJpdHkiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiOTBmYjYzMzUtNmQzZC00ZjBhLTgzMWQtNTFlMDMxYTVhZTY2IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZGF0YWxhbmQtcHVibGljIiwic2Vzc2lvbl9zdGF0ZSI6IjExYTYwOWRjLTI0YzctNGM4NC05YzYyLTAxOWJlMjI4ZWQzOSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1kYXRhbGFuZHNlY3VyaXR5IiwiUk9MRV9VU0VSIiwib2ZmbGluZV9hY2Nlc3MiLCJST0xFX0FETUlOIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJzaWQiOiIxMWE2MDlkYy0yNGM3LTRjODQtOWM2Mi0wMTliZTIyOGVkMzkiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiYWRtaW5fdXNlciJ9.OvYMzKor7M7SJLXlAqgD0lrnKdgPoRDNaKQnYxye6ih6V5_VvDvEiSbLwXPuQPVXE5MZLE4LINhCIyZUVhnzrOJa1SUG9MMVGx2F-tmuymUDplH2f8lqPuimrn8XLfKA-uNNUeLEy_-NLs3su0Mh_4LVLHSccC3NUv15yqEv7VMGzegMKSqlaR9QkxFsboHz0ZXMGbwD1hjbMrDf8JaNWjV7XVY_TMIbyGVkW0sMoi_nSJQmKFheKj2EFk_JXu-rQhVPltCrh-EBH31qQcUEvw52ZSzLvP44VxGo39YaTVL-GheDe7DMnx9DIVMeVcgOnP8o4jtMfXuRb1EUZF7Wtg"

    @Test
    fun `company can be posted`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation, token)
    }

    @Test
    fun `meta info about a specific company can be retrieved by its company Id`() {
        val storedCompany = CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation, token)
        mockMvc.perform(
            get("/companies/${storedCompany.companyId}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON)
            )
    }
}
