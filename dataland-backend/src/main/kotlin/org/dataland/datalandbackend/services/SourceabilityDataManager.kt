package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.SourceabilityEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfo
import org.dataland.datalandbackend.model.metainformation.SourceabilityInfoResponse
import org.dataland.datalandbackend.repositories.SourceabilityDataRepository
import org.dataland.datalandbackend.repositories.utils.NonSourceableDataSearchFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * A service class for managing information about the sourceabilty of datasets.
 */
@Service("SourceabilityDataManager")
class SourceabilityDataManager(
    @Autowired private val sourceabilityDataRepository: SourceabilityDataRepository,
) {
    /**
     * The method stores meta information to a non-sourceable dataset in the nonSourceableDataRepository
     * @param sourceabilityInfo the of the dataset
     */
    @Transactional
    fun storeNonSourceableData(sourceabilityInfo: SourceabilityInfo): SourceabilityInfoResponse? {
        val creationTime = Instant.now().toEpochMilli()
        val userId = DatalandAuthentication.fromContext().userId
        val sourceabilityEntity =
            SourceabilityEntity(
                eventId = null,
                companyId = sourceabilityInfo.companyId,
                dataType = sourceabilityInfo.dataType,
                reportingPeriod = sourceabilityInfo.reportingPeriod,
                isNonSourceable = sourceabilityInfo.isNonSourceable,
                reason = sourceabilityInfo.reason,
                creationTime = creationTime,
                userId = userId,
            )
        return sourceabilityDataRepository.save(sourceabilityEntity).toApiModel()
    }

    /**
     * The method retrieves non-sourceable datasets by given filters.
     * @param companyId if not empty, filters the requested information by companyId.
     * @param dataType if not empty, filters the requested information by data type.
     * @param reportingPeriod if not empty, filters the requested information by reporting period.
     * @param nonSourceable if not null, filters the requested information to include only datasets
     *                      with a non-sourceable flag matching the provided value (true or false).
     * @return a list of SourceabilityInfoResponse objects that match the specified filters.
     */
    fun getSourceabilityDataByFilters(
        companyId: String?,
        dataType: DataType?,
        reportingPeriod: String?,
        nonSourceable: Boolean?,
    ): List<SourceabilityInfoResponse> {
        val sourceabilityEntities =
            sourceabilityDataRepository
                .searchNonSourceableData(
                    NonSourceableDataSearchFilter(
                        companyId,
                        dataType,
                        reportingPeriod,
                        nonSourceable,
                    ),
                )
        return sourceabilityEntities.map { it.toApiModel() }
    }

    /**
     * Gets the latest non-sourceable info for triple (companyId, dataType, reportingPeriod)
     * @param companyId companyId
     * @param dataType dataType
     * @param reportingPeriod reportingPeriod
     * @return most recent SourceabilityInfoResponse for triple (companyId, dataType, reportingPeriod)
     */
    fun getLatestSourceabilityInfoForDataset(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
    ): SourceabilityInfoResponse? =
        sourceabilityDataRepository
            .getLatestSourceabilityInfoForDataset(
                NonSourceableDataSearchFilter(
                    companyId,
                    dataType,
                    reportingPeriod,
                ),
            )?.toApiModel()

    /**
     * Stores a NonSourceableEntity in the data-sourceability table, marking the previously
     * non-sourceable dataset as sourceable. This is triggered by the upload of the dataset.
     *
     * @param companyId the ID of the company associated with the dataset.
     * @param dataType the type of the dataset being uploaded.
     * @param reportingPeriod the reporting period of the dataset, typically a specific year or quarter.
     * @param uploaderId the ID of the user who uploaded the dataset, used for audit purposes.
     */
    fun storeSourceableData(
        companyId: String,
        dataType: DataType,
        reportingPeriod: String,
        uploaderId: String,
    ): SourceabilityInfoResponse? {
        val creationTime = Instant.now().toEpochMilli()

        val sourceabilityEntity =
            SourceabilityEntity(
                eventId = null,
                companyId = companyId,
                dataType = dataType,
                reportingPeriod = reportingPeriod,
                isNonSourceable = false,
                reason = "Uploaded by a user with the Id:$uploaderId",
                creationTime = creationTime,
                userId = uploaderId,
            )
        return sourceabilityDataRepository.save(sourceabilityEntity).toApiModel()
    }
}
