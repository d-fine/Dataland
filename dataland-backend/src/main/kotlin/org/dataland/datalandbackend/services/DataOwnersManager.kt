package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyDataOwnersEntity
import org.dataland.datalandbackend.repositories.DataOwnerRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a (company) data ownership manager for Dataland
 * @param dataOwnerRepository  JPA for data ownership relations
 * @param companyQueryManager service to query companies stored on Dataland
 */
@Service("DataRequestManager")
class DataOwnersManager(
    @Autowired private val dataOwnerRepository: DataOwnerRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to add a data owner to a given company
     * @param companyId the ID of the company to which the data owner is to be added
     * @param uploadUserId the ID of the user who is to become a data owner
     * @return an entity holding the data ownership relations for the given company
     */
    @Transactional
    fun addDataOwnerToCompany(companyId: String, uploadUserId: String): CompanyDataOwnersEntity {
        if (dataOwnerRepository.existsById(companyId)) {
            val dataOwnersForCompany = dataOwnerRepository.findById(companyId).get()
            return if (dataOwnersForCompany.dataOwners.contains(uploadUserId)) {
                logger.info(
                    "User with Id $uploadUserId is already data owner of company " +
                        companyQueryManager.getCompanyById(companyId).companyName,
                )
                dataOwnersForCompany
            } else {
                dataOwnersForCompany.dataOwners.add(uploadUserId)
                dataOwnerRepository.save(dataOwnersForCompany)
            }
        } else {
            return dataOwnerRepository.save(
                CompanyDataOwnersEntity(
                    companyId = companyId,
                    dataOwners = mutableListOf(uploadUserId),
                ),
            )
        }
    }
}
