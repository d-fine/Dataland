package db.migration.utils

import org.json.JSONObject
import kotlin.io.path.div

object JsonUtils {
    /**
     * Loads a json object from a json file in the resources path corresponding the migration script
     * @param version the version string prefix of the migrations class name
     * @param filename the name of the json file to load
     */
    fun readJsonFromResourcesFile(filename: String): JSONObject {
        val jsonPath = javaClass.getResource("/db/migration/$filename")!!.readText()
        return JSONObject(jsonPath)
    }
}
