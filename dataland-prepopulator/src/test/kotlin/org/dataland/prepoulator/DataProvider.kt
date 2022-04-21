package org.dataland.prepoulator

import org.springframework.core.io.ClassPathResource

class DataProvider {
    private val dataFile = ClassPathResource("/Testiso.csv").file
    fun getFile(): String {
        return dataFile.path
    }

}