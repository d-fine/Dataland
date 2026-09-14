package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.DataPointUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.orEmpty

object PreApprovalServiceUtils {
    /**
     * Validates that the given per-framework exempt fields lists (as submitted via a PATCH or PUT request) do
     * not contain the same data point type identifier more than once.
     *
     * This is checked at the request stage, before the incoming lists are converted to sets and merged into the
     * persisted [PreApprovalConfig]: converting a `List` to a `Set` silently drops duplicates, so a duplicate
     * submitted by a client would otherwise never be reported back to them.
     *
     * @param exemptFields the per-framework exempt fields lists to check, or `null` if not submitted (e.g. an
     * absent field in a PATCH request, which is left unchanged and therefore not re-validated here)
     * @throws InvalidInputApiException if any framework's list contains a duplicate data point type identifier
     */
    fun validateNoDuplicateExemptFields(exemptFields: Map<DataTypeEnum, List<String>>?) {
        if (exemptFields == null) return

        val issues =
            exemptFields.mapNotNull { (dataType, dataPointTypeIds) ->
                val duplicates =
                    dataPointTypeIds
                        .groupingBy { it }
                        .eachCount()
                        .filterValues { count -> count > 1 }
                        .keys

                if (duplicates.isNotEmpty()) {
                    "Duplicate data point type IDs $duplicates configured in exemptFields for framework $dataType"
                } else {
                    null
                }
            }

        if (issues.isNotEmpty()) {
            throw InvalidInputApiException(
                summary = "Invalid pre-approval config",
                message = issues.joinToString(separator = "; "),
            )
        }
    }

    /**
     * Retrieves a mapping of configured data type enums to their associated list of data point type IDs.
     *
     * This method consolidates the configuration details from the provided `PreApprovalConfig` to produce
     * a unified map, where keys represent `DataTypeEnum` values and each key's value is a list of associated
     * data point type IDs retrieved from the exempt fields, individual decimal thresholds, and individual
     * integer thresholds present in the configuration.
     *
     * @param preApprovalConfig The configuration object containing mappings of data types to related details,
     * such as exempt fields, decimal thresholds, and integer thresholds.
     * @return A map where the keys are `DataTypeEnum` instances and the values are lists of associated
     * data point type IDs.
     */
    fun getConfiguredDataPointTypeIds(preApprovalConfig: PreApprovalConfig): Map<DataTypeEnum, List<String>> {
        val dataTypeEnums =
            buildSet {
                addAll(preApprovalConfig.exemptFields.keys)
                addAll(preApprovalConfig.individualDecimalThresholds.keys)
                addAll(preApprovalConfig.individualIntegerThresholds.keys)
            }

        val allDataPointTypeIds =
            dataTypeEnums.associateWith { dataType ->
                buildList {
                    addAll(preApprovalConfig.exemptFields[dataType].orEmpty())
                    addAll(preApprovalConfig.individualDecimalThresholds[dataType].orEmpty().keys)
                    addAll(preApprovalConfig.individualIntegerThresholds[dataType].orEmpty().keys)
                }
            }

        return allDataPointTypeIds
    }

    /**
     * Checks whether all data point type IDs configured in the pre-approval configuration exist in their
     * respective framework's specification.
     *
     * @param preApprovalConfig The pre-approval configuration containing the data point type IDs to be validated against the framework.
     * @param specificationService The client used to fetch each framework's specification schema.
     * @return a list of human-readable issue descriptions, one per framework with unknown data point type IDs;
     *         empty if all configured data point type IDs are known to their framework's specification.
     */
    fun findUnknownDataPointTypeIdIssues(
        preApprovalConfig: PreApprovalConfig,
        specificationService: SpecificationControllerApi,
    ): List<String> {
        val allDataPointTypeIds = getConfiguredDataPointTypeIds(preApprovalConfig)

        return allDataPointTypeIds.mapNotNull { (dataType, dataPointTypeIds) ->
            val frameworkSpecificationSchema = specificationService.getFrameworkSpecification(dataType.value).schema
            val actualDataPointTypes = DataPointUtils.getDataPointTypes(frameworkSpecificationSchema)

            val unknownDataPointTypeIds = dataPointTypeIds.filter { it !in actualDataPointTypes }
            if (unknownDataPointTypeIds.isEmpty()) {
                null
            } else {
                "Unknown data point type IDs $unknownDataPointTypeIds configured for framework $dataType"
            }
        }
    }

    /**
     * Checks whether any data point type ID that is exempt for a framework is also individually thresholded
     * (as a decimal or integer) for that same framework.
     *
     * @param preApprovalConfig The pre-approval configuration that contains the exempt fields and individual
     * threshold configurations to be validated.
     * @return a list of human-readable issue descriptions, one per framework with such an overlap;
     *         empty if no overlaps are found.
     */
    fun findExemptFieldsOverlappingIndividualThresholdsIssues(preApprovalConfig: PreApprovalConfig): List<String> {
        val dataTypes =
            buildSet {
                addAll(preApprovalConfig.exemptFields.keys)
                addAll(preApprovalConfig.individualDecimalThresholds.keys)
                addAll(preApprovalConfig.individualIntegerThresholds.keys)
            }

        return dataTypes.mapNotNull { dataType ->
            val exemptDataPointTypeIds = preApprovalConfig.exemptFields[dataType].orEmpty()
            val individuallyThresholdedDataPointTypeIds =
                preApprovalConfig.individualDecimalThresholds[dataType].orEmpty().keys +
                    preApprovalConfig.individualIntegerThresholds[dataType].orEmpty().keys

            val overlappingDataPointTypeIds = exemptDataPointTypeIds intersect individuallyThresholdedDataPointTypeIds
            if (overlappingDataPointTypeIds.isEmpty()) {
                null
            } else {
                "Data point type IDs $overlappingDataPointTypeIds are both exempt and individually thresholded " +
                    "for framework $dataType"
            }
        }
    }

    /**
     * Checks whether any data point type ID has both an individual decimal threshold and an individual integer
     * threshold configured for the same framework.
     *
     * @param preApprovalConfig The pre-approval configuration that contains the individual threshold
     * configurations to be validated.
     * @return a list of human-readable issue descriptions, one per framework with such an overlap;
     *         empty if no overlaps are found.
     */
    fun findDecimalAndIntegerThresholdOverlapIssues(preApprovalConfig: PreApprovalConfig): List<String> {
        val dataTypes =
            preApprovalConfig.individualDecimalThresholds.keys +
                preApprovalConfig.individualIntegerThresholds.keys

        return dataTypes.mapNotNull { dataType ->
            val decimalDataPointTypeIds = preApprovalConfig.individualDecimalThresholds[dataType].orEmpty().keys
            val integerDataPointTypeIds = preApprovalConfig.individualIntegerThresholds[dataType].orEmpty().keys

            val overlappingDataPointTypeIds = decimalDataPointTypeIds intersect integerDataPointTypeIds
            if (overlappingDataPointTypeIds.isEmpty()) {
                null
            } else {
                "Data point type IDs $overlappingDataPointTypeIds have both an individual decimal and integer " +
                    "threshold for framework $dataType"
            }
        }
    }
}
