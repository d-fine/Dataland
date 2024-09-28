package org.dataland.e2etests.utils

import org.awaitility.Awaitility.await
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.openApiClient.api.EutaxonomyNonFinancialsDataQaReportControllerApi
import org.dataland.datalandqaservice.openApiClient.api.SfdrDataQaReportControllerApi
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE
import java.util.concurrent.TimeUnit

class QaApiAccessor {
    val sfdrQaReportControllerApi = SfdrDataQaReportControllerApi(BASE_PATH_TO_QA_SERVICE)
    val euTaxonomyNonFinancialsQaReportControllerApi =
        EutaxonomyNonFinancialsDataQaReportControllerApi(
            BASE_PATH_TO_QA_SERVICE,
        )

    /**
     * Wait until QaStatus is accepted for all Upload Infos or throw error. The metadata of the provided uploadInfos
     * are updated in the process.
     *
     * @param uploadInfos List of UploadInfo for which an update of the QaStatus should be checked and awaited
     * @param metaDataControllerApi the api controller for accessing the data
     * @return Input list of UplaodInfo but with updated metadata
     */
    fun ensureQaCompletedAndUpdateUploadInfo(
        uploadInfos: List<UploadInfo>,
        metaDataControllerApi: MetaDataControllerApi,
    ) {
        await().atMost(10, TimeUnit.SECONDS).until {
            checkIfQaPassedAndUpdateUploadInfo(uploadInfos, metaDataControllerApi)
        }
    }

    private fun checkIfQaPassedAndUpdateUploadInfo(
        uploadInfos: List<UploadInfo>,
        metaDataControllerApi: MetaDataControllerApi,
    ): Boolean {
        return uploadInfos.all { uploadInfo ->
            val metaData =
                uploadInfo.actualStoredDataMetaInfo
                    ?: throw NullPointerException(
                        "To check QA Status, metadata is required but was null for $uploadInfo",
                    )
            if (metaData.qaStatus != QaStatus.Accepted) {
                uploadInfo.actualStoredDataMetaInfo = metaDataControllerApi.getDataMetaInfo(metaData.dataId)
            }
            return uploadInfo.actualStoredDataMetaInfo!!.qaStatus == QaStatus.Accepted
        }
    }

    /**
     * Waits until the status of all provided [metaDatas] is QaStatus.Accepted. Then returns an updated list of metaData
     * each of which has qaStatus = QaStatus.Accepted.
     * @param metaDatas the metadatas whose IDs to check for
     * @param metaDataControllerApi the api controller for accessing the data
     */
    fun ensureQaIsPassed(
        metaDatas: List<DataMetaInformation>,
        metaDataControllerApi: MetaDataControllerApi,
    ): List<DataMetaInformation> {
        await().atMost(10, TimeUnit.SECONDS).until {
            checkIfQaPassedForMetaDataList(metaDatas, metaDataControllerApi)
        }
        val updatedMetaDatas = mutableListOf<DataMetaInformation>()
        metaDatas.forEach { metaData ->
            updatedMetaDatas.add(metaDataControllerApi.getDataMetaInfo(metaData.dataId))
        }
        return updatedMetaDatas
    }

    private fun checkIfQaPassedForMetaDataList(
        metaDatas: List<DataMetaInformation>,
        metaDataControllerApi: MetaDataControllerApi,
    ): Boolean {
        return metaDatas.all { metaData ->
            return (metaDataControllerApi.getDataMetaInfo(metaData.dataId).qaStatus == QaStatus.Accepted)
        }
    }
}
