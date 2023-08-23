package db.migration.utils

import org.json.JSONObject

/**
 * If there is a value under the specified key, return it. Otherwise, return JsonObject.NULL
 */
fun JSONObject.getOrJsonNull(key: String): Any {
    return this.opt(key) ?: JSONObject.NULL
}

/**
 * If there is a value under the specified key (!= JsonObject.NULL), return it. Otherwise, return null
 */
fun JSONObject.getOrJavaNull(key: String): Any? {
    val jsonObject = this.opt(key)
    return if (jsonObject == JSONObject.NULL) {
        null
    } else {
        jsonObject
    }
}
