package org.dataland.frameworktoolbox.template

import org.dataland.frameworktoolbox.utils.LoggerDelegate
import java.io.File

/**
 * Extract a single sheet form a xlsx file to a csv file using libreoffice
 */
class ExcelToCsvConverter(
    private val inputExcelFile: File,
    private val sheetName: String,
    private val targetCsvFile: File,
) {
    private val conversionUtilsDirectory = File("./dataland-framework-toolbox/excel-to-csv/")
    private val logger by LoggerDelegate()

    /**
     * Performs the XLSX to CSV conversion
     */
    fun convert() {
        val imageHash = buildConversionDockerContainerAndCaptureSha()
        useDockerContainerToConvertXlsx(imageHash)
    }

    private fun useDockerContainerToConvertXlsx(imageHash: String) {
        logger.info("Converting XLSX file using docker container")
        val dockerProcess =
            ProcessBuilder(
                "docker", "run", "--rm", "-v", "${inputExcelFile.absoluteFile.canonicalPath}:/mount/excel.xlsx:ro",
                imageHash, "excel-$sheetName.csv",
            ).redirectOutput(targetCsvFile)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        dockerProcess.waitFor()
        require(dockerProcess.exitValue() == 0) { "Docker process should terminate successfully." }
    }

    private fun buildConversionDockerContainerAndCaptureSha(): String {
        logger.info("Building docker container for a platform-independent XLSX -> CSV conversion")
        val buildProcess =
            ProcessBuilder("docker", "build", "-q", ".")
                .directory(conversionUtilsDirectory)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        buildProcess.waitFor()
        require(buildProcess.exitValue() == 0) { "Build process should terminate successfully." }

        val stdout = buildProcess.inputStream.bufferedReader().readText()
        require(stdout.startsWith("sha256:")) {
            "Build process should have returned the build hash. Instead got: $stdout"
        }
        logger.info("CSV-Convert image hash: $stdout")
        return stdout.trim()
    }
}
