package org.dataland.datalanddataexporter.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanddataexporter.utils.FileHandlingUtils.createDirectories
import org.dataland.datalanddataexporter.utils.FileHandlingUtils.getTimestamp
import org.dataland.datalanddataexporter.utils.FileHandlingUtils.readTransformationConfig
import org.dataland.datalanddataexporter.utils.FileHandlingUtils.writeCsv
import org.dataland.datalanddataexporter.utils.TransformationUtils.COMPANY_ID_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.COMPANY_NAME_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.ISIN_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.LEI_IDENTIFIER
import org.dataland.datalanddataexporter.utils.TransformationUtils.REPORTING_PERIOD_HEADER
import org.dataland.datalanddataexporter.utils.TransformationUtils.checkConsistency
import org.dataland.datalanddataexporter.utils.TransformationUtils.convertDataToJson
import org.dataland.datalanddataexporter.utils.TransformationUtils.getHeaders
import org.dataland.datalanddataexporter.utils.TransformationUtils.getLeiToIsinMapping
import org.dataland.datalanddataexporter.utils.TransformationUtils.mapJsonToCsv
import org.dataland.datalanddataexporter.utils.TransformationUtils.mapJsonToLegacyCsvFields
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File

/**
 * A class for handling the transformation of JSON files into CSV
 */
@Component("CsvExporter")
class CsvExporter(
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val sfdrDataControllerApi: SfdrDataControllerApi,
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 1 * * *")
    private fun triggerExport() {
        val outputDirectory = "/var/export/csv/sql_server"
        exportSfdrData(outputDirectory)
    }

    /**
     * Exports all SFDR data and the associated LEI to ISIN mapping to a CSV file.
     */
    fun exportSfdrData(outputDirectory: String) {
        logger.info("Starting the export of SFDR data.")
        val csvData = mutableListOf<Map<String, String>>()
        val isinData = mutableListOf<Map<String, String>>()

        createDirectories(outputDirectory)
        val transformationRules = readTransformationConfig("./transformationRules/SfdrSqlServer.config")
        val headers = getHeaders(transformationRules)
        val dataIds = getAllSfdrDataIds()

        val legacyFields = readTransformationConfig("./transformationRules/SfdrLegacyCsvExportFields.config")

        dataIds.forEach { dataId ->
            logger.info("Exporting data with ID: $dataId")

            val dataToExport = mutableMapOf<String, String>()
            val companyAssociatedData = sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId)
            val data = convertDataToJson(companyAssociatedData)
            val companyData = companyDataControllerApi.getCompanyById(companyAssociatedData.companyId)

            try {
                checkConsistency(data, transformationRules, legacyFields)
            } catch (exception: IllegalArgumentException) {
                logger.error("Consistency check failed for data with ID $dataId and exception ${exception.message}.")
                logger.warn("Skipping data with ID: $dataId")
                return@forEach
            }

            isinData.addAll(getLeiToIsinMapping(companyData.companyInformation))
            dataToExport += mapJsonToCsv(data, transformationRules)
            dataToExport += getCompanyRelatedData(companyAssociatedData, companyData)
            dataToExport += mapJsonToLegacyCsvFields(data, legacyFields)
            csvData.add(dataToExport)
        }
        println("dataToExport")
        print(csvData)
        writeCsvFiles(outputDirectory, csvData, isinData, headers)
    }

    /**
     * Gets all SFDR data IDs from the metadata endpoint in the backend.
     * @return A list of SFDR data IDs
     */
    fun getAllSfdrDataIds(): List<String> {
        val dataIds = mutableListOf<String>()
        val metaData = metaDataControllerApi.getListOfDataMetaInfo(dataType = DataTypeEnum.sfdr)
        metaData.forEach { dataIds.add(it.dataId) }
        return dataIds
    }

    /**
     * Gets the company-related data (LEI, reporting period, company ID, ...) for the CSV export.
     * @param companyAssociatedData The company associated data
     * @param companyData The company data
     * @return A map of company-related data
     */
    private fun getCompanyRelatedData(
        companyAssociatedData: CompanyAssociatedDataSfdrData,
        companyData: StoredCompany,
    ): Map<String, String> {
        val extractedData = mutableMapOf<String, String>()
        extractedData[REPORTING_PERIOD_HEADER] = companyAssociatedData.reportingPeriod
        extractedData[COMPANY_ID_HEADER] = companyAssociatedData.companyId
        extractedData[COMPANY_NAME_HEADER] = companyData.companyInformation.companyName
        val leiEntry = companyData.companyInformation.identifiers[LEI_IDENTIFIER] ?: emptyList()
        extractedData[LEI_HEADER] = if (leiEntry.isEmpty()) "" else leiEntry[0]
        return extractedData
    }

    /**
     * Writes the CSV files for ISINs and SFDR data to the output directory.
     * @param outputDirectory The output directory
     * @param csvData The SFDR CSV data
     * @param isinData The LEI to ISIN mapping data
     * @param headers The headers of the SFDR CSV data
     */
    fun writeCsvFiles(
        outputDirectory: String,
        csvData: List<Map<String, String>>,
        isinData: List<Map<String, String>>,
        headers: List<String>,
    ) {
        logger.info("Writing results to CSV files.")
        val timestamp = getTimestamp()
        val dataOutputFile = File("$outputDirectory/SfdrData_$timestamp.csv")
        val isinOutputFile = File("$outputDirectory/SfdrIsin_$timestamp.csv")
        writeCsv(csvData, dataOutputFile, headers)
        writeCsv(isinData, isinOutputFile, listOf(LEI_HEADER, ISIN_HEADER))
    }
}
