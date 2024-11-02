package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional

class DataPointManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataMetaInformationManager::class.java)
    private val specificationManager = mock(SpecificationControllerApi::class.java)
    private val datasetDatapointRepository = mock(DatasetDatapointRepository::class.java)
    private val dataPointManager = DataPointManager(dataManager, metaDataManager, specificationManager, datasetDatapointRepository)

    private val correlationId = "test-correlation-id"

    @Test
    fun `retrieveDataPoint should throw InvalidInputApiException when dataPointIdentifier does not exist`() {
        val dataId = "test-data-id"
        val dataPointIdentifier = "non-existent-identifier"

        `when`(specificationManager.getDataPointSpecification(dataPointIdentifier))
            .thenThrow(ClientException("Data point identifier not found"))

        assertThrows<InvalidInputApiException> {
            dataPointManager.retrieveDataPoint(dataId, dataPointIdentifier, correlationId)
        }
    }

    @Test
    fun `getDataSetFromId should throw InvalidInputApiException when dataSetId does not exist`() {
        val dataSetId = "non-existent-data-set-id"
        val framework = "test-framework"

        `when`(datasetDatapointRepository.findById(dataSetId)).thenReturn(Optional.empty())

        assertThrows<InvalidInputApiException> {
            dataPointManager.getDataSetFromId(dataSetId, framework, correlationId)
        }
    }
}
