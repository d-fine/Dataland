package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReview
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service class for dataset review objects.
 */
@Service
class DatasetReviewService(
    @Autowired var datasetReviewRepository: DatasetReviewRepository,
) {
    /**
     * Method to set reviewer to current user.
     */
    @Transactional
    fun setReviewer(datasetReviewId: UUID): DatasetReview {
        val datasetReview =
            datasetReviewRepository.findById(datasetReviewId).orElseThrow {
                ResourceNotFoundApiException(
                    "Dataset review object not found",
                    "NoDataset review object with the id: $datasetReviewId could be found.",
                )
            }
        datasetReview.reviewerUserId = DatalandAuthentication.fromContext().userId

        return datasetReviewRepository.save(datasetReview).toDatasetReview()
    }
}
