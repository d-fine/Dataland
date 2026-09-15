package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackendutils.interfaces.DataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.model.StorableDataPoint
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class RawDataPointTypeSpecification(
    val id: String,
    val name: String,
    val businessDefinition: String,
    val dataPointBaseTypeId: String,
    val frameworkOwnership: List<String>,
    val calculationRules: List<CalculationRule> = emptyList(),
)

internal data class AssembledDataManagerTestContext(
    val datasetId: String,
    val dataDimensions: BasicDatasetDimensions,
    val uploaderUserId: String,
)

internal class AssembledDataManagerTestHelpers(
    private val datasetDatapointRepository: DatasetDatapointRepository,
    private val metaDataManager: DataPointMetaInformationManager,
    private val dataAvailabilityChecker: DataAvailabilityChecker,
    private val storageClient: StorageControllerApi,
    private val context: AssembledDataManagerTestContext,
) {
    fun setMockData(
        dataPoints: Map<String, String>,
        dataContent: Map<String, String>,
    ) {
        doReturn(
            Optional.of(
                DatasetDatapointEntity(
                    datasetId = context.datasetId,
                    dataPoints = dataPoints,
                ),
            ),
        ).whenever(datasetDatapointRepository).findById(context.datasetId)

        doAnswer { invocation ->
            invocation.getArgument<Collection<String>>(0).map { dataPointId ->
                makeDataPointMetaInfo(dataPointId, dataPoints.filterValues { it == dataPointId }.keys.first())
            }
        }.whenever(metaDataManager).getDataPointMetaInformationByIds(any())

        doReturn(
            dataPoints.map { (dataPointType, dataPointId) -> makeDataPointMetaInfo(dataPointId, dataPointType) },
        ).whenever(metaDataManager).getActiveDataPointMetaInformationList(any<List<DataPointDimensions>>())

        doAnswer { invocation ->
            val dimensionsByDataset =
                invocation.getArgument<Map<BasicDatasetDimensions, Collection<BasicDataPointDimensions>>>(0)
            dimensionsByDataset.mapValues { (_, dimensions) ->
                dimensions.mapNotNull { dimension ->
                    dataPoints[dimension.dataPointType]?.let { dataPointId ->
                        makeDataPointMetaInfo(dataPointId, dimension.dataPointType, dimension.companyId, dimension.reportingPeriod)
                    }
                }
            }
        }.whenever(dataAvailabilityChecker)
            .getViewableDataPointMetaData(any<Map<BasicDatasetDimensions, Collection<BasicDataPointDimensions>>>())

        doAnswer { invocation ->
            val dataPointId = invocation.getArgument<List<String>>(1)
            dataPointId.associateWith { id ->
                StorableDataPoint(
                    dataPoint = dataContent[id] ?: "",
                    dataPointType = dataPoints.filterValues { it == id }.keys.first(),
                    companyId = context.dataDimensions.companyId,
                    reportingPeriod = context.dataDimensions.reportingPeriod,
                )
            }
        }.whenever(storageClient).selectBatchDataPointsByIds(any(), any())
    }

    private fun makeDataPointMetaInfo(
        dataPointId: String,
        dataPointType: String,
        companyId: String = context.dataDimensions.companyId,
        reportingPeriod: String = context.dataDimensions.reportingPeriod,
    ) = DataPointMetaInformationEntity(
        dataPointId = dataPointId,
        companyId = companyId,
        dataPointType = dataPointType,
        reportingPeriod = reportingPeriod,
        uploaderUserId = context.uploaderUserId,
        uploadTime = Instant.now().toEpochMilli(),
        currentlyActive = true,
        qaStatus = QaStatus.Accepted,
    )
}
