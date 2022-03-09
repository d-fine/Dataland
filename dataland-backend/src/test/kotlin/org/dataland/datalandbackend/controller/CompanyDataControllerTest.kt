package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.StoredDataSet
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {

    val storedDataSets = listOf(
        StoredDataSet(name = "Company A", payload = "Data"),
        StoredDataSet(name = "Holding B", payload = "Information"),
        StoredDataSet(name = "Group C", payload = "Inputs")
    )

    fun uploadDataSet(mockMvc: MockMvc, storedDataSet: StoredDataSet) {
        mockMvc.perform(
            post("/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(storedDataSet))
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `data can be added`() {
        uploadDataSet(mockMvc, storedDataSets[0])
    }

    @Test
    fun `data can be retrieved`() {
        val testSet = storedDataSets[0]
        uploadDataSet(mockMvc, testSet)

        mockMvc.perform(
            get("/data/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.name").value(testSet.name))
            .andExpect(jsonPath("\$.payload").value(testSet.payload))
    }

    @Test
    fun `list the data`() {
        for (dataset in storedDataSets)
            uploadDataSet(mockMvc, dataset)
        mockMvc.perform(
            get("/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }
}
