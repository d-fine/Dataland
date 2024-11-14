package org.dataland.datalandspecification.database.fs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.specifications.DataPointSpecification
import org.dataland.datalandspecification.specifications.DataPointTypeSpecification
import org.dataland.datalandspecification.specifications.FrameworkSpecification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

private inline fun <reified T> loadSpecifications(
    folder: File,
    objectMapper: ObjectMapper,
    logger: Logger,
): MutableMap<String, T> {
    val specifications = mutableMapOf<String, T>()
    folder.listFiles()?.forEach { file ->
        try {
            val specification = objectMapper.readValue<T>(file)
            specifications[file.nameWithoutExtension] = specification
        } catch (e: IOException) {
            logger.error("Failed to load ${T::class.simpleName} specification from file: ${file.name}", e)
            throw e
        }
    }
    return specifications
}

private inline fun <reified T> saveSpecifications(
    folder: File,
    objectMapper: ObjectMapper,
    specifications: Map<String, T>,
) {
    specifications.forEach { (id, specification) ->
        val file = File(folder, "$id.json")
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, specification)
    }
}

/**
 * A specification database that loads specifications from the file system.
 */
class FileSystemSpecificationDatabase(
    private val baseFolder: File,
    private val objectMapper: ObjectMapper,
) : SpecificationDatabase() {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        loadSpecifications<DataPointTypeSpecification>(File(baseFolder, "dataPointTypes"), objectMapper, logger)
            .forEach { (id, specification) ->
                dataPointTypeSpecifications[id] = specification
            }
        logger.info("Loaded ${dataPointTypeSpecifications.size} data point type specifications")
        loadSpecifications<DataPointSpecification>(File(baseFolder, "dataPoints"), objectMapper, logger)
            .forEach { (id, specification) ->
                dataPointSpecifications[id] = specification
            }
        logger.info("Loaded ${dataPointSpecifications.size} data point specifications")
        loadSpecifications<FrameworkSpecification>(File(baseFolder, "frameworks"), objectMapper, logger)
            .forEach { (id, specification) ->
                frameworkSpecifications[id] = specification
            }
        logger.info("Loaded ${frameworkSpecifications.size} framework specifications")
        validateIntegrity()
    }

    private fun validateIntegrity() {
        frameworkSpecifications.forEach {
            it.value.validateIntegrity(this)
            assert(it.value.id == it.key) { "Framework ID does not match key: ${it.key}" }
        }
        dataPointTypeSpecifications.forEach {
            it.value.validateIntegrity()
            assert(it.value.id == it.key) { "Data point type ID does not match key: ${it.key}" }
        }
        dataPointSpecifications.forEach {
            it.value.validateIntegrity(this)
            assert(it.value.id == it.key) { "Data point ID does not match key: ${it.key}" }
        }
    }

    /**
     * Save the specifications to the file system.
     */
    fun saveToDisk() {
        validateIntegrity()
        saveSpecifications(File(baseFolder, "dataPointTypes"), objectMapper, dataPointTypeSpecifications)
        saveSpecifications(File(baseFolder, "dataPoints"), objectMapper, dataPointSpecifications)
        saveSpecifications(File(baseFolder, "frameworks"), objectMapper, frameworkSpecifications)
    }
}
