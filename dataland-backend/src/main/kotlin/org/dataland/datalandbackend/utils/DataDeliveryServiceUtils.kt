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
                objectNode.properties().forEach { (fieldName, childNode) ->
                    if (fieldName == "dataSource" && childNode.isObject) {
                        val dataSource = childNode as ObjectNode
                        dataSource.path("fileReference").takeIf { it.isTextual }?.asText()?.let { documentId ->
                            documentMetaInfo[documentId]?.let { metadata ->
                                metadata.documentName?.let { dataSource.put("fileName", it) }
                                metadata.publicationDate?.let { dataSource.put("publicationDate", it.toString()) }
                            }
                        }
                    }
                    enhanceDataSources(childNode, documentMetaInfo)
                }
            }
            jsonNode.isArray -> jsonNode.forEach { childNode -> enhanceDataSources(childNode, documentMetaInfo) }
        }
    }
}
