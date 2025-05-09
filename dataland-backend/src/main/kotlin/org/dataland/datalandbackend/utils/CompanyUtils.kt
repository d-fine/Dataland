package org.dataland.datalandbackend.utils

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A utility class for company data associated functions
 */

@Service("CompanyUtils")
class CompanyUtils
    @Autowired
    constructor(
        private val companyRepository: StoredCompanyRepository,
    ) {
        /**
         * Method to verify if a company ID represents an actual company on Dataland or not
         * @param companyId the ID of the to be verified company
         * @return a boolean signaling if the company exists or not
         */
        fun checkCompanyIdExists(companyId: String): Boolean = companyRepository.existsById(companyId)

        /**
         * Method to verify that a given company exists in the company store
         * @param companyId the ID of the company to be verified
         * @throws ResourceNotFoundApiException if the company does not exist
         */
        @Throws(ResourceNotFoundApiException::class)
        fun assertCompanyIdExists(companyId: String) {
            if (!checkCompanyIdExists(companyId)) {
                throw ResourceNotFoundApiException("Company not found", "Dataland does not know the company ID $companyId")
            }
        }

        /**
         * Return a company by ID and throw an exception if it does not exist.
         */
        fun getCompanyByIdAndAssertExistence(companyId: String): StoredCompanyEntity {
            assertCompanyIdExists(companyId)
            return companyRepository.findById(companyId).get()
        }
    }
