package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.DataPointId
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.model.DocumentMetaInfoEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

/**
 * Utility class for enhancing data points by enriching them with metadata from referenced documents.
 *
 * This service interacts with the `DocumentControllerApi` to fetch metadata for document references
 * and updates the JSON content of data points with additional information like document name and publication date.
 *
 * This class is designed to process both stored and calculated data points, allowing for batch processing of
 * document metadata to optimize interactions with the document manager.
 */
@Service
class DataDeliveryServiceUtils(
    @Qualifier("getDocumentControllerApi")
    private val documentControllerApi: DocumentControllerApi,
) {
    private val referencedReportsUtilities = ReferencedReportsUtilities()
    private val objectMapper = JsonUtils.defaultObjectMapper

    /**
     * Stored and calculated data points after their referenced document metadata has been added to their JSON content.
     */
    data class EnhancedDataPoints(
        val allStoredDataPoints: Map<DataPointId, UploadedDataPoint>,
        val calculatedData: Map<BasicDatasetDimensions, List<UploadedDataPoint>>,
    )

    /**
     * Enhances every referenced document in stored and calculated data points with its file name and publication date.
     *
     * All file references are collected before making one batch request to the document manager. The input maps remain
     * unchanged; this method returns copies of their data points with enriched JSON content.
     *
     * @param allStoredDataPoints stored data points indexed by their IDs
     * @param calculatedData calculated data points grouped by dataset dimensions
     * @return stored and calculated data points with referenced document metadata added to their JSON content
     */
    fun enhanceDataPoints(
        allStoredDataPoints: Map<DataPointId, UploadedDataPoint>,
        calculatedData: Map<BasicDatasetDimensions, List<UploadedDataPoint>>,
    ): EnhancedDataPoints {
        val documentMetaInfo = resolveReferenceIds(getRequiredReferencedIds(allStoredDataPoints, calculatedData))

        return EnhancedDataPoints(
            allStoredDataPoints =
                allStoredDataPoints.mapValues { (_, dataPoint) ->
                    enhanceDataPoint(dataPoint, documentMetaInfo)
                },
            calculatedData =
                calculatedData.mapValues { (_, dataPoints) ->
                    dataPoints.map { dataPoint -> enhanceDataPoint(dataPoint, documentMetaInfo) }
                },
        )
    }

    /**
     * Collects distinct file references from the data sources in both stored and calculated data points.
     *
     * @param allStoredDataPoints stored data points indexed by their IDs
     * @param calculatedData calculated data points grouped by dataset dimensions
     * @return distinct referenced document IDs
     */
    private fun getRequiredReferencedIds(
        allStoredDataPoints: Map<DataPointId, UploadedDataPoint>,
        calculatedData: Map<BasicDatasetDimensions, List<UploadedDataPoint>>,
    ): List<String> {
        val allDataPoints = allStoredDataPoints.values + calculatedData.values.flatten()

        return allDataPoints
            .flatMap { uploadedDataPoint ->
                val companyReports = mutableListOf<CompanyReport>()
                referencedReportsUtilities.getAllCompanyReportsFromDataSource(
                    uploadedDataPoint.dataPoint,
                    companyReports,
                )
                companyReports.map { it.fileReference }
            }.distinct()
    }

    /**
     * Retrieves metadata for the supplied document IDs from the document manager in a single batch request.
     *
     * @param referenceIds referenced document IDs to retrieve
     * @return document metadata indexed by document ID
     */
    private fun resolveReferenceIds(referenceIds: List<String>): Map<String, DocumentMetaInfoEntity> {
        if (referenceIds.isEmpty()) {
            return emptyMap()
        }

        return documentControllerApi.getDocumentMetaInformationBatch(referenceIds)
    }

    /**
     * Returns a copy of [dataPoint] whose matching data sources contain the document name and publication date.
     *
     * @param dataPoint the data point to enrich
     * @param documentMetaInfo document metadata indexed by document ID
     * @return a copy of [dataPoint] with matching data sources enriched
     */
    private fun enhanceDataPoint(
        dataPoint: UploadedDataPoint,
        documentMetaInfo: Map<String, DocumentMetaInfoEntity>,
    ): UploadedDataPoint {
        if (documentMetaInfo.isEmpty()) return dataPoint

        val dataPointJson = objectMapper.readTree(dataPoint.dataPoint)
        enhanceDataSources(dataPointJson, documentMetaInfo)
        return dataPoint.copy(dataPoint = objectMapper.writeValueAsString(dataPointJson))
    }

    /**
     * Recursively traverses a JSON tree and enriches each `dataSource` object whose `fileReference` occurs in
     * [documentMetaInfo].
     *
     * @param jsonNode the current JSON node to inspect and update in place
     * @param documentMetaInfo document metadata indexed by document ID
     */
    private fun enhanceDataSources(
        jsonNode: JsonNode,
        documentMetaInfo: Map<String, DocumentMetaInfoEntity>,
    ) {
        when {
            jsonNode.isObject -> {
                val objectNode = jsonNode as ObjectNode
                enhanceObjectNode(objectNode, documentMetaInfo)
            }

            jsonNode.isArray -> {
                jsonNode.forEach { childNode -> enhanceDataSources(childNode, documentMetaInfo) }
            }
        }
    }

    /**
     * Enhances an `ObjectNode`'s properties by enriching `dataSource` objects with additional metadata
     * found in the provided `documentMetaInfo` map. This method operates recursively on the node's properties
     * and any nested structures.
     *
     * @param objectNode the `ObjectNode` to enhance, containing JSON data to inspect and update in place
     * @param documentMetaInfo a map of document metadata indexed by document IDs, used to enrich matching `dataSource` objects
     */
    private fun enhanceObjectNode(
        objectNode: ObjectNode,
        documentMetaInfo: Map<String, DocumentMetaInfoEntity>,
    ) {
        objectNode.properties().forEach { (fieldName, childNode) ->
            if (fieldName == "dataSource" && childNode.isObject) {
                enhanceDataSource(childNode, documentMetaInfo)
            }
            enhanceDataSources(childNode, documentMetaInfo)
        }
    }

    /**
     * Enhances the given JSON data source with metadata from the provided document meta-information map.
     * It updates the JSON object with the corresponding document name and publication date if applicable.
     *
     * @param jsonNode The JSON node to be enhanced. This must be an object node to be processed.
     * @param documentMetaInfo A map containing metadata information, where the key is the file reference
     * and the value is a `DocumentMetaInfoEntity` containing metadata associated with the file.
     */
    private fun enhanceDataSource(
        jsonNode: JsonNode,
        documentMetaInfo: Map<String, DocumentMetaInfoEntity>,
    ) {
        val dataSource = jsonNode as? ObjectNode

        val documentId = dataSource?.path("fileReference")?.takeIf { it.isTextual }?.asText()

        val metadata = documentId?.let { documentMetaInfo[it] }

        if (dataSource != null && metadata != null) {
            metadata.documentName?.let { dataSource.put("fileName", it) }
            metadata.publicationDate?.let { dataSource.put("publicationDate", it.toString()) }
        }
    }
}
