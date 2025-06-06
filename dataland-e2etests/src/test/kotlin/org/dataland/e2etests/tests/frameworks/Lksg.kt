package org.dataland.e2etests.tests.frameworks

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.LksgData
import org.dataland.datalandbackend.openApiClient.model.LksgGrievanceAssessmentMechanism
import org.dataland.datalandbackend.openApiClient.model.LksgProcurementCategory
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.testDataProviders.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Lksg {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()

    private val listOfOneLksgDataset = apiAccessor.testDataProviderForLksgData.getTData(1)
    private val listOfOneCompanyInformation =
        apiAccessor.testDataProviderForLksgData
            .getCompanyInformationWithoutIdentifiers(1)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun removeNullMapEntriesFromSupplierCountryCountAndSortAllRiskPositions(dataset: LksgData): LksgData {
        val fixedDataset = dataset.copy()
        // The following block is a workaround to circumvent a bug in the generated clients
        // which do not allow for null entries as map values but retain them at the same time.
        // On upload, however, they are not being serialized.
        fixedDataset.general.productionSpecificOwnOperations?.procurementCategories?.forEach {
            val keysOfEntriesToDelete = mutableListOf<String>()
            it.value.numberOfSuppliersPerCountryCode?.forEach { numberOfSuppliersPerCountry ->
                if (numberOfSuppliersPerCountry.value == null) {
                    keysOfEntriesToDelete.add(numberOfSuppliersPerCountry.key)
                }
            }
            keysOfEntriesToDelete.forEach { key ->
                (it.value.numberOfSuppliersPerCountryCode as? MutableMap<String, LksgProcurementCategory>)?.remove(key)
            }
        }

        val fixedDataSetWithSortedComplaintsRisks = sortComplaintRisksInDataset(fixedDataset)
        val fixedDataSetWithAllSortedRiskPositions =
            sortDatasetsInSecondTest(
                listOf(fixedDataSetWithSortedComplaintsRisks),
            )[0]

        return fixedDataSetWithAllSortedRiskPositions
    }

    @Test
    fun `post a company with Lksg data and check if the data can be retrieved correctly`() {
        val fixedDataSet = removeNullMapEntriesFromSupplierCountryCountAndSortAllRiskPositions(listOfOneLksgDataset[0])
        val listOfUploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                listOfOneCompanyInformation,
                listOf(fixedDataSet),
                apiAccessor::lksgUploaderFunction,
            )
        val receivedDataMetaInformation = listOfUploadInfo[0].actualStoredDataMetaInfo
        val downloadedAssociatedData =
            apiAccessor.dataControllerApiForLksgData
                .getCompanyAssociatedLksgData(receivedDataMetaInformation!!.dataId)
        val downloadedAssociatedDataType =
            apiAccessor.metaDataControllerApi
                .getDataMetaInfo(receivedDataMetaInformation.dataId)
                .dataType

        assertEquals(receivedDataMetaInformation.companyId, downloadedAssociatedData.companyId)
        assertEquals(receivedDataMetaInformation.dataType, downloadedAssociatedDataType)
        assertEquals(sortDatasetsInFirstTest(fixedDataSet), downloadedAssociatedData.data)
    }

    @Test
    fun `check that dataset cannot be uploaded if document does not exist`() {
        val companyId = "1908273127903192839781293898312983"
        val companyName = "TestForBrokenFileReference"
        val companyInformation =
            FrameworkTestDataProvider.forFrameworkPreparedFixtures(LksgData::class.java).getByCompanyName(companyName)
        val lksgData = companyInformation.t

        val dataset = removeNullMapEntriesFromSupplierCountryCountAndSortAllRiskPositions(lksgData)

        val uploadPair = Pair(dataset, "2022")

        val exception =
            assertThrows<ClientException> {
                apiAccessor.uploadWithWait(
                    companyId = companyId,
                    frameworkData = uploadPair.first,
                    reportingPeriod = uploadPair.second,
                    uploadFunction = apiAccessor::lksgUploaderFunction,
                )
            }

        val testClientError = exception.response as ClientError<*>

        assertTrue(testClientError.statusCode == 400)
        assertTrue(testClientError.body.toString().contains("Invalid input"))
        assertTrue(testClientError.body.toString().contains("The document reference doesn't exist"))
    }

    private fun sortComplaintRisksInDataset(dataset: LksgData): LksgData {
        val complaintRisksIdentifiedRisks =
            dataset.governance
                ?.grievanceMechanismOwnOperations
                ?.complaintsRiskPosition
                ?.toMutableList() ?: mutableListOf()
        val complaintRisksIdentifiedRisksSorted: MutableList<LksgGrievanceAssessmentMechanism> = mutableListOf()
        for (i in complaintRisksIdentifiedRisks.indices) {
            complaintRisksIdentifiedRisksSorted.add(
                complaintRisksIdentifiedRisks[i].copy(
                    complaintRisksIdentifiedRisks[i].riskPositions.sorted(),
                ),
            )
        }

        val datasetWithSortedIdentifiedRisks =
            dataset.copy(
                governance =
                    dataset.governance?.copy(
                        grievanceMechanismOwnOperations =
                            dataset.governance?.grievanceMechanismOwnOperations?.copy(
                                complaintsRiskPosition = complaintRisksIdentifiedRisksSorted.sortedBy { it.riskPositions.first() },
                            ),
                    ),
            )
        return datasetWithSortedIdentifiedRisks
    }

    private fun sortDatasetsInFirstTest(fixedDataSet: LksgData): LksgData {
        val firstSorting =
            fixedDataSet.copy(
                general =
                    fixedDataSet.general.copy(
                        productionSpecific =
                            fixedDataSet.general.productionSpecific?.copy(
                                specificProcurement =
                                    fixedDataSet.general.productionSpecific
                                        ?.specificProcurement
                                        ?.sorted(),
                            ),
                    ),
                governance =
                    fixedDataSet.governance?.copy(
                        riskManagementOwnOperations =
                            fixedDataSet.governance?.riskManagementOwnOperations?.copy(
                                identifiedRisks =
                                    fixedDataSet.governance?.riskManagementOwnOperations?.identifiedRisks?.sortedBy
                                        { it.riskPosition },
                            ),
                        generalViolations =
                            fixedDataSet.governance?.generalViolations?.copy(
                                humanRightsOrEnvironmentalViolationsDefinition =
                                    fixedDataSet.governance
                                        ?.generalViolations
                                        ?.humanRightsOrEnvironmentalViolationsDefinition
                                        ?.sortedBy { it.riskPosition },
                            ),
                    ),
            )
        val complaintsRiskPosition = firstSorting.governance?.grievanceMechanismOwnOperations?.complaintsRiskPosition
        complaintsRiskPosition?.forEach { it.riskPositions.sorted() }

        val secondSorting =
            firstSorting.copy(
                governance =
                    firstSorting.governance?.copy(
                        grievanceMechanismOwnOperations =
                            firstSorting.governance!!.grievanceMechanismOwnOperations?.copy(
                                complaintsRiskPosition = complaintsRiskPosition?.sortedBy { it.riskPositions.first() },
                            ),
                    ),
            )
        return secondSorting
    }

    private fun sortDatasetsInSecondTest(uploadedDatasets: List<LksgData>): List<LksgData> {
        val sortedUploadedDatasets = mutableListOf<LksgData>()
        uploadedDatasets.forEach { dataset ->
            val sortedDataset = sortComplaintRisksInDataset(dataset)

            sortedUploadedDatasets.add(
                dataset.copy(
                    general =
                        sortedDataset.general.copy(
                            productionSpecific =
                                sortedDataset.general.productionSpecific?.copy(
                                    specificProcurement =
                                        sortedDataset.general.productionSpecific
                                            ?.specificProcurement
                                            ?.sorted(),
                                ),
                        ),
                    governance =
                        sortedDataset.governance?.copy(
                            riskManagementOwnOperations =
                                sortedDataset.governance?.riskManagementOwnOperations?.copy(
                                    identifiedRisks =
                                        sortedDataset.governance
                                            ?.riskManagementOwnOperations
                                            ?.identifiedRisks
                                            ?.sortedBy { it.riskPosition },
                                ),
                            grievanceMechanismOwnOperations =
                                sortedDataset.governance?.grievanceMechanismOwnOperations?.copy(
                                    complaintsRiskPosition =
                                        sortedDataset.governance
                                            ?.grievanceMechanismOwnOperations
                                            ?.complaintsRiskPosition
                                            ?.sortedBy
                                            { it.riskPositions.first() },
                                ),
                            generalViolations =
                                sortedDataset.governance?.generalViolations?.copy(
                                    humanRightsOrEnvironmentalViolationsDefinition =
                                        sortedDataset.governance
                                            ?.generalViolations
                                            ?.humanRightsOrEnvironmentalViolationsDefinition
                                            ?.sortedBy
                                            { it.riskPosition },
                                ),
                        ),
                ),
            )
        }
        return sortedUploadedDatasets
    }
}
