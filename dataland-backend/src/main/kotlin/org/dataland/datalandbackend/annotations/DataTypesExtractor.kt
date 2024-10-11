package org.dataland.datalandbackend.annotations

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter

/**
 * Class to extract DataType annotations
 */
class DataTypesExtractor {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to list all the permissible data types
     * @return list of all permissible data types
     */
    fun getAllDataTypes(): List<String> {
        val provider = ClassPathScanningCandidateComponentProvider(false)
        provider.addIncludeFilter(AnnotationTypeFilter(DataType::class.java))
        val modelBeans = provider.findCandidateComponents("org.dataland.datalandbackend")
        val dataTypes =
            modelBeans
                .map { Class.forName(it.beanClassName).getAnnotation(DataType::class.java) }
                .sortedBy { it.order }
                .map { it.name }

        logger.info("Searching for known Datatypes. Datatypes found: $dataTypes")
        return dataTypes
    }
}
