package org.dataland.prepoulator

import org.springframework.core.io.ClassPathResource
import java.io.File

class DataProvider {
    private val dataFile = ClassPathResource("/Testiso.csv").file
    //private val outFile = ClassPathResource("/Output.json").file
    fun getFile(): String {
        return dataFile.path
    }
    //fun getOutFile(): File {
    //    return outFile
    //}
}
