package org.dataland.datalandbackend.annotations

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.RegexPatternTypeFilter
import java.util.regex.Pattern


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
        provider.addIncludeFilter(RegexPatternTypeFilter(Pattern.compile(".*")))
        val modelBeans = provider.findCandidateComponents("org.dataland.datalandbackend.model")
        val dataTypes = modelBeans.map{Class.forName(it.beanClassName)}
            .filter { it.isAnnotationPresent(DataType::class.java) }
            .map{it.simpleName}
        logger.info("Searching for known Datatypes. Datatypes found: $dataTypes")
        return dataTypes
    }
}
