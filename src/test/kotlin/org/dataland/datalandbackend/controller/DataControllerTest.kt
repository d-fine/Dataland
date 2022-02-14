package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.DataSet
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
internal class DataControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    fun uploadDataSet(mockMvc: MockMvc, dataSet: DataSet) {
        mockMvc.perform(
            post("/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(dataSet))
        )
            .andExpectAll(status().isOk, content().contentType("application/json"))
    }

    @Test
    fun data_can_be_added() {
        uploadDataSet(mockMvc, dataSets[0])
    }

    @Test
    fun data_can_be_retrieved() {
        val testSet = dataSets[0]
        uploadDataSet(mockMvc, testSet)

        mockMvc.perform(
            get("/data/0")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType("application/json"))
            .andExpect(jsonPath("\$.name").value(testSet.name))
            .andExpect(jsonPath("\$.payload").value(testSet.payload))
    }

    @Test
    fun list_data() {
        for (dataset in dataSets)
            uploadDataSet(mockMvc, dataset)
        mockMvc.perform(
            get("/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType("application/json"))
    }

    /* val dataStore = DataStoreInterface()
     val dataController = DataController(dataStore)

     @Test
     fun check_healthiness(){
         Assertions.assertEquals("Healthy", dataController.getData().body)
     }*/

    companion object {
        val dataSets = listOf<DataSet>(
            DataSet(name = "Company A", payload = "Data"),
            DataSet(name = "Holding B", payload = "Information"),
            DataSet(name = "Group C", payload = "Inputs")
        )
    }
}
