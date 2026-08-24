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

    data class EnhancedDataPoints(
        val allStoredDataPoints: Map<DataPointId, UploadedDataPoint>,
        val calculatedData: Map<BasicDatasetDimensions, List<UploadedDataPoint>>,
    )

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

    private fun resolveReferenceIds(referenceIds: List<String>): Map<String, DocumentMetaInfoEntity> {
        if (referenceIds.isEmpty()) {
            return emptyMap()
        }

        return documentControllerApi.getDocumentMetaInformationBatch(referenceIds)
    }

    private fun enhanceDataPoint(
        dataPoint: UploadedDataPoint,
        documentMetaInfo: Map<String, DocumentMetaInfoEntity>,
    ): UploadedDataPoint {
        if (documentMetaInfo.isEmpty()) return dataPoint

        val dataPointJson = objectMapper.readTree(dataPoint.dataPoint)
        enhanceDataSources(dataPointJson, documentMetaInfo)
        return dataPoint.copy(dataPoint = objectMapper.writeValueAsString(dataPointJson))
    }

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
