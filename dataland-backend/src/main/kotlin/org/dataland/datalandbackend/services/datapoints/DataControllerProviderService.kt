package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.controller.DataController
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataExportStore
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
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
        @Qualifier("AssembledExportService") private val dataExportService: DataExportService<Any>,
        private val dataExportStore: DataExportStore,
        private val storedDataManager: DataManager,
        private val assembledDataManager: AssembledDataManager,
        private val metaDataManager: DataMetaInformationManager,
        private val companyQueryManager: CompanyQueryManager,
    ) {
        private val dataTypeClassCache = ConcurrentHashMap<DataType, Class<out Any>>()

        private fun getDataController(
            dataTypeClass: Class<out Any>,
            dataManager: DatasetStorageService,
        ): DataController<Any> =
            DataController(
                dataManager,
                metaDataManager,
                dataExportService,
                dataExportStore,
                companyQueryManager,
                dataTypeClass,
            )

        /**
         * Get a data controller for a framework using a stored data manager
         */
        fun getStoredDataControllerForFramework(dataType: DataType): DataController<Any> {
            val dataTypeClass = getClassForDataType(dataType)
            return getDataController(dataTypeClass, storedDataManager)
        }

        /**
         * Get a data controller for a framework using an assembled data manager
         */
        fun getAssembledDataControllerForFramework(dataType: DataType): DataController<Any> {
            val dataTypeClass = getClassForDataType(dataType)
            return getDataController(dataTypeClass, assembledDataManager)
        }

        /**
         * Get the class for a data type
         */
        fun getClassForDataType(dataType: DataType): Class<out Any> {
            val valueFromCache = dataTypeClassCache[dataType]
            if (valueFromCache != null) {
                return valueFromCache
            }
            val provider = ClassPathScanningCandidateComponentProvider(false)
            provider.addIncludeFilter(AnnotationTypeFilter(org.dataland.datalandbackend.annotations.DataType::class.java))
            val modelBeans = provider.findCandidateComponents("org.dataland.datalandbackend")
            val matchingClass =
                modelBeans
                    .map { Class.forName(it.beanClassName).asSubclass(Any::class.java) }
                    .firstOrNull { it.getAnnotation(org.dataland.datalandbackend.annotations.DataType::class.java).name == dataType.name }
                    ?: throw IllegalArgumentException("No class found for data type ${dataType.name}")
            dataTypeClassCache[dataType] = matchingClass
            return matchingClass
        }
    }
