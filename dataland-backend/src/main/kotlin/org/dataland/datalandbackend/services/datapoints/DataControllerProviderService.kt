package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.controller.DataController
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides data controllers for all framework data types
 */
@Service
class DataControllerProviderService
    @Autowired
    constructor(
        private val dataExportService: DataExportService,
        private val storedDataManager: DataManager,
        private val assembledDataManager: AssembledDataManager,
        private val metaDataManager: DataMetaInformationManager,
        private val objectMapper: ObjectMapper,
    ) {
        private val dataTypeClassCache = ConcurrentHashMap<DataType, Class<*>>()

        /**
         * Get a data controller for a framework using a stored data manager
         */
        fun getStoredDataControllerForFramework(dataType: DataType): DataController<Any> {
            val dataTypeClass = getClassForDataType(dataType)
            return DataController(
                storedDataManager,
                metaDataManager,
                dataExportService,
                objectMapper,
                dataTypeClass as Class<Any>,
            )
        }

        /**
         * Get a data controller for a framework using an assembled data manager
         */
        fun getAssembledDataControllerForFramework(dataType: DataType): DataController<Any> {
            val dataTypeClass = getClassForDataType(dataType)
            return DataController(
                assembledDataManager,
                metaDataManager,
                dataExportService,
                objectMapper,
                dataTypeClass as Class<Any>,
            )
        }

        /**
         * Get the class for a data type
         */
        fun getClassForDataType(dataType: DataType): Class<*> {
            val valueFromCache = dataTypeClassCache[dataType]
            if (valueFromCache != null) {
                return valueFromCache
            }
            val provider = ClassPathScanningCandidateComponentProvider(false)
            provider.addIncludeFilter(AnnotationTypeFilter(org.dataland.datalandbackend.annotations.DataType::class.java))
            val modelBeans = provider.findCandidateComponents("org.dataland.datalandbackend")
            val matchingClass =
                modelBeans
                    .map { Class.forName(it.beanClassName) }
                    .firstOrNull { it.getAnnotation(org.dataland.datalandbackend.annotations.DataType::class.java).name == dataType.name }
                    ?: throw IllegalArgumentException("No class found for data type ${dataType.name}")
            dataTypeClassCache[dataType] = matchingClass
            return matchingClass
        }
    }
