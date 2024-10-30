package org.dataland.datalandspecificationservice.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandspecification.database.SpecificationDatabase
import org.dataland.datalandspecification.database.fs.FileSystemSpecificationDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File

/**
 * Configuration for the specification database.
 */
@Configuration
class SpecificationDatabaseConfiguration {
    /**
     * Get the specification database based on this applications resources.
     */
    @Bean
    fun getSpecificationDatabase(
        @Autowired objectMapper: ObjectMapper,
        @Value("\${dataland.specification-folder}") specificationFolder: String,
    ): SpecificationDatabase =
        FileSystemSpecificationDatabase(
            baseFolder = File(specificationFolder),
            objectMapper = objectMapper,
        )
}
