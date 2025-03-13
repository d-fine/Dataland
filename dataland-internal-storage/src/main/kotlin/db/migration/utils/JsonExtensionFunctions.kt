package db.migration.utils

import org.json.JSONObject

/**
 * If there is a value under the specified key, return it. Otherwise, return JsonObject.NULL
 */
fun JSONObject.getOrJsonNull(key: String): Any = this.opt(key) ?: JSONObject.NULL

/**
 * If there is a value under the specified key (!= JsonObject.NULL), return it. Otherwise, return null
 */
fun JSONObject.getOrJavaNull(key: String): Any? {
    val anyObject = this.opt(key)
    return if (anyObject == JSONObject.NULL) {
        null
    } else {
        anyObject
    }
}

/**
 * Returns a nested JSON-field using a path specifier.
 * @param keyPath the path specifying the field (e.g. "social/socialAndEmployeeMatters/rateOfAccidents").
 *                Hierarchies are expected to be separated by '/'.
 */
fun JSONObject.getFromPath(keyPath: String): Any? =
    keyPath
        .split('/')
        .dropLast(1)
        .fold(this, { jsonObj: JSONObject?, pathSegment: String ->
            jsonObj?.optJSONObject(pathSegment)
        })
        ?.getOrJavaNull(keyPath.substringAfterLast('/'))
