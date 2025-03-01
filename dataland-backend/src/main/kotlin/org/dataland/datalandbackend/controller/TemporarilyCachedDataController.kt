package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TemporarilyCachedDataApi
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.PrivateDataManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream

/**
 * Implementation of the controller for delivering and removing temporarily stored data
 * @param dataManager service to manage data storage
 */
@RestController
class TemporarilyCachedDataController(
    @Autowired var dataManager: DataManager,
    @Autowired var privateDataManager: PrivateDataManager,
) : TemporarilyCachedDataApi {
    override fun getReceivedPublicData(dataId: String): ResponseEntity<String> =
        ResponseEntity.ok(dataManager.selectPublicDatasetFromTemporaryStorage(dataId))

    override fun getBatchReceivedPublicData(dataId: List<String>): ResponseEntity<Map<String, String>> {
        val responseObject = dataId.associateWith { dataManager.selectRawPublicDatasetFromTemporaryStorage(it) }
        return ResponseEntity.ok(responseObject)
    }

    override fun getReceivedPrivateJson(dataId: String): ResponseEntity<String> =
        ResponseEntity
            .ok(privateDataManager.getJsonFromInMemoryStore(dataId))

    override fun getReceivedPrivateDocument(hash: String): ResponseEntity<InputStreamResource> {
        val blob =
            privateDataManager.getDocumentFromInMemoryStore(hash)
                ?: throw ResourceNotFoundApiException(
                    "Documents for hash \"$hash\" not found in temporary storage",
                    "Dataland does not know the files associated to \"$hash\"",
                )
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(ByteArrayInputStream(blob)))
    }
}
