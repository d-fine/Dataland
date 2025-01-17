package org.dataland.datalanddataexporter.services

import ApiRetryException
import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.SfdrDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
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
import org.dataland.datalanddataexporter.utils.TransformationUtils.checkConsistencyOfDataAndTransformationRules
import org.dataland.datalanddataexporter.utils.TransformationUtils.checkConsistencyOfLegacyRulesAndTransformationRules
import org.dataland.datalanddataexporter.utils.TransformationUtils.convertDataToJson
import org.dataland.datalanddataexporter.utils.TransformationUtils.getCurrentAndLegacyHeaders
import org.dataland.datalanddataexporter.utils.TransformationUtils.getLeiToIsinMapping
import org.dataland.datalanddataexporter.utils.TransformationUtils.mapJsonToCsv
import org.dataland.datalanddataexporter.utils.TransformationUtils.mapJsonToLegacyCsv
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.net.SocketTimeoutException

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

    companion object {
        const val MAX_RETRIES = 3
    }

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
        val legacyRules = readTransformationConfig("./transformationRules/SfdrLegacyCsvExportFields.config")
        val headers = getCurrentAndLegacyHeaders(transformationRules, legacyRules)
        val dataIds = getAllSfdrDataIds()

        dataIds.forEach { dataId ->
            try {
                val (deltaCsvData, deltaIsinData) = getSfdrDataForSingleDataId(dataId, transformationRules, legacyRules)
                csvData.add(deltaCsvData)
                isinData.addAll(deltaIsinData)
            } catch (e: ApiRetryException) {
                logger.error("Common API error occurred for data ID: $dataId. Error: ${e.message}. Skipping this ID.")
            } catch (e: IllegalArgumentException) {
                logger.error("IllegalArgumentException for data ID: $dataId. Error: ${e.message}. Skipping this ID.")
            }
        }

        writeCsvFiles(outputDirectory, csvData, isinData, headers)
    }

    /**
     * Gets the SFDR data for a single dataId and its associated LEI to ISIN mapping
     * @return A list of SFDR data IDs
     */
    fun getSfdrDataForSingleDataId(
        dataId: String,
        transformationRules: Map<String, String>,
        legacyRules: Map<String, String>,
    ): Pair<MutableMap<String, String>, List<Map<String, String>>> {
        logger.info("Exporting data with ID: $dataId")

        val csvData = mutableMapOf<String, String>()

        val companyAssociatedData = retryOnCommonApiErrors { sfdrDataControllerApi.getCompanyAssociatedSfdrData(dataId) }
        val companyData = retryOnCommonApiErrors { companyDataControllerApi.getCompanyById(companyAssociatedData.companyId) }

        val data = convertDataToJson(companyAssociatedData)

        validateConsistency(data, transformationRules, legacyRules, dataId)

        val isinData = getLeiToIsinMapping(companyData.companyInformation)
        csvData += mapJsonToCsv(data, transformationRules)
        csvData += mapJsonToLegacyCsv(data, legacyRules)
        csvData += getCompanyRelatedData(companyAssociatedData, companyData)

        return Pair(csvData, isinData)
    }

    /**
     * Checks the consistency of the JSON data with the transformation rules.
     * @param data The JSON node
     * @param transformationRules The transformation rules
     * @param legacyRules The transformation rules
     * @param dataId The dataId
     */
    private fun validateConsistency(
        data: JsonNode,
        transformationRules: Map<String, String>,
        legacyRules: Map<String, String>,
        dataId: String,
    ) {
        try {
            checkConsistencyOfDataAndTransformationRules(data, transformationRules)
            checkConsistencyOfLegacyRulesAndTransformationRules(transformationRules, legacyRules)
        } catch (exception: IllegalArgumentException) {
            logger.error("Validate consistency failed for data ID: $dataId.")
            throw IllegalArgumentException("Consistency check failed for data with ID $dataId: ${exception.message}", exception)
        }
    }

    /**
     * Retry multiples times for common API errors
     */
    private fun <T> retryOnCommonApiErrors(functionToExecute: () -> T): T {
        var counter = 0
        while (counter < MAX_RETRIES) {
            try {
                return functionToExecute()
            } catch (exception: ClientException) {
                logger.error("Unexpected client exception occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: SocketTimeoutException) {
                logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: ServerException) {
                logger.error("Unexpected server exception. Response was: ${exception.message}.")
                counter++
            }
        }
        logger.error("Maximum number of retries exceeded.")
        throw ApiRetryException("Operation failed after $MAX_RETRIES attempts due to common API errors.")
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
