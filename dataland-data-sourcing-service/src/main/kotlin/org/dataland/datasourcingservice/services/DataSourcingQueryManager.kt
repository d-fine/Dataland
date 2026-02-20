package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.utils.isUserAdmin
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class for handling data sourcing queries.
 */
@Service("DataSourcingQueryManager")
class DataSourcingQueryManager(
    @Autowired private val dataSourcingRepository: DataSourcingRepository,
    @Autowired private val companyRolesControllerApi: CompanyRolesControllerApi,
) {
    private fun getCurrentUserProviderCompanyIds(): Set<UUID> {
        val userId = DatalandAuthentication.fromContextOrNull()?.userId ?: return emptySet()
        return companyRolesControllerApi
            .getExtendedCompanyRoleAssignments(userId = ValidationUtils.convertToUUID(userId))
            .map { UUID.fromString(it.companyId) }
            .toSet()
    }

    /**
     * Search data sourcings based on the provided filters and return a paginated chunk.
     * @param companyId to filter by
     * @param dataType to filter by
     * @param reportingPeriod to filter by
     * @param state to filter by
     * @param chunkSize the size of the chunk to return
     * @param chunkIndex the index of the chunk to return
     * @return list of matching StoredDataSourcing objects
     */
    @Transactional(readOnly = true)
    fun searchDataSourcings(
        companyId: UUID?,
        dataType: String?,
        reportingPeriod: String?,
        state: DataSourcingState?,
        chunkSize: Int = 100,
        chunkIndex: Int = 0,
    ): List<StoredDataSourcing> {
        val isAdmin = isUserAdmin()
        val providerCompanyIds = if (isAdmin) emptySet() else getCurrentUserProviderCompanyIds()
        return dataSourcingRepository
            .findByIdsAndFetchAllReferences(
                dataSourcingRepository
                    .searchDataSourcingEntities(
                        companyId, dataType, reportingPeriod, state,
                        PageRequest.of(chunkIndex, chunkSize),
                    ).content,
            ).map { entity ->
                entity.toStoredDataSourcing(
                    isAdmin = isAdmin,
                    isAdminOrProvider =
                        isAdmin ||
                            entity.documentCollector in providerCompanyIds ||
                            entity.dataExtractor in providerCompanyIds,
                )
            }
    }
}
