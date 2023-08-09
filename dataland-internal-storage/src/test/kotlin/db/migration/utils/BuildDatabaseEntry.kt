package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.json.JSONObject

fun buildDatabaseEntry(dataset: JSONObject, dataType: DataTypeEnum): String {
    val objectMapper = ObjectMapper()
    val dataBaseEntry = JSONObject()
    dataBaseEntry.put("dataType", dataType.value)
    dataBaseEntry.put("data", dataset.toString())
    return objectMapper.writeValueAsString(dataBaseEntry.toString())
}