package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.net.URI
import java.net.URL
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component


/**
 * Implementation of a lego manager for Dataland
 */
@Component("LegoManager")
class LegoManager() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataInMemoryStorage = mutableMapOf<String, String>()
    private val frameworkLocation = "http://localhost:8081/Frameworks"

    fun getJsonNodeFromString(json: String): JsonNode {
        return ObjectMapper().readTree(json)
    }

    fun getJsonSchemaFromUrl(uri: String): JsonSchema {
        val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
        return factory.getSchema(URI(uri))
    }

    fun getJsonNodeFromUrl(url: String?): JsonNode {
        return ObjectMapper().readTree(URL(url))
    }

    fun identifyLegosInSchema(jsonNode: JsonNode, relevantFields: MutableList<String>, fieldName: String) {
        if (jsonNode.has("\$ref")) {
            logger.info("Found relevant field $fieldName")
            val reference = jsonNode.get("\$ref").toString().trim('"')
            logger.info("Found reference $reference")
            val uniqueIdentifier = getJsonNodeFromUrl(reference).get("\$uniqueIdentifier")
            logger.info("Found identifier $uniqueIdentifier")
            relevantFields.add(fieldName)
        } else if (jsonNode.isObject) {
            val fields = jsonNode.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                identifyLegosInSchema(jsonField.value, relevantFields, jsonField.key)
            }
        }
    }

    fun dataInvalid(framework: String, data: JsonNode): Boolean {
        if (frameworkInvalid(framework)) {
            throw Exception("Framework $framework does not exist!")
        }

        logger.info("Get Schema and validate")
        val schema = getJsonSchemaFromUrl("$frameworkLocation/$framework.json")
        val errors = schema.validate(data)
        if (errors.isNotEmpty()) {
            errors.forEach { logger.info(it.toString()) }
            return true
        }
        return false
    }

    fun storeData(framework: String, reportingPeriod: String, companyId: String, data: String) {
        logger.info("Received data $data for storage.")
        val dataNode = getJsonNodeFromString(data)

        if (dataInvalid(framework, dataNode)) {
            throw Exception("Data failed validation!")
        }
        logger.info("Passed Validation!")

        logger.info("Identifying blocks.")
        val schemaProperties = getJsonNodeFromUrl("$frameworkLocation/$framework.json").get("properties")
        val relevantFields = mutableListOf<String>()
        identifyLegosInSchema(schemaProperties, relevantFields, "")

        logger.info("Storing blocks.")
        relevantFields.forEach {
            fieldName -> logger.info("Processing field name $fieldName")
            dataInMemoryStorage["$reportingPeriod:$companyId:$fieldName"] = dataNode.findValue(fieldName).toString()
        }
    }

    fun populateSchemaNodes(node: JsonNode, fieldName: String, replacementValue: JsonNode) {
        if (node.has(fieldName)) {
            (node as ObjectNode).set<JsonNode?>(fieldName, replacementValue)
        } else if (node.isObject) {
            val fields = node.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                populateSchemaNodes(jsonField.value, fieldName, replacementValue)
            }
        }
    }

    fun retrieveData(framework: String, reportingPeriod: String, companyId: String): String {
        if (frameworkInvalid(framework)) {
            throw Exception("Framework $framework does not exist!")
        }

        logger.info("Identifying blocks.")
        val schemaProperties = getJsonNodeFromUrl("$frameworkLocation/$framework.json").get("properties")
        val relevantFields = mutableListOf<String>()
        identifyLegosInSchema(schemaProperties, relevantFields, "")

        relevantFields.forEach {
            var replacementValue = getJsonNodeFromString("")
            val key = "$reportingPeriod:$companyId:$it"
            if (dataInMemoryStorage.containsKey(key)) {
                replacementValue = getJsonNodeFromString(dataInMemoryStorage[key]!!)
            }
            populateSchemaNodes(schemaProperties, it, replacementValue)
        }

        return schemaProperties.toPrettyString()
    }

    private fun frameworkInvalid(framework: String): Boolean {
        try {
            getJsonSchemaFromUrl("$frameworkLocation/$framework.json")
        } catch (exception: Exception) {
            logger.error("Unable to retrieve schema for framework $framework!")
            return true
        }
        return false
    }
}
